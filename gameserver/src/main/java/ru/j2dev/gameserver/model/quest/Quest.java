package ru.j2dev.gameserver.model.quest;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.logging.LogUtils;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.TroveUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadGameType;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExQuestNpcLogList;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PtsUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Quest implements OnInitScriptListener {
    public static final String SOUND_ITEMGET = "ItemSound.quest_itemget";
    public static final String SOUND_ACCEPT = "ItemSound.quest_accept";
    public static final String SOUND_MIDDLE = "ItemSound.quest_middle";
    public static final String SOUND_FINISH = "ItemSound.quest_finish";
    public static final String SOUND_GIVEUP = "ItemSound.quest_giveup";
    public static final String SOUND_TUTORIAL = "ItemSound.quest_tutorial";
    public static final String SOUND_JACKPOT = "ItemSound.quest_jackpot";
    public static final String SOUND_LIQUID_MIX_01 = "SkillSound5.liquid_mix_01";
    public static final String SOUND_LIQUID_SUCCESS_01 = "SkillSound5.liquid_success_01";
    public static final String SOUND_LIQUID_FAIL_01 = "SkillSound5.liquid_fail_01";
    public static final String SOUND_BEFORE_BATTLE = "Itemsound.quest_before_battle";
    public static final String SOUND_FANFARE_MIDDLE = "ItemSound.quest_fanfare_middle";
    public static final String SOUND_FANFARE2 = "ItemSound.quest_fanfare_2";
    public static final String SOUND_BROKEN_KEY = "ItemSound2.broken_key";
    public static final String SOUND_ENCHANT_SUCESS = "ItemSound3.sys_enchant_sucess";
    public static final String SOUND_ENCHANT_FAILED = "ItemSound3.sys_enchant_failed";
    public static final String SOUND_ED_CHIMES05 = "AmdSound.ed_chimes_05";
    public static final String SOUND_ED_DRONE_02 = "AmbSound.ed_drone_02";
    public static final String SOUND_CD_CRYSTAL_LOOP = "AmbSound.cd_crystal_loop";
    public static final String SOUND_DT_PERCUSSION_01 = "AmbSound.dt_percussion_01";
    public static final String SOUND_AC_PERCUSSION_02 = "AmbSound.ac_percussion_02";
    public static final String SOUND_ARMOR_WOOD_3 = "ItemSound.armor_wood_3";
    public static final String SOUND_ITEM_DROP_EQUIP_ARMOR_CLOTH = "ItemSound.item_drop_equip_armor_cloth";
    public static final String SOUND_MT_CREAK01 = "AmbSound.mt_creak01";
    public static final String SOUND_D_WIND_LOOT_02 = "AmdSound.d_wind_loot_02";
    public static final String SOUND_CHARSTAT_OPEN_01 = "InterfaceSound.charstat_open_01";
    public static final String SOUND_DD_HORROR_01 = "AmbSound.dd_horror_01";
    public static final String SOUND_HORROR1 = "SkillSound5.horror_01";
    public static final String SOUND_HORROR2 = "SkillSound5.horror_02";
    public static final String SOUND_ELCROKI_SONG_FULL = "EtcSound.elcroki_song_full";
    public static final String SOUND_ELCROKI_SONG_1ST = "EtcSound.elcroki_song_1st";
    public static final String SOUND_ELCROKI_SONG_2ND = "EtcSound.elcroki_song_2nd";
    public static final String SOUND_ELCROKI_SONG_3RD = "EtcSound.elcroki_song_3rd";
    public static final String SOUND_ITEMDROP_ARMOR_LEATHER = "ItemSound.itemdrop_armor_leather";
    public static final String SOUND_EG_DRON_02 = "AmbSound.eg_dron_02";
    public static final String SOUND_MHFIGHTER_CRY = "ChrSound.MHFighter_cry";
    public static final String SOUND_ITEMDROP_WEAPON_SPEAR = "ItemSound.itemdrop_weapon_spear";
    public static final String SOUND_FDELF_CRY = "ChrSound.FDElf_Cry";
    public static final String SOUND_DD_HORROR_02 = "AmdSound.dd_horror_02";
    public static final String SOUND_D_HORROR_03 = "AmbSound.d_horror_03";
    public static final String SOUND_D_HORROR_15 = "AmbSound.d_horror_15";
    public static final String SOUND_ANTARAS_FEAR = "SkillSound3.antaras_fear";
    public static final String NO_QUEST_DIALOG = "no-quest";
    public static final int ADENA_ID = 57;
    public static final int PARTY_NONE = 0;
    public static final int PARTY_ONE = 1;
    public static final int PARTY_ALL = 2;
    public static final int CREATED = 1;
    public static final int STARTED = 2;
    public static final int COMPLETED = 3;
    public static final int DELAYED = 4;
    private static final Logger LOGGER = LoggerFactory.getLogger(Quest.class);

    protected final String _name;
    protected final int _party;
    protected final int _questId;
    private final Map<Integer, Map<String, QuestTimer>> _pausedQuestTimers;
    private final TIntHashSet _questItems;
    protected String _descr;
    private TIntObjectHashMap<List<QuestNpcLogInfo>> _npcLogList;

    public Quest(final boolean party) {
        this(party ? 1 : 0);
    }

    public Quest(final int party) {
        _pausedQuestTimers = new ConcurrentHashMap<>();
        _questItems = new TIntHashSet();
        _npcLogList = TroveUtils.emptyIntObjectMap();
        _name = getClass().getSimpleName();
        _questId = Integer.parseInt(_name.split("_")[1]);
        _party = party;
    }

    public static void updateQuestInDb(final QuestState qs) {
        updateQuestVarInDb(qs, "<state>", qs.getStateName());
    }

    public static void updateQuestVarInDb(final QuestState qs, final String var, final String value) {
        final Player player = qs.getPlayer();
        if (player == null) {
            return;
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("REPLACE INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)");
            statement.setInt(1, qs.getPlayer().getObjectId());
            statement.setString(2, qs.getQuest().getName());
            statement.setString(3, var);
            statement.setString(4, value);
            statement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("could not insert char quest:", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public static void deleteQuestInDb(final QuestState qs) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=?");
            statement.setInt(1, qs.getPlayer().getObjectId());
            statement.setString(2, qs.getQuest().getName());
            statement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("could not delete char quest:", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public static void deleteQuestVarInDb(final QuestState qs, final String var) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=? AND var=?");
            statement.setInt(1, qs.getPlayer().getObjectId());
            statement.setString(2, qs.getQuest().getName());
            statement.setString(3, var);
            statement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("could not delete char quest:", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public static void restoreQuestStates(final Player player) {
        Connection con = null;
        PreparedStatement statement = null;
        PreparedStatement invalidQuestData = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? and name=?");
            statement = con.prepareStatement("SELECT name,value FROM character_quests WHERE char_id=? AND var=?");
            statement.setInt(1, player.getObjectId());
            statement.setString(2, "<state>");
            rset = statement.executeQuery();
            while (rset.next()) {
                final String questId = rset.getString("name");
                final String state = rset.getString("value");
                if ("Start".equalsIgnoreCase(state)) {
                    invalidQuestData.setInt(1, player.getObjectId());
                    invalidQuestData.setString(2, questId);
                    invalidQuestData.executeUpdate();
                } else {
                    final Quest q = QuestManager.getQuest(questId);
                    if (q == null) {
                        if (Config.DONTLOADQUEST) {
                            continue;
                        }
                        LOGGER.warn("Unknown quest " + questId + " for player " + player.toString());
                    } else {
                        new QuestState(q, player, getStateId(state));
                    }
                }
            }
            DbUtils.close(statement, rset);
            statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE char_id=?");
            statement.setInt(1, player.getObjectId());
            rset = statement.executeQuery();
            while (rset.next()) {
                final String questId = rset.getString("name");
                final String var = rset.getString("var");
                String value = rset.getString("value");
                final QuestState qs = player.getQuestState(questId);
                if (qs == null) {
                    continue;
                }
                if ("cond".equals(var) && Integer.parseInt(value) < 0) {
                    value = String.valueOf(Integer.parseInt(value) | 0x1);
                }
                qs.set(var, value, false);
            }
        } catch (Exception e) {
            LOGGER.error("could not insert char quest:", e);
        } finally {
            DbUtils.closeQuietly(invalidQuestData);
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public static String getStateName(final int state) {
        switch (state) {
            case 1: {
                return "Start";
            }
            case 2: {
                return "Started";
            }
            case 3: {
                return "Completed";
            }
            case 4: {
                return "Delayed";
            }
            default: {
                return "Start";
            }
        }
    }

    public static int getStateId(final String state) {
        if ("Start".equalsIgnoreCase(state)) {
            return 1;
        }
        if ("Started".equalsIgnoreCase(state)) {
            return 2;
        }
        if ("Completed".equalsIgnoreCase(state)) {
            return 3;
        }
        if ("Delayed".equalsIgnoreCase(state)) {
            return 4;
        }
        return 1;
    }

    public static NpcInstance addSpawnToInstance(final int npcId, final int x, final int y, final int z, final int heading, final int randomOffset, final int refId) {
        return addSpawnToInstance(npcId, new Location(x, y, z, heading), randomOffset, refId);
    }

    public static NpcInstance addSpawnToInstance(final int npcId, final Location loc, final int randomOffset, final int refId) {
        try {
            final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(npcId);
            if (template != null) {
                final NpcInstance npc = Objects.requireNonNull(NpcTemplateHolder.getInstance().getTemplate(npcId)).getNewInstance();
                npc.setReflection(refId);
                npc.setSpawnedLoc((randomOffset > 50) ? Location.findPointToStay(loc, 50, randomOffset, npc.getGeoIndex()) : loc);
                npc.spawnMe(npc.getSpawnedLoc());
                return npc;
            }
        } catch (Exception e1) {
            LOGGER.warn("Could not spawn Npc " + npcId);
        }
        return null;
    }

    public void addQuestItem(final int... ids) {
        for (final int id : ids) {
            if (id != 0) {
                ItemTemplate i;
                i = ItemTemplateHolder.getInstance().getTemplate(id);
                if (_questItems.contains(id)) {
                    LOGGER.warn("Item " + i + " multiple times in quest drop in " + getName());
                }
                _questItems.add(id);
            }
        }
    }

    public int[] getItems() {
        return _questItems.toArray();
    }

    public boolean isQuestItem(final int id) {
        return _questItems.contains(id);
    }

    public List<QuestNpcLogInfo> getNpcLogList(final int cond) {
        return _npcLogList.get(cond);
    }

    public void addAttackId(final int... attackIds) {
        for (final int attackId : attackIds) {
            addEventId(attackId, QuestEventType.ATTACKED_WITH_QUEST);
        }
    }

    public NpcTemplate addEventId(final int npcId, final QuestEventType eventType) {
        try {
            final NpcTemplate t = NpcTemplateHolder.getInstance().getTemplate(npcId);
            if (t != null) {
                t.addQuestEvent(eventType, this);
            }
            return t;
        } catch (Exception e) {
            LOGGER.error("", e);
            return null;
        }
    }

    public void addKillId(final int... killIds) {
        for (final int killid : killIds) {
            addEventId(killid, QuestEventType.MOB_KILLED_WITH_QUEST);
        }
    }

    public void addKillNpcWithLog(final int cond, final String varName, final int max, final int... killIds) {
        if (killIds.length == 0) {
            throw new IllegalArgumentException("Npc list cant be empty!");
        }
        addKillId(killIds);
        if (_npcLogList.isEmpty()) {
            _npcLogList = new TIntObjectHashMap<>(5);
        }
        List<QuestNpcLogInfo> vars = _npcLogList.get(cond);
        if (vars == null) {
            _npcLogList.put(cond, vars = new ArrayList<>(5));
        }
        vars.add(new QuestNpcLogInfo(killIds, varName, max));
    }

    public boolean updateKill(final NpcInstance npc, final QuestState st) {
        final Player player = st.getPlayer();
        if (player == null) {
            return false;
        }
        final List<QuestNpcLogInfo> vars = getNpcLogList(st.getCond());
        if (vars == null) {
            return false;
        }
        boolean done = true;
        boolean find = false;
        for (final QuestNpcLogInfo info : vars) {
            int count = st.getInt(info.getVarName());
            if (!find && ArrayUtils.contains(info.getNpcIds(), npc.getNpcId())) {
                find = true;
                if (count < info.getMaxCount()) {
                    st.set(info.getVarName(), ++count);
                    player.sendPacket(new ExQuestNpcLogList(st));
                }
            }
            if (count != info.getMaxCount()) {
                done = false;
            }
        }
        return done;
    }

    public void addKillId(final Collection<Integer> killIds) {
        killIds.forEach(this::addKillId);
    }

    public NpcTemplate addSkillUseId(final int npcId) {
        return addEventId(npcId, QuestEventType.MOB_TARGETED_BY_SKILL);
    }

    public void addStartNpc(final int... npcIds) {
        for (final int talkId : npcIds) {
            addStartNpc(talkId);
        }
    }

    public NpcTemplate addStartNpc(final int npcId) {
        addTalkId(npcId);
        return addEventId(npcId, QuestEventType.QUEST_START);
    }

    public void addFirstTalkId(final int... npcIds) {
        for (final int npcId : npcIds) {
            addEventId(npcId, QuestEventType.NPC_FIRST_TALK);
        }
    }

    public void addTalkId(final int... talkIds) {
        for (final int talkId : talkIds) {
            addEventId(talkId, QuestEventType.QUEST_TALK);
        }
    }

    public void addTalkId(final Collection<Integer> talkIds) {
        talkIds.forEach(this::addTalkId);
    }


    public String getDescr(final Player player) {
        if (!isVisible()) {
            return null;
        }

        final QuestState qs = player.getQuestState(_name);
        int state = 2;
        if (qs == null || qs.isCreated() && qs.isNowAvailable()) {
            state = 1;
        } else if (qs.isCompleted() || !qs.isNowAvailable()) {
            state = 3;
        }

        int fStringId = _questId;
        if (fStringId >= 10000) {
            fStringId -= 5000;
        }
        fStringId = fStringId * 100 + state;
        return PtsUtils.MakeFString(player, fStringId, "", "", "", "", "");
    }

    public String getName() {
        return _name;
    }

    public int getQuestIntId() {
        return _questId;
    }

    public int getParty() {
        return _party;
    }

    public QuestState newQuestState(final Player player, final int state) {
        final QuestState qs = new QuestState(this, player, state);
        updateQuestInDb(qs);
        return qs;
    }

    public QuestState newQuestStateAndNotSave(final Player player, final int state) {
        return new QuestState(this, player, state);
    }

    public void notifyAttack(final NpcInstance npc, final QuestState qs) {
        String res;
        try {
            res = onAttack(npc, qs);
        } catch (Exception e) {
            showError(qs.getPlayer(), e);
            return;
        }
        showResult(npc, qs.getPlayer(), res);
    }

    public void notifyDeath(final Creature killer, final Creature victim, final QuestState qs) {
        String res;
        try {
            res = onDeath(killer, victim, qs);
        } catch (Exception e) {
            showError(qs.getPlayer(), e);
            return;
        }
        showResult(null, qs.getPlayer(), res);
    }

    public void notifyEvent(final String event, final QuestState qs, final NpcInstance npc) {
        String res;
        try {
            res = onEvent(event, qs, npc);
        } catch (Exception e) {
            showError(qs.getPlayer(), e);
            return;
        }
        showResult(npc, qs.getPlayer(), res);
    }

    public void notifyKill(final NpcInstance npc, final QuestState qs) {
        String res;
        try {
            res = onKill(npc, qs);
        } catch (Exception e) {
            showError(qs.getPlayer(), e);
            return;
        }
        showResult(npc, qs.getPlayer(), res);
    }

    public void notifyKill(final Player target, final QuestState qs) {
        String res;
        try {
            res = onKill(target, qs);
        } catch (Exception e) {
            showError(qs.getPlayer(), e);
            return;
        }
        showResult(null, qs.getPlayer(), res);
    }

    public final boolean notifyFirstTalk(final NpcInstance npc, final Player player) {
        String res;
        try {
            res = onFirstTalk(npc, player);
        } catch (Exception e) {
            showError(player, e);
            return true;
        }
        return showResult(npc, player, res, true);
    }

    public boolean notifyTalk(final NpcInstance npc, final QuestState qs) {
        String res;
        try {
            res = onTalk(npc, qs);
        } catch (Exception e) {
            showError(qs.getPlayer(), e);
            return true;
        }
        return showResult(npc, qs.getPlayer(), res);
    }

    public boolean notifySkillUse(final NpcInstance npc, final Skill skill, final QuestState qs) {
        String res;
        try {
            res = onSkillUse(npc, skill, qs);
        } catch (Exception e) {
            showError(qs.getPlayer(), e);
            return true;
        }
        return showResult(npc, qs.getPlayer(), res);
    }

    public void notifyCreate(final QuestState qs) {
        try {
            onCreate(qs);
        } catch (Exception e) {
            showError(qs.getPlayer(), e);
        }
    }

    public void notifyOlympiadResult(final QuestState qs, final OlympiadGameType type, final boolean isWin) {
        try {
            onOlympiadResult(qs, type, isWin);
        } catch (Exception e) {
            showError(qs.getPlayer(), e);
        }
    }

    public void onOlympiadResult(final QuestState qs, final OlympiadGameType type, final boolean isWin) {
    }

    public void onCreate(final QuestState qs) {
    }

    public String onAttack(final NpcInstance npc, final QuestState qs) {
        return null;
    }

    public String onDeath(final Creature killer, final Creature victim, final QuestState qs) {
        return null;
    }

    public String onEvent(final String event, final QuestState qs, final NpcInstance npc) {
        return null;
    }

    public String onKill(final NpcInstance npc, final QuestState qs) {
        return null;
    }

    public String onKill(final Player killed, final QuestState st) {
        return null;
    }

    public String onKill(final NpcInstance npc,Player killer, final QuestState st) {
        return null;
    }

    public String onFirstTalk(final NpcInstance npc, final Player player) {
        return null;
    }

    public String onTalk(final NpcInstance npc, final QuestState qs) {
        return null;
    }

    public String onSkillUse(final NpcInstance npc, final Skill skill, final QuestState qs) {
        return null;
    }

    public void onAbort(final QuestState qs) {
    }

    public boolean canAbortByPacket() {
        return true;
    }

    private void showError(final Player player, final Throwable t) {
        LOGGER.error("", t);
        if (player != null && player.isGM()) {
            final String res = "<html><body><title>Script error</title>" + LogUtils.dumpStack(t).replace("\n", "<br>") + "</body></html>";
            showResult(null, player, res);
        }
    }

    protected void showHtmlFile(final Player player, final String fileName, final boolean showQuestInfo) {
        showHtmlFile(player, fileName, showQuestInfo, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    protected void showHtmlFile(final Player player, final String fileName, final boolean showQuestInfo, final Object... arg) {
        if (player == null) {
            return;
        }
        final GameObject target = player.getTarget();
        final NpcHtmlMessage npcReply = new NpcHtmlMessage((target == null) ? 5 : target.getObjectId());
        npcReply.setFile("quests/" + getClass().getSimpleName() + "/" + fileName);
        if (arg.length % 2 == 0) {
            for (int i = 0; i < arg.length; i += 2) {
                npcReply.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
            }
        }
        player.sendPacket(npcReply);
    }

    protected void showSimpleHtmFile(final Player player, final String fileName) {
        if (player == null) {
            return;
        }
        final NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
        npcReply.setFile(fileName);
        player.sendPacket(npcReply);
    }

    private boolean showResult(final NpcInstance npc, final Player player, final String res) {
        return showResult(npc, player, res, false);
    }

    private boolean showResult(final NpcInstance npc, final Player player, final String res, final boolean isFirstTalk) {
        boolean showQuestInfo = showQuestInfo(player);
        if (isFirstTalk) {
            showQuestInfo = false;
        }
        if (res == null) {
            return true;
        }
        if (res.isEmpty()) {
            return false;
        }
        if (res.startsWith("no_quest") || "noquest".equalsIgnoreCase(res) || "no-quest".equalsIgnoreCase(res)) {
            showSimpleHtmFile(player, "no-quest.htm");
        } else if ("completed".equalsIgnoreCase(res)) {
            showSimpleHtmFile(player, "completed-quest.htm");
        } else if (res.endsWith(".htm")) {
            showHtmlFile(player, res, showQuestInfo);
        } else {
            final NpcHtmlMessage npcReply = new NpcHtmlMessage((npc == null) ? 5 : npc.getObjectId());
            npcReply.setHtml(res);
            player.sendPacket(npcReply);
        }
        return true;
    }

    private boolean showQuestInfo(final Player player) {
        final QuestState qs = player.getQuestState(getName());
        return (qs == null || qs.getState() == 1) && isVisible();
    }

    void pauseQuestTimers(final QuestState qs) {
        if (qs.getTimers().isEmpty()) {
            return;
        }
        for (final QuestTimer timer : qs.getTimers().values()) {
            timer.setQuestState(null);
            timer.pause();
        }
        _pausedQuestTimers.put(qs.getPlayer().getObjectId(), qs.getTimers());
    }

    void resumeQuestTimers(final QuestState qs) {
        final Map<String, QuestTimer> timers = _pausedQuestTimers.remove(qs.getPlayer().getObjectId());
        if (timers == null) {
            return;
        }
        qs.getTimers().putAll(timers);
        for (final QuestTimer timer : qs.getTimers().values()) {
            timer.setQuestState(qs);
            timer.start();
        }
    }

    protected String str(final long i) {
        return String.valueOf(i);
    }

    public NpcInstance addSpawn(final int npcId, final int x, final int y, final int z, final int heading, final int randomOffset, final int despawnDelay) {
        return addSpawn(npcId, new Location(x, y, z, heading), randomOffset, despawnDelay);
    }

    public NpcInstance addSpawn(final int npcId, final Location loc, final int randomOffset, final int despawnDelay) {
        final NpcInstance result = Functions.spawn((randomOffset > 50) ? Location.findPointToStay(loc, 0, randomOffset, ReflectionManager.DEFAULT.getGeoIndex()) : loc, npcId);
        if (despawnDelay > 0 && result != null) {
            ThreadPoolManager.getInstance().schedule(new DeSpawnScheduleTimerTask(result), despawnDelay);
        }
        return result;
    }

    public boolean isVisible() {
        return true;
    }

    public QuestRates getRates() {
        return Config.QUEST_RATES.computeIfAbsent(getQuestIntId(), k -> new QuestRates(getQuestIntId()));
    }

    @Override
    public void onInit() {
        if(!Config.DONTLOADQUEST) {
            if(!ArrayUtils.contains(Config.ALT_NOLOAD_QUESTS, _questId)) {
                QuestManager.addQuest(this);
            }
        }
    }

    public class DeSpawnScheduleTimerTask extends RunnableImpl {
        NpcInstance _npc;

        public DeSpawnScheduleTimerTask(final NpcInstance npc) {
            _npc = null;
            _npc = npc;
        }

        @Override
        public void runImpl() {
            if (_npc != null) {
                if (_npc.getSpawn() != null) {
                    _npc.getSpawn().deleteAll();
                } else {
                    _npc.deleteMe();
                }
            }
        }
    }
}
