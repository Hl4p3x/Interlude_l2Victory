package ru.j2dev.gameserver.model.entity.olympiad;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager.NobleRecord;
import ru.j2dev.gameserver.model.entity.olympiad.participants.SinglePlayerOlympiadPlayer;
import ru.j2dev.gameserver.model.entity.olympiad.participants.TeamOlympiadPlayer;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.TimeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

@HideAccess
@StringEncryption
public class OlympiadGameManager {
    private static final int COMPETITION_PAUSE = 30000;
    private static final int COMPETITION_PREPARATION_DELAY = 60;
    private static final int BACKPORT_DELAY = 20;
    private static final Logger LOGGER = LoggerFactory.getLogger(OlympiadGameManager.class);
    private static final String GET_COMP_RECORDS = "SELECT `oc`.`char_id` AS `char_obj_id`, `on1`.`char_name` AS `char_name`, `on1`.`class_id` AS `char_class_id`, `on2`.`char_id` AS `rival_obj_id`, `on2`.`char_name` AS `rival_name`, `on2`.`class_id` AS `rival_class_id`, `oc`.`result` AS `result`, `oc`.`rule` AS `rules`, `oc`.`elapsed_time` AS `elapsed_time`, `oc`.`mtime` AS `mtime` FROM `oly_comps` AS `oc` JOIN `oly_nobles` AS `on1` ON `oc`.`char_id` = `on1`.`char_id` JOIN `oly_nobles` AS `on2` ON `oc`.`rival_id` = `on2`.`char_id` WHERE `oc`.`char_id` = ? AND `oc`.`season` = ? ";
    private static final String ADD_COMP_RECORD = "INSERT INTO `oly_comps` (`season`, `char_id`, `rival_id`, `rule`, `result`, `elapsed_time`, `mtime`) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private final ConcurrentLinkedQueue<OlympiadGame> _activeOlympiadGames = new ConcurrentLinkedQueue<>();
    private ScheduledFuture<?> _start_task;
    private int _start_fail_trys;

    public static OlympiadGameManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public boolean isActiveCompetitionInPrgress() {
        return !_activeOlympiadGames.isEmpty();
    }

    public Collection<OlympiadGame> getCompetitions() {
        return _activeOlympiadGames;
    }

    private synchronized boolean TryCreateCompetitions(final OlympiadGameType type, final int cls_id) {
        if (!OlympiadStadiumManager.getInstance().isStadiumAvailable()) {
            LOGGER.warn("CompetitionManager: not enough stadiums.");
            return false;
        }
        if (!OlympiadPlayersManager.getInstance().isEnough(type, cls_id)) {
            OlympiadPlayersManager.getInstance().broadcastToEntrys(type, Msg.THE_MATCH_MAY_BE_DELAYED_DUE_TO_NOT_ENOUGH_COMBATANTS, cls_id);
            return false;
        }
        for (Player[][] participants = OlympiadPlayersManager.getInstance().retrieveEntrys(type, cls_id); OlympiadSystemManager.getInstance().isRegAllowed() && OlympiadStadiumManager.getInstance().isStadiumAvailable() && participants != null && participants[0] != null && participants[1] != null; participants = OlympiadPlayersManager.getInstance().retrieveEntrys(type, cls_id)) {
            final OlympiadStadium olympiadStadium = OlympiadStadiumManager.getInstance().pollStadium();
            if (olympiadStadium == null) {
                LOGGER.error("CompetitionManager: stadium == null wtf?");
                return false;
            }
            StartCompetition(type, olympiadStadium, participants[0], participants[1]);
        }
        return true;
    }

    private void StartCompetition(final OlympiadGameType type, final OlympiadStadium olympiadStadium, final Player[] p0, final Player[] p1) {
        final OlympiadGame comp = new OlympiadGame(type, olympiadStadium);
        if (type == OlympiadGameType.TEAM_CLASS_FREE) {
            comp.setPlayers(new OlympiadPlayer[]{new TeamOlympiadPlayer(OlympiadPlayer.SIDE_BLUE, comp, p0), new TeamOlympiadPlayer(OlympiadPlayer.SIDE_RED, comp, p1)});
        } else if (type == OlympiadGameType.CLASS_FREE || type == OlympiadGameType.CLASS_INDIVIDUAL) {
            comp.setPlayers(new OlympiadPlayer[]{new SinglePlayerOlympiadPlayer(OlympiadPlayer.SIDE_BLUE, comp, p0[0]), new SinglePlayerOlympiadPlayer(OlympiadPlayer.SIDE_RED, comp, p1[0])});
        }
        comp.scheduleTask(new StadiumTeleportTask(comp), 100L);
        comp.start();
        _activeOlympiadGames.add(comp);
    }

    public void FinishCompetition(final OlympiadGame comp) {
        if (comp == null) {
            return;
        }
        try {
            comp.finish();
            if (comp.getState() != OlympiadGameState.INIT) {
                comp.teleportParticipantsBack();
            }
            OlympiadStadiumManager.getInstance().putStadium(comp.getStadium());
            _activeOlympiadGames.remove(comp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean RunComps(final OlympiadGameType type) {
        if (type == OlympiadGameType.CLASS_INDIVIDUAL) {
            final boolean ret = false;
            Arrays.stream(ClassId.values()).filter(cid -> cid.level() == 3).forEach(cid -> TryCreateCompetitions(type, cid.getId()));
            return ret;
        }
        return TryCreateCompetitions(type, 0);
    }

    public void scheduleStartTask() {
        if (OlympiadSystemManager.getInstance().isRegAllowed()) {
            _start_task = ThreadPoolManager.getInstance().schedule(new CompetitionStarterTask(), Math.min(COMPETITION_PAUSE * (_start_fail_trys + 1), 60000));
        }
    }

    public void cancelStartTask() {
        if (_start_task != null) {
            _start_task.cancel(true);
            _start_task = null;
        }
    }

    public synchronized SystemMessage AddParticipationRequest(final OlympiadGameType type, final Player[] players) {
        if (!OlympiadSystemManager.getInstance().isRegAllowed()) {
            return Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS;
        }
        for (final Player noble : players) {
            if (!noble.isNoble()) {
                return new SystemMessage(1501).addName(noble);
            }
            if (noble.isInDuel()) {
                return new SystemMessage(1599);
            }
            if (noble.getBaseClassId() != noble.getClassId().getId() || noble.getClassId().getLevel() < 4) {
                return new SystemMessage(1500).addName(noble);
            }
            if (OlympiadPlayersManager.getInstance().isRegistred(noble)) {
                return new SystemMessage(1502).addName(noble);
            }
            if (noble.getInventoryLimit() * 0.8 <= noble.getInventory().getSize()) {
                return new SystemMessage(1691).addName(noble);
            }
            if (noble.isCursedWeaponEquipped()) {
                return new SystemMessage(1857).addName(noble).addItemName(noble.getCursedWeaponEquippedId());
            }
            if (NoblessManager.getInstance().getPointsOf(noble.getObjectId()) < 1) {
                return new SystemMessage(SystemMsg.S1).addString(new CustomMessage("THE_REQUEST_CANNOT_BE_COMPLETED_BECAUSE_THE_REQUIREMENTS_ARE_NOT_MET_IN_ORDER_TO_PARTICIPATE_IN", noble, new Object[0]).toString());
            }
            if (Config.OLY_RESTRICT_CLASS_IDS.length > 0 && ArrayUtils.contains(Config.OLY_RESTRICT_CLASS_IDS, noble.getActiveClassId())) {
                return new SystemMessage(SystemMsg.S1).addString(new CustomMessage("olympiad.restrictedclasses", noble, new Object[0]).toString());
            }
            if (Config.OLY_RESTRICT_HWID && noble.getNetConnection().getHwid() != null && OlympiadPlayersManager.getInstance().isHWIDRegistred(noble.getNetConnection().getHwid())) {
                return new SystemMessage(SystemMsg.S1).addString(new CustomMessage("olympiad.iphwid.check", noble, new Object[0]).toString());
            }
            if (Config.OLY_RESTRICT_IP && noble.getNetConnection().getIpAddr() != null && OlympiadPlayersManager.getInstance().isIPRegistred(noble.getNetConnection().getIpAddr())) {
                return new SystemMessage(SystemMsg.S1).addString(new CustomMessage("olympiad.iphwid.check", noble, new Object[0]).toString());
            }
        }
        OlympiadPlayersManager.getInstance().createEntry(type, players);
        switch (type) {
            case CLASS_INDIVIDUAL: {
                return Msg.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES;
            }
            case CLASS_FREE: {
                return Msg.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES;
            }
            case TEAM_CLASS_FREE: {
                return Msg.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES;
            }
            default: {
                return null;
            }
        }
    }

    public void scheduleFinishCompetition(final OlympiadGame comp, final int count_down, final long delay) {
        comp.scheduleTask(new FinishCompetitionTask(comp, count_down), delay);
    }

    public void scheduleCompetitionPreparation(final OlympiadGame comp) {
        comp.scheduleTask(new CompetitionPreparationTask(comp), 1000L);
    }

    public void addCompetitionResult(final int season, final NobleRecord winner, final int win_points, final NobleRecord looser, final int loose_points, final OlympiadGameType type, final boolean tie, final boolean disconn, final long elapsed_time) {
        if (winner == null || looser == null || type == null) {
            return;
        }
        if (disconn) {
            Log.add(String.format("CompetitionResult: %s(%d) - %d disconnected against %s(%d) in %s", looser.char_name, looser.char_id, loose_points, winner.char_name, winner.char_id, type.name()), "olympiad");
        } else if (!tie) {
            Log.add(String.format("CompetitionResult: %s(%d) + %d win against %s(%d) - %d in %s", winner.char_name, winner.char_id, win_points, looser.char_name, looser.char_id, loose_points, type.name()), "olympiad");
        } else {
            Log.add(String.format("CompetitionResult: %s(%d) tie against %s(%d) in %s", winner.char_name, winner.char_id, looser.char_name, looser.char_id, type.name()), "olympiad");
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement(ADD_COMP_RECORD);
            if (!disconn) {
                pstmt.setInt(1, season);
                pstmt.setInt(2, winner.char_id);
                pstmt.setInt(3, looser.char_id);
                pstmt.setInt(4, type.getTypeIdx());
                pstmt.setByte(5, (byte) (tie ? 0 : 1));
                pstmt.setInt(6, (int) elapsed_time);
                pstmt.setInt(7, (int) (System.currentTimeMillis() / 1000L));
                pstmt.executeUpdate();
            }
            pstmt.setInt(1, season);
            pstmt.setInt(2, looser.char_id);
            pstmt.setInt(3, winner.char_id);
            pstmt.setInt(4, type.getTypeIdx());
            pstmt.setByte(5, (byte) (tie ? 0 : -1));
            pstmt.setInt(6, (int) elapsed_time);
            pstmt.setInt(7, (int) (System.currentTimeMillis() / 1000L));
            pstmt.executeUpdate();
        } catch (Exception ex) {
            LOGGER.warn("CompetitionManager: Can't save competition result", ex);
        } finally {
            DbUtils.closeQuietly(conn, pstmt);
        }
    }

    public Collection<CompetitionResults> getCompetitionResults(final int obj_id, final int season) {
        final ArrayList<CompetitionResults> result = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement(GET_COMP_RECORDS);
            pstmt.setInt(1, obj_id);
            pstmt.setInt(2, season);
            rset = pstmt.executeQuery();
            while (rset.next()) {
                result.add(new CompetitionResults(rset.getInt("char_obj_id"), rset.getInt("char_class_id"), rset.getString("char_name"), rset.getInt("rival_obj_id"), rset.getInt("rival_class_id"), rset.getString("rival_name"), OlympiadGameType.getTypeOf(rset.getByte("rules")), rset.getByte("result"), rset.getInt("elapsed_time"), rset.getLong("mtime")));
            }
        } catch (Exception ex) {
            LOGGER.warn("CompetitionManager: Can't load competitions records", ex);
        } finally {
            DbUtils.closeQuietly(conn, pstmt, rset);
        }
        return result;
    }

    public void showCompetitionList(final Player player) {
        if (!OlympiadSystemManager.getInstance().isRegAllowed()) {
            player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
            return;
        }
        if (player.isOlyParticipant() || OlympiadPlayersManager.getInstance().isRegistred(player)) {
            player.sendPacket(Msg.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
            return;
        }
        final StringBuilder sb = new StringBuilder();
        for (final OlympiadStadium olympiadStadium : OlympiadStadiumManager.getInstance().getAllStadiums()) {
            sb.append("<a action=\"bypass -h _olympiad?command=move_op_field&field=").append(olympiadStadium.getStadiumId() + 1).append("\">");
            sb.append(new CustomMessage("Olympiad.CompetitionState.ARENA", player)).append(olympiadStadium.getStadiumId() + 1);
            sb.append("&nbsp;&nbsp;&nbsp;");
            boolean isEmpty = true;
            for (final OlympiadGame comp : _activeOlympiadGames) {
                if (comp.getStadium() == olympiadStadium && comp.getState() != OlympiadGameState.INIT) {
                    sb.append(comp._olympiadPlayers[0].getName()).append(" : ").append(comp._olympiadPlayers[1].getName());
                    sb.append("&nbsp;");
                    switch (comp.getState()) {
                        case STAND_BY: {
                            sb.append(new CustomMessage("Olympiad.CompetitionState.STAND_BY", player));
                            break;
                        }
                        case PLAYING: {
                            sb.append(new CustomMessage("Olympiad.CompetitionState.PLAYING", player));
                            break;
                        }
                        case FINISH: {
                            sb.append(new CustomMessage("Olympiad.CompetitionState.FINISH", player));
                            break;
                        }
                    }
                    isEmpty = false;
                }
            }
            if (isEmpty) {
                sb.append(new CustomMessage("Olympiad.CompetitionState.EMPTY", player));
            }
            sb.append("</a><br>");
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, null);
        html.setFile("olympiad/arenas.htm");
        html.replace("%arenas%", sb.toString());
        player.sendPacket(html);
    }

    public void watchCompetition(final Player player, final int stadium_id) {
        if (!OlympiadSystemManager.getInstance().isRegAllowed()) {
            player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
            return;
        }
        if (player.getPet() != null || player.isMounted()) {
            return;
        }
        if (player.isInStoreMode()) {
            return;
        }
        if (stadium_id < 1 || stadium_id > 22) {
            return;
        }
        if (player.isOlyParticipant() || OlympiadPlayersManager.getInstance().isRegistred(player)) {
            player.sendPacket(Msg.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
            return;
        }
        final OlympiadStadium olympiadStadium = OlympiadStadiumManager.getInstance().getStadium(stadium_id - 1);
        if (olympiadStadium.getObserverCount() > Config.OLY_MAX_SPECTATORS_PER_STADIUM) {
            player.sendMessage("To many observers on this stadium.");
            return;
        }
        if (player.isOlyObserver()) {
            player.switchOlympiadObserverArena(olympiadStadium);
        } else {
            player.enterOlympiadObserverMode(olympiadStadium);
        }
    }

    private static class LazyHolder {
        private static final OlympiadGameManager INSTANCE = new OlympiadGameManager();
    }

    private class CompetitionStarterTask implements Runnable {
        @Override
        public void run() {
            try {
                if (!OlympiadSystemManager.getInstance().isRegAllowed()) {
                    return;
                }
                Arrays.stream(OlympiadGameType.values()).forEach(type -> {
                    if (RunComps(type)) {
                        getInstance()._start_fail_trys = 0;
                    } else if (getInstance()._start_fail_trys < 5) {
                        getInstance()._start_fail_trys++;
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                getInstance().scheduleStartTask();
            }
        }
    }

    public class StadiumTeleportTask implements Runnable {
        private final OlympiadGame _game;
        private int _countdown;

        public StadiumTeleportTask(final OlympiadGame game) {
            this(game, Config.OLYMPIAD_STADIUM_TELEPORT_DELAY);
        }

        public StadiumTeleportTask(final OlympiadGame game, final int countdown) {
            _game = game;
            _countdown = countdown;
            if (_game.getState() == null) {
                _game.setState(OlympiadGameState.INIT);
            }
        }

        @Override
        public void run() {
            if (_countdown > 0) {
                _game.broadcastPacket(new SystemMessage(1492).addNumber(_countdown), true, false);
                long delay = 1000L;
                switch (_countdown) {
                    case 30:
                    case 45: {
                        _countdown -= 15;
                        delay = 15000L;
                        break;
                    }
                    case 15: {
                        _countdown = 5;
                        delay = 5000L;
                        break;
                    }
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5: {
                        --_countdown;
                        delay = 1000L;
                        break;
                    }
                }
                if (_game.ValidateParticipants()) {
                    return;
                }
                _game.scheduleTask(new StadiumTeleportTask(_game, _countdown), (_countdown > 0) ? delay : 1000L);
            } else {
                if (_game.ValidateParticipants()) {
                    return;
                }
                _game.getStadium().setZonesActive(false);
                _game.teleportParticipantsOnStadium();
                _game.setState(OlympiadGameState.STAND_BY);
                getInstance().scheduleCompetitionPreparation(_game);
                if (Config.NPC_OLYMPIAD_GAME_ANNOUNCE) {
                    OlympiadSystemManager.getInstance().announceCompetition(_game.getType(), _game.getStadium().getStadiumId());
                }
            }
        }
    }

    public class CompetitionPreparationTask implements Runnable {
        private final OlympiadGame _game;
        private int _countdown;

        CompetitionPreparationTask(final OlympiadGame game) {
            this(game, COMPETITION_PREPARATION_DELAY);
        }

        CompetitionPreparationTask(final OlympiadGame game, final int countdown) {
            _game = game;
            _countdown = countdown;
        }

        @Override
        public void run() {
            if (_countdown > 0) {
                if (_countdown < 10 || _countdown % 10 == 0) {
                    _game.broadcastPacket(new SystemMessage(1495).addNumber(_countdown), true, true);
                }
                long delay = 1000L;
                switch (_countdown) {
                    case 55:
                    case 60: {
                        _countdown -= 5;
                        delay = 5000L;
                        if (Config.OLY_BROADCAST_CLASS_ID) {
                            _game.broadcastClassId();
                        }
                        break;
                    }
                    case 20:
                    case 30:
                    case 40:
                    case 50: {
                        _countdown -= 10;
                        delay = 10000L;
                        break;
                    }
                    case 10: {
                        _countdown -= 5;
                        delay = 5000L;
                        break;
                    }
                    case 5: {
                        _game.applyBuffs();
                    }
                    case 1:
                    case 2:
                    case 3:
                    case 4: {
                        _countdown--;
                        delay = 1000L;
                        break;
                    }
                }
                _game.scheduleTask(new CompetitionPreparationTask(_game, _countdown), (_countdown > 0) ? delay : 2000L);
            } else {
                _game.getStadium().setZonesActive(true);
                _game.restoreHPCPMP();
                _game.broadcastEverybodyOlympiadUserInfo();
                _game.broadcastEverybodyEffectIcons();
                _game.broadcastPacket(new PlaySound("ns17_f"), true, true);
                _game.broadcastPacket(Msg.STARTS_THE_GAME, true, true);
                _game.setState(OlympiadGameState.PLAYING);
                getInstance().scheduleFinishCompetition(_game, -1, Config.OLYMPIAD_COMPETITION_TIME);
            }
        }
    }

    public class FinishCompetitionTask implements Runnable {
        private final OlympiadGame _game;
        private int _countdown;

        FinishCompetitionTask(final OlympiadGame game, final int countdown) {
            _game = game;
            _countdown = countdown;
        }

        @Override
        public void run() {
            if (_game.getState() != OlympiadGameState.FINISH) {
                _game.setState(OlympiadGameState.FINISH);
                _game.ValidateWinner();
                _game.scheduleTask(new FinishCompetitionTask(_game, BACKPORT_DELAY), 100L);
            } else if (_game.getState() == OlympiadGameState.FINISH) {
                if (_countdown > 0) {
                    _game.broadcastPacket(new SystemMessage(1499).addNumber(_countdown), true, false);
                    final int dur = (_countdown > 5) ? (_countdown / 2) : 1;
                    _countdown -= dur;
                    _game.scheduleTask(new FinishCompetitionTask(_game, _countdown), dur * 1000);
                } else {
                    getInstance().FinishCompetition(_game);
                }
            }
        }
    }

    public class CompetitionResults {
        final int char_id;
        final int rival_id;
        final String char_name;
        final String rival_name;
        final byte result;
        final int char_class_id;
        final int rival_class_id;
        final int elapsed_time;
        final OlympiadGameType type;
        final long mtime;

        private CompetitionResults(final int _wid, final int _wcid, final String _wn, final int _lid, final int _lcid, final String _ln, final OlympiadGameType _type, final byte _r, final int _et, final long _mtime) {
            char_id = _wid;
            char_class_id = _wcid;
            char_name = _wn;
            rival_id = _lid;
            rival_name = _ln;
            rival_class_id = _lcid;
            type = _type;
            result = _r;
            elapsed_time = _et;
            mtime = _mtime;
        }

        public String toString(final Player player, final MutableInt wins, final MutableInt looses, final MutableInt ties) {
            String main = null;
            if (result == 0) {
                main = StringHolder.getInstance().getNotNull(player, "hero.history.tie");
            } else if (result > 0) {
                main = StringHolder.getInstance().getNotNull(player, "hero.history.win");
            } else if (result < 2) {
                main = StringHolder.getInstance().getNotNull(player, "hero.history.loss");
            }
            if (result > 0) {
                wins.increment();
            } else if (result == 0) {
                ties.increment();
            } else if (result < 0) {
                looses.increment();
            }
            main = main.replace("%classId%", String.valueOf(rival_class_id));
            main = main.replace("%name%", rival_name);
            main = main.replace("%date%", TimeUtils.toHeroRecordFormat(mtime));
            main = main.replace("%time%", String.format("%02d:%02d", elapsed_time / COMPETITION_PREPARATION_DELAY, elapsed_time % COMPETITION_PREPARATION_DELAY));
            main = main.replace("%victory_count%", wins.toString());
            main = main.replace("%tie_count%", ties.toString());
            main = main.replace("%loss_count%", looses.toString());
            return main;
        }
    }
}
