package ru.j2dev.gameserver.model.entity.olympiad;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.database.mysql;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.entity.HeroDiary;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager.NobleRecord;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadGameManager.CompetitionResults;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.pledge.Alliance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Strings;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@HideAccess
@StringEncryption
public class HeroManager {
    public static final int[] HERO_WEAPONS = {6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621, 9388, 9389, 9390};
    private static final Logger LOGGER = LoggerFactory.getLogger(HeroManager.class);
    private static final String SQL_GET_HEROES = "SELECT  `oly_heroes`.`char_id` AS `char_id`, `oly_nobles`.`char_name` AS `name`, `oly_nobles`.`class_id` AS `class_id`, `oly_heroes`.`count` AS `count`, `oly_heroes`.`played` AS `played`, `oly_heroes`.`active` AS `active`, `oly_heroes`.`message` AS `message` FROM    `oly_heroes`,`oly_nobles` WHERE   `oly_heroes`.`char_id` = `oly_nobles`.`char_id`";
    private static final String SQL_SET_HEROES = "REPLACE INTO `oly_heroes` (`char_id`, `count`, `played`, `active`, `message`) VALUES (?, ?, ?, ?, ?)";
    private static Map<Integer, List<HeroDiary>> _herodiary = new ConcurrentHashMap<>();
    private static Map<Integer, String> _heroMessage = new ConcurrentHashMap<>();;

    private final ArrayList<HeroRecord> _currentHeroes = new ArrayList<>();
    private final ArrayList<HeroRecord> _allHeroes = new ArrayList<>();

    private HeroManager() {
        loadHeroes();
    }

    public static HeroManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public static void addSkills(final Player player) {
        player.addSkill(SkillTable.getInstance().getInfo(395, 1));
        player.addSkill(SkillTable.getInstance().getInfo(396, 1));
        player.addSkill(SkillTable.getInstance().getInfo(1374, 1));
        player.addSkill(SkillTable.getInstance().getInfo(1375, 1));
        player.addSkill(SkillTable.getInstance().getInfo(1376, 1));
        player.sendSkillList();
    }

    public static void removeSkills(final Player player) {
        player.removeSkillById(395);
        player.removeSkillById(396);
        player.removeSkillById(1374);
        player.removeSkillById(1375);
        player.removeSkillById(1376);
        player.sendSkillList();
    }

    public static boolean isHaveHeroWeapon(final Player player) {
        return Arrays.stream(HERO_WEAPONS).anyMatch(iid -> player.getInventory().getCountOf(iid) > 0L);
    }

    private static void removeAllHeroWeapons(final Player player) {
        boolean removed = false;
        for (final int itemId : HERO_WEAPONS) {
            while (player.getInventory().destroyItemByItemId(itemId, 1L)) {
                removed = true;
            }
            if (removed) {
                player.sendPacket(new SystemMessage(1726).addItemName(itemId));
            }
            removed = false;
        }
    }

    public static void checkHeroWeaponary(final Player player) {
        if (!player.isHero()) {
            removeAllHeroWeapons(player);
        }
    }

    private synchronized Collection<NobleRecord> CalcHeroesContenders() {
        LOGGER.info("HeroManager: Calculating heroes contenders.");
        final HashMap<ClassId, NobleRecord> hero_contenders_map = new HashMap<>();
        for (final NobleRecord nr : NoblessManager.getInstance().getNoblesRecords()) {
            try {
                if (nr.comp_done < Config.OLY_MIN_HERO_COMPS) {
                    continue;
                }
                if (nr.comp_win < Config.OLY_MIN_HERO_WIN) {
                    continue;
                }
                ClassId cid = null;
                for (final ClassId cid2 : ClassId.values()) {
                    if (cid2.getId() == nr.class_id && cid2.level() == 3) {
                        cid = cid2;
                    }
                }
                if (cid == null) {
                    LOGGER.warn("HeroManager: Not third or null ClassID for character '" + nr.char_name + "'");
                } else if (hero_contenders_map.containsKey(cid)) {
                    final NobleRecord nr2 = hero_contenders_map.get(cid);
                    if (nr.points_current <= nr2.points_current && (nr.points_current != nr2.points_current || nr.comp_win <= nr2.comp_win)) {
                        continue;
                    }
                    hero_contenders_map.put(cid, nr);
                } else {
                    hero_contenders_map.put(cid, nr);
                }
            } catch (Exception ex) {
                LOGGER.warn("HeroManager: Exception while claculating new heroes", ex);
            }
        }
        hero_contenders_map.values().forEach(nr -> Log.add(String.format("%s(%d) pretended to be a hero. points_current = %d", nr.char_name, nr.char_id, nr.points_current), "olympiad"));
        return new ArrayList<>(hero_contenders_map.values());
    }

    private void loadHeroes() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            stmt = conn.createStatement();
            rset = stmt.executeQuery(SQL_GET_HEROES);
            while (rset.next()) {
                final int char_id = rset.getInt("char_id");
                final String name = rset.getString("name");
                final int class_id = rset.getInt("class_id");
                final int count = rset.getInt("count");
                final boolean played = rset.getInt("played") != 0;
                final boolean active = rset.getInt("active") != 0;
                final String message = rset.getString("message");
                final HeroRecord nr = new HeroRecord(char_id, name, class_id, count, active, played, message);
                if (played) {
                    _currentHeroes.add(nr);
                }
                _allHeroes.add(nr);
            }
            applyClanAndAlly();
        } catch (Exception ex) {
            LOGGER.warn("HeroManager: Exception while loading heroes", ex);
        } finally {
            DbUtils.closeQuietly(conn, stmt, rset);
        }
    }

    public void saveHeroes() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement(SQL_SET_HEROES);
            for (final HeroRecord nr : _allHeroes) {
                pstmt.setInt(1, nr.char_id);
                pstmt.setInt(2, nr.count);
                pstmt.setInt(3, nr.played ? 1 : 0);
                pstmt.setInt(4, nr.active ? 1 : 0);
                pstmt.setString(5, nr.message);
                pstmt.executeUpdate();
            }
        } catch (Exception ex) {
            LOGGER.warn("HeroManager: Exception while saving heroes", ex);
        } finally {
            DbUtils.closeQuietly(conn, pstmt);
        }
    }

    private void clearHeroes() {
        LOGGER.info("HeroManager: Clearing previus season heroes.");
        mysql.set("UPDATE `oly_heroes` SET `played` = 0, `active` = 0");
        if (!_currentHeroes.isEmpty()) {
            _currentHeroes.stream().filter(nr -> nr.active).forEach(nr -> {
                final Player player = GameObjectsStorage.getPlayer(nr.char_id);
                if (player != null) {
                    player.getInventory().unEquipItemInBodySlot(0x100);
                    player.getInventory().unEquipItemInBodySlot(0x80);
                    player.getInventory().unEquipItemInBodySlot(0x4000);
                    player.getInventory().unEquipItemInBodySlot(0x10000);
                    player.getInventory().unEquipItemInBodySlot(0x80000);
                    player.getInventory().unEquipItemInBodySlot(0x40000);
                    player.getInventory().getItems().stream().filter(Objects::nonNull).filter(ItemInstance::isHeroWeapon).forEach(item -> player.getInventory().destroyItem(item));
                    player.getWarehouse().getItems().stream().filter(Objects::nonNull).filter(item -> !item.isEquipable()).filter(ItemInstance::isHeroWeapon).forEach(item -> player.getWarehouse().destroyItem(item));
                    player.unsetVar("CustomHeroEndTime");
                    player.setHero(false);
                    player.updatePledgeClass();
                    player.broadcastUserInfo(true);
                    removeAllHeroWeapons(player);
                }
                nr.played = false;
                nr.active = false;
            });
        }
        saveHeroes();
        _currentHeroes.clear();
    }

    public synchronized void ComputeNewHeroNobleses() {
        LOGGER.info("HeroManager: Computing new heroes.");
        try {
            NoblessManager.getInstance().SaveNobleses();
            saveHeroes();
            final Collection<NobleRecord> hContenders = CalcHeroesContenders();
            clearHeroes();
            for (final NobleRecord hnr : hContenders) {
                HeroRecord hr = null;
                for (final HeroRecord hr2 : _allHeroes) {
                    if (hnr.char_id == hr2.char_id) {
                        hr = hr2;
                    }
                }
                if (hr == null) {
                    hr = new HeroRecord(hnr.char_id, hnr.char_name, hnr.class_id, 0, false, true, "");
                    _allHeroes.add(hr);
                }
                final HeroRecord heroRecord = hr;
                ++heroRecord.count;
                hr.played = true;
                _currentHeroes.add(hr);
            }
            saveHeroes();
            NoblessManager.getInstance().TransactNewSeason();
            NoblessManager.getInstance().ComputeRanks();
            NoblessManager.getInstance().SaveNobleses();
            applyClanAndAlly();
        } catch (Exception ex) {
            LOGGER.warn("HeroManager: Can't compute heroes.", ex);
        }
    }

    private void applyClanAndAlly() {
        _currentHeroes.stream().filter(Objects::nonNull).forEach(hr -> {
            final Entry<Clan, Alliance> e = ClanTable.getInstance().getClanAndAllianceByCharId(hr.char_id);
            if (e.getKey() != null) {
                hr.clan_name = e.getKey().getName();
                hr.clan_crest = e.getKey().getCrestId();
            } else {
                hr.clan_name = "";
                hr.clan_crest = 0;
            }
            if (e.getValue() != null) {
                hr.ally_name = e.getValue().getAllyName();
                hr.ally_crest = e.getValue().getAllyCrestId();
            } else {
                hr.ally_name = "";
                hr.ally_crest = 0;
            }
        });
    }

    public Collection<HeroRecord> getCurrentHeroes() {
        return _currentHeroes;
    }

    public boolean isCurrentHero(final Player player) {
        return player != null && !_currentHeroes.isEmpty() && isCurrentHero(player.getObjectId());
    }

    public boolean isInactiveHero(final Player player) {
        return player != null && isInactiveHero(player.getObjectId());
    }

    public void activateHero(final Player player) {
        if (player == null) {
            return;
        }
        if (_currentHeroes.isEmpty()) {
            return;
        }
        _currentHeroes.stream().filter(hr -> hr.char_id == player.getObjectId() && hr.played).forEach(hr -> {
            hr.active = true;
            if (player.getBaseClassId() == player.getActiveClassId()) {
                addSkills(player);
            }
            player.setHero(true);
            player.unsetVar("CustomHeroEndTime");
            player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
            player.updatePledgeClass();
            player.getPlayer().sendUserInfo(true);
            if (player.getClan() != null && player.getClan().getLevel() >= 5) {
                player.getClan().incReputation(1000, true, "Hero:activateHero:" + player);
                player.getClan().broadcastToOtherOnlineMembers(new SystemMessage(1776).addString(player.getName()).addNumber(Math.round(1000.0 * Config.RATE_CLAN_REP_SCORE)), player);
            }
            player.broadcastUserInfo(true);
            saveHeroes();
        });
    }

    public boolean isCurrentHero(final int obj_id) {
        if (_currentHeroes.isEmpty()) {
            return false;
        }
        return _currentHeroes.stream().filter(hr -> hr.char_id == obj_id).findFirst().filter(hr -> hr.active && hr.played).isPresent();
    }

    public boolean isInactiveHero(final int obj_id) {
        if (_currentHeroes.isEmpty()) {
            return false;
        }
        return _currentHeroes.stream().filter(hr -> hr.char_id == obj_id).findFirst().filter(hr -> hr.played && !hr.active).isPresent();
    }

    public void showHistory(final Player player, final int targetClassId, final int page) {
        final int perpage = 15;
        HeroRecord hr = null;
        for (final HeroRecord hr2 : getCurrentHeroes()) {
            if (hr2.active && hr2.played && hr2.class_id == targetClassId) {
                hr = hr2;
            }
        }
        if (hr == null) {
            return;
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, null);
        html.setFile("olympiad/monument_hero_info.htm");
        html.replace("%title%", StringHolder.getInstance().getNotNull(player, "hero.history"));
        final Collection<CompetitionResults> crs_list = hr.getCompetitions();
        final CompetitionResults[] crs = crs_list.toArray(new CompetitionResults[0]);
        int allStatWinner = 0;
        int allStatLoss = 0;
        int allStatTie = 0;
        for (final CompetitionResults h : crs) {
            if (h.result > 0) {
                ++allStatWinner;
            } else if (h.result < 0) {
                ++allStatLoss;
            } else {
                ++allStatTie;
            }
        }
        html.replace("%wins%", String.valueOf(allStatWinner));
        html.replace("%ties%", String.valueOf(allStatTie));
        html.replace("%losses%", String.valueOf(allStatLoss));
        final int min = perpage * (page - 1);
        final int max = perpage * page;
        final MutableInt currentWinner = new MutableInt(0);
        final MutableInt currentLoss = new MutableInt(0);
        final MutableInt currentTie = new MutableInt(0);
        final StringBuilder b = new StringBuilder(500);
        for (int i = 0; i < crs.length; ++i) {
            final CompetitionResults h2 = crs[i];
            if (h2.result > 0) {
                currentWinner.increment();
            } else if (h2.result < 0) {
                currentLoss.increment();
            } else {
                currentTie.increment();
            }
            if (i >= min) {
                if (i >= max) {
                    break;
                }
                b.append("<tr><td>");
                b.append(h2.toString(player, currentWinner, currentLoss, currentTie));
                b.append("</td></tr");
            }
        }
        if (min > 0) {
            html.replace("%buttprev%", "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">");
            html.replace("%prev_bypass%", "_match?class=" + targetClassId + "&page=" + (page - 1));
        } else {
            html.replace("%buttprev%", "");
        }
        if (crs.length > max) {
            html.replace("%buttnext%", "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">");
            html.replace("%prev_bypass%", "_match?class=" + targetClassId + "&page=" + (page + 1));
        } else {
            html.replace("%buttnext%", "");
        }
        html.replace("%list%", b.toString());
        player.sendPacket(html);
    }

    public void loadDiary(final int charId) {
        final List<HeroDiary> diary = new ArrayList<>();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM  heroes_diary WHERE charId=? ORDER BY time ASC");
            statement.setInt(1, charId);
            rset = statement.executeQuery();
            while (rset.next()) {
                final long time = rset.getLong("time");
                final int action = rset.getInt("action");
                final int param = rset.getInt("param");
                final HeroDiary d = new HeroDiary(action, time, param);
                diary.add(d);
            }
            _herodiary.put(charId, diary);
            if (Config.DEBUG) {
                LOGGER.info("HeroManager: Loaded " + diary.size() + " diary entries for Hero(object id: #" + charId + ")");
            }
        } catch (SQLException e) {
            LOGGER.warn("HeroManager: Couldnt load Hero Diary for CharId: " + charId, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void showHeroDiary(final Player activeChar, final int heroclass, final int page) {
        final int perpage = 10;
        HeroRecord hr = null;
        for (final HeroRecord hr2 : getCurrentHeroes()) {
            if (hr2.active && hr2.played && hr2.class_id == heroclass) {
                hr = hr2;
            }
        }
        if (hr == null) {
            return;
        }
        final List<HeroDiary> mainlist = _herodiary.get(hr.char_id);
        if (mainlist != null) {
            final NpcHtmlMessage html = new NpcHtmlMessage(activeChar, null);
            html.setFile("olympiad/monument_hero_info.htm");
            html.replace("%title%", StringHolder.getInstance().getNotNull(activeChar, "hero.diary"));
            html.replace("%heroname%", hr.name);
            html.replace("%message%", hr.message);
            final List<HeroDiary> list = new ArrayList<>(mainlist);
            Collections.reverse(list);
            boolean color = true;
            final StringBuilder fList = new StringBuilder(500);
            int counter = 0;
            int breakat = 0;
            for (int i = (page - 1) * perpage; i < list.size(); ++i) {
                breakat = i;
                final HeroDiary diary = list.get(i);
                final Entry<String, String> entry = diary.toString(activeChar);
                fList.append("<tr><td>");
                if (color) {
                    fList.append("<table width=270 bgcolor=\"131210\">");
                } else {
                    fList.append("<table width=270>");
                }
                fList.append("<tr><td width=270><font color=\"LEVEL\">").append(entry.getKey()).append("</font></td></tr>");
                fList.append("<tr><td width=270>").append(entry.getValue()).append("</td></tr>");
                fList.append("<tr><td>&nbsp;</td></tr></table>");
                fList.append("</td></tr>");
                color = !color;
                if (++counter >= perpage) {
                    break;
                }
            }
            if (breakat < list.size() - 1) {
                html.replace("%buttprev%", "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">");
                html.replace("%prev_bypass%", "_diary?class=" + heroclass + "&page=" + (page + 1));
            } else {
                html.replace("%buttprev%", "");
            }
            if (page > 1) {
                html.replace("%buttnext%", "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">");
                html.replace("%next_bypass%", "_diary?class=" + heroclass + "&page=" + (page - 1));
            } else {
                html.replace("%buttnext%", "");
            }
            html.replace("%list%", fList.toString());
            activeChar.sendPacket(html);
        }
    }

    public void addHeroDiary(final int playerId, final int id, final int param) {
        insertHeroDiary(playerId, id, param);
        final List<HeroDiary> list = _herodiary.get(playerId);
        if (list != null) {
            list.add(new HeroDiary(id, System.currentTimeMillis(), param));
        }
    }

    private void insertHeroDiary(final int charId, final int action, final int param) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO heroes_diary (charId, time, action, param) values(?,?,?,?)");
            statement.setInt(1, charId);
            statement.setLong(2, System.currentTimeMillis());
            statement.setInt(3, action);
            statement.setInt(4, param);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            LOGGER.error("HeroManager: SQL exception while saving DiaryData.", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void setHeroMessage(final int charId, final String message) {
        HeroRecord hr = null;
        for (final HeroRecord hr2 : getCurrentHeroes()) {
            if (hr2.active && hr2.played && hr2.char_id == charId) {
                hr = hr2;
            }
        }
        hr.message = Strings.stripSlashes(message);
    }

    private static class LazyHolder {
        private static final HeroManager INSTANCE = new HeroManager();
    }

    public class HeroRecord {
        public final int char_id;
        public final int class_id;
        public final String name;
        public int count;
        public boolean active;
        public boolean played;
        public String message;
        public String clan_name;
        public String ally_name;
        public int clan_crest;
        public int ally_crest;
        public Collection<CompetitionResults> competitions;

        private HeroRecord(final int _char_id, final String _name, final int _class_id, final int _count, final boolean _active, final boolean _played, final String _message) {
            char_id = _char_id;
            name = _name;
            count = _count;
            class_id = _class_id;
            active = _active;
            played = _played;
            message = _message;
        }

        public Collection<CompetitionResults> getCompetitions() {
            if (competitions == null) {
                competitions = OlympiadGameManager.getInstance().getCompetitionResults(char_id, OlympiadSystemManager.getInstance().getCurrentSeason() - 1);
            }
            return competitions;
        }
    }
}
