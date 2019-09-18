package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Summon;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.HashMap;
import java.util.Map;

public class _230_TestOfSummoner extends Quest {
    static int MARK_OF_SUMMONER_ID = 3336;
    static int LETOLIZARDMAN_AMULET_ID;
    static int SAC_OF_REDSPORES_ID;
    static int KARULBUGBEAR_TOTEM_ID;
    static int SHARDS_OF_MANASHEN_ID;
    static int BREKAORC_TOTEM_ID;
    static int CRIMSON_BLOODSTONE_ID;
    static int TALONS_OF_TYRANT_ID;
    static int WINGS_OF_DRONEANT_ID;
    static int TUSK_OF_WINDSUS_ID;
    static int FANGS_OF_WYRM_ID;
    static int LARS_LIST1_ID;
    static int LARS_LIST2_ID;
    static int LARS_LIST3_ID;
    static int LARS_LIST4_ID;
    static int LARS_LIST5_ID;
    static int GALATEAS_LETTER_ID = 3352;
    static int BEGINNERS_ARCANA_ID = 3353;
    static int ALMORS_ARCANA_ID;
    static int CAMONIELL_ARCANA_ID;
    static int BELTHUS_ARCANA_ID;
    static int BASILLIA_ARCANA_ID;
    static int CELESTIEL_ARCANA_ID;
    static int BRYNTHEA_ARCANA_ID;
    static int CRYSTAL_OF_PROGRESS1_ID;
    static int CRYSTAL_OF_INPROGRESS1_ID;
    static int CRYSTAL_OF_FOUL1_ID;
    static int CRYSTAL_OF_DEFEAT1_ID;
    static int CRYSTAL_OF_VICTORY1_ID;
    static int CRYSTAL_OF_PROGRESS2_ID;
    static int CRYSTAL_OF_INPROGRESS2_ID;
    static int CRYSTAL_OF_FOUL2_ID;
    static int CRYSTAL_OF_DEFEAT2_ID;
    static int CRYSTAL_OF_VICTORY2_ID;
    static int CRYSTAL_OF_PROGRESS3_ID;
    static int CRYSTAL_OF_INPROGRESS3_ID;
    static int CRYSTAL_OF_FOUL3_ID;
    static int CRYSTAL_OF_DEFEAT3_ID;
    static int CRYSTAL_OF_VICTORY3_ID;
    static int CRYSTAL_OF_PROGRESS4_ID;
    static int CRYSTAL_OF_INPROGRESS4_ID;
    static int CRYSTAL_OF_FOUL4_ID;
    static int CRYSTAL_OF_DEFEAT4_ID;
    static int CRYSTAL_OF_VICTORY4_ID;
    static int CRYSTAL_OF_PROGRESS5_ID;
    static int CRYSTAL_OF_INPROGRESS5_ID;
    static int CRYSTAL_OF_FOUL5_ID;
    static int CRYSTAL_OF_DEFEAT5_ID;
    static int CRYSTAL_OF_VICTORY5_ID;
    static int CRYSTAL_OF_PROGRESS6_ID;
    static int CRYSTAL_OF_INPROGRESS6_ID;
    static int CRYSTAL_OF_FOUL6_ID;
    static int CRYSTAL_OF_DEFEAT6_ID;
    static int CRYSTAL_OF_VICTORY6_ID;
    static int[] npc;
    static int Lara;
    static int Galatea;
    static int Almors;
    static int Camoniell;
    static int Belthus;
    static int Basilla;
    static int Celestiel;
    static int Brynthea;
    static int[][] SUMMONERS;
    static Map<Integer, String> NAMES;
    static Map<Integer, Integer[]> DROPLIST_LARA;
    static String[] STATS;
    static int[][] LISTS;
    static Map<Integer, Integer[]> DROPLIST_SUMMON;
    static Map<Integer, String> DROPLIST_SUMMON_VARS = new HashMap<>();

    static {
        _230_TestOfSummoner.LETOLIZARDMAN_AMULET_ID = 3337;
        _230_TestOfSummoner.SAC_OF_REDSPORES_ID = 3338;
        _230_TestOfSummoner.KARULBUGBEAR_TOTEM_ID = 3339;
        _230_TestOfSummoner.SHARDS_OF_MANASHEN_ID = 3340;
        _230_TestOfSummoner.BREKAORC_TOTEM_ID = 3341;
        _230_TestOfSummoner.CRIMSON_BLOODSTONE_ID = 3342;
        _230_TestOfSummoner.TALONS_OF_TYRANT_ID = 3343;
        _230_TestOfSummoner.WINGS_OF_DRONEANT_ID = 3344;
        _230_TestOfSummoner.TUSK_OF_WINDSUS_ID = 3345;
        _230_TestOfSummoner.FANGS_OF_WYRM_ID = 3346;
        _230_TestOfSummoner.LARS_LIST1_ID = 3347;
        _230_TestOfSummoner.LARS_LIST2_ID = 3348;
        _230_TestOfSummoner.LARS_LIST3_ID = 3349;
        _230_TestOfSummoner.LARS_LIST4_ID = 3350;
        _230_TestOfSummoner.LARS_LIST5_ID = 3351;
        _230_TestOfSummoner.ALMORS_ARCANA_ID = 3354;
        _230_TestOfSummoner.CAMONIELL_ARCANA_ID = 3355;
        _230_TestOfSummoner.BELTHUS_ARCANA_ID = 3356;
        _230_TestOfSummoner.BASILLIA_ARCANA_ID = 3357;
        _230_TestOfSummoner.CELESTIEL_ARCANA_ID = 3358;
        _230_TestOfSummoner.BRYNTHEA_ARCANA_ID = 3359;
        _230_TestOfSummoner.CRYSTAL_OF_PROGRESS1_ID = 3360;
        _230_TestOfSummoner.CRYSTAL_OF_INPROGRESS1_ID = 3361;
        _230_TestOfSummoner.CRYSTAL_OF_FOUL1_ID = 3362;
        _230_TestOfSummoner.CRYSTAL_OF_DEFEAT1_ID = 3363;
        _230_TestOfSummoner.CRYSTAL_OF_VICTORY1_ID = 3364;
        _230_TestOfSummoner.CRYSTAL_OF_PROGRESS2_ID = 3365;
        _230_TestOfSummoner.CRYSTAL_OF_INPROGRESS2_ID = 3366;
        _230_TestOfSummoner.CRYSTAL_OF_FOUL2_ID = 3367;
        _230_TestOfSummoner.CRYSTAL_OF_DEFEAT2_ID = 3368;
        _230_TestOfSummoner.CRYSTAL_OF_VICTORY2_ID = 3369;
        _230_TestOfSummoner.CRYSTAL_OF_PROGRESS3_ID = 3370;
        _230_TestOfSummoner.CRYSTAL_OF_INPROGRESS3_ID = 3371;
        _230_TestOfSummoner.CRYSTAL_OF_FOUL3_ID = 3372;
        _230_TestOfSummoner.CRYSTAL_OF_DEFEAT3_ID = 3373;
        _230_TestOfSummoner.CRYSTAL_OF_VICTORY3_ID = 3374;
        _230_TestOfSummoner.CRYSTAL_OF_PROGRESS4_ID = 3375;
        _230_TestOfSummoner.CRYSTAL_OF_INPROGRESS4_ID = 3376;
        _230_TestOfSummoner.CRYSTAL_OF_FOUL4_ID = 3377;
        _230_TestOfSummoner.CRYSTAL_OF_DEFEAT4_ID = 3378;
        _230_TestOfSummoner.CRYSTAL_OF_VICTORY4_ID = 3379;
        _230_TestOfSummoner.CRYSTAL_OF_PROGRESS5_ID = 3380;
        _230_TestOfSummoner.CRYSTAL_OF_INPROGRESS5_ID = 3381;
        _230_TestOfSummoner.CRYSTAL_OF_FOUL5_ID = 3382;
        _230_TestOfSummoner.CRYSTAL_OF_DEFEAT5_ID = 3383;
        _230_TestOfSummoner.CRYSTAL_OF_VICTORY5_ID = 3384;
        _230_TestOfSummoner.CRYSTAL_OF_PROGRESS6_ID = 3385;
        _230_TestOfSummoner.CRYSTAL_OF_INPROGRESS6_ID = 3386;
        _230_TestOfSummoner.CRYSTAL_OF_FOUL6_ID = 3387;
        _230_TestOfSummoner.CRYSTAL_OF_DEFEAT6_ID = 3388;
        _230_TestOfSummoner.CRYSTAL_OF_VICTORY6_ID = 3389;
        _230_TestOfSummoner.npc = new int[]{30063, 30634, 30635, 30636, 30637, 30638, 30639, 30640};
        _230_TestOfSummoner.Lara = _230_TestOfSummoner.npc[0];
        _230_TestOfSummoner.Galatea = _230_TestOfSummoner.npc[1];
        _230_TestOfSummoner.Almors = _230_TestOfSummoner.npc[2];
        _230_TestOfSummoner.Camoniell = _230_TestOfSummoner.npc[3];
        _230_TestOfSummoner.Belthus = _230_TestOfSummoner.npc[4];
        _230_TestOfSummoner.Basilla = _230_TestOfSummoner.npc[5];
        _230_TestOfSummoner.Celestiel = _230_TestOfSummoner.npc[6];
        _230_TestOfSummoner.Brynthea = _230_TestOfSummoner.npc[7];
        _230_TestOfSummoner.SUMMONERS = new int[][]{{30635, _230_TestOfSummoner.ALMORS_ARCANA_ID, _230_TestOfSummoner.CRYSTAL_OF_VICTORY1_ID}, {30636, _230_TestOfSummoner.CAMONIELL_ARCANA_ID, _230_TestOfSummoner.CRYSTAL_OF_VICTORY2_ID}, {30637, _230_TestOfSummoner.BELTHUS_ARCANA_ID, _230_TestOfSummoner.CRYSTAL_OF_VICTORY3_ID}, {30638, _230_TestOfSummoner.BASILLIA_ARCANA_ID, _230_TestOfSummoner.CRYSTAL_OF_VICTORY4_ID}, {30639, _230_TestOfSummoner.CELESTIEL_ARCANA_ID, _230_TestOfSummoner.CRYSTAL_OF_VICTORY5_ID}, {30640, _230_TestOfSummoner.BRYNTHEA_ARCANA_ID, _230_TestOfSummoner.CRYSTAL_OF_VICTORY6_ID}};
        (_230_TestOfSummoner.NAMES = new HashMap<>()).put(30635, "Almors");
        _230_TestOfSummoner.NAMES.put(30636, "Camoniell");
        _230_TestOfSummoner.NAMES.put(30637, "Belthus");
        _230_TestOfSummoner.NAMES.put(30638, "Basilla");
        _230_TestOfSummoner.NAMES.put(30639, "Celestiel");
        _230_TestOfSummoner.NAMES.put(30640, "Brynthea");
        (_230_TestOfSummoner.DROPLIST_LARA = new HashMap<>()).put(20555, new Integer[]{1, 80, _230_TestOfSummoner.SAC_OF_REDSPORES_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20557, new Integer[]{1, 25, _230_TestOfSummoner.LETOLIZARDMAN_AMULET_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20558, new Integer[]{1, 25, _230_TestOfSummoner.LETOLIZARDMAN_AMULET_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20559, new Integer[]{1, 25, _230_TestOfSummoner.LETOLIZARDMAN_AMULET_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20580, new Integer[]{1, 50, _230_TestOfSummoner.LETOLIZARDMAN_AMULET_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20581, new Integer[]{1, 75, _230_TestOfSummoner.LETOLIZARDMAN_AMULET_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20582, new Integer[]{1, 75, _230_TestOfSummoner.LETOLIZARDMAN_AMULET_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20600, new Integer[]{2, 80, _230_TestOfSummoner.KARULBUGBEAR_TOTEM_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20563, new Integer[]{2, 80, _230_TestOfSummoner.SHARDS_OF_MANASHEN_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20552, new Integer[]{3, 60, _230_TestOfSummoner.CRIMSON_BLOODSTONE_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20267, new Integer[]{3, 25, _230_TestOfSummoner.BREKAORC_TOTEM_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20268, new Integer[]{3, 25, _230_TestOfSummoner.BREKAORC_TOTEM_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20271, new Integer[]{3, 25, _230_TestOfSummoner.BREKAORC_TOTEM_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20269, new Integer[]{3, 50, _230_TestOfSummoner.BREKAORC_TOTEM_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20270, new Integer[]{3, 50, _230_TestOfSummoner.BREKAORC_TOTEM_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20553, new Integer[]{4, 70, _230_TestOfSummoner.TUSK_OF_WINDSUS_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20192, new Integer[]{4, 50, _230_TestOfSummoner.TALONS_OF_TYRANT_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20193, new Integer[]{4, 50, _230_TestOfSummoner.TALONS_OF_TYRANT_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20089, new Integer[]{5, 30, _230_TestOfSummoner.WINGS_OF_DRONEANT_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20090, new Integer[]{5, 60, _230_TestOfSummoner.WINGS_OF_DRONEANT_ID});
        _230_TestOfSummoner.DROPLIST_LARA.put(20176, new Integer[]{5, 50, _230_TestOfSummoner.FANGS_OF_WYRM_ID});
        _230_TestOfSummoner.STATS = new String[]{"cond", "step", "Lara_Part", "Arcanas", "Belthus", "Brynthea", "Celestiel", "Camoniell", "Basilla", "Almors"};
        _230_TestOfSummoner.LISTS = new int[][]{new int[0], {_230_TestOfSummoner.LARS_LIST1_ID, _230_TestOfSummoner.SAC_OF_REDSPORES_ID, _230_TestOfSummoner.LETOLIZARDMAN_AMULET_ID}, {_230_TestOfSummoner.LARS_LIST2_ID, _230_TestOfSummoner.KARULBUGBEAR_TOTEM_ID, _230_TestOfSummoner.SHARDS_OF_MANASHEN_ID}, {_230_TestOfSummoner.LARS_LIST3_ID, _230_TestOfSummoner.CRIMSON_BLOODSTONE_ID, _230_TestOfSummoner.BREKAORC_TOTEM_ID}, {_230_TestOfSummoner.LARS_LIST4_ID, _230_TestOfSummoner.TUSK_OF_WINDSUS_ID, _230_TestOfSummoner.TALONS_OF_TYRANT_ID}, {_230_TestOfSummoner.LARS_LIST5_ID, _230_TestOfSummoner.WINGS_OF_DRONEANT_ID, _230_TestOfSummoner.FANGS_OF_WYRM_ID}};
        (_230_TestOfSummoner.DROPLIST_SUMMON = new HashMap<>()).put(27102, new Integer[]{_230_TestOfSummoner.CRYSTAL_OF_PROGRESS1_ID, _230_TestOfSummoner.CRYSTAL_OF_INPROGRESS1_ID, _230_TestOfSummoner.CRYSTAL_OF_FOUL1_ID, _230_TestOfSummoner.CRYSTAL_OF_DEFEAT1_ID, _230_TestOfSummoner.CRYSTAL_OF_VICTORY1_ID});
        _230_TestOfSummoner.DROPLIST_SUMMON.put(27103, new Integer[]{_230_TestOfSummoner.CRYSTAL_OF_PROGRESS2_ID, _230_TestOfSummoner.CRYSTAL_OF_INPROGRESS2_ID, _230_TestOfSummoner.CRYSTAL_OF_FOUL2_ID, _230_TestOfSummoner.CRYSTAL_OF_DEFEAT2_ID, _230_TestOfSummoner.CRYSTAL_OF_VICTORY2_ID});
        _230_TestOfSummoner.DROPLIST_SUMMON.put(27104, new Integer[]{_230_TestOfSummoner.CRYSTAL_OF_PROGRESS3_ID, _230_TestOfSummoner.CRYSTAL_OF_INPROGRESS3_ID, _230_TestOfSummoner.CRYSTAL_OF_FOUL3_ID, _230_TestOfSummoner.CRYSTAL_OF_DEFEAT3_ID, _230_TestOfSummoner.CRYSTAL_OF_VICTORY3_ID});
        _230_TestOfSummoner.DROPLIST_SUMMON.put(27105, new Integer[]{_230_TestOfSummoner.CRYSTAL_OF_PROGRESS4_ID, _230_TestOfSummoner.CRYSTAL_OF_INPROGRESS4_ID, _230_TestOfSummoner.CRYSTAL_OF_FOUL4_ID, _230_TestOfSummoner.CRYSTAL_OF_DEFEAT4_ID, _230_TestOfSummoner.CRYSTAL_OF_VICTORY4_ID});
        _230_TestOfSummoner.DROPLIST_SUMMON.put(27106, new Integer[]{_230_TestOfSummoner.CRYSTAL_OF_PROGRESS5_ID, _230_TestOfSummoner.CRYSTAL_OF_INPROGRESS5_ID, _230_TestOfSummoner.CRYSTAL_OF_FOUL5_ID, _230_TestOfSummoner.CRYSTAL_OF_DEFEAT5_ID, _230_TestOfSummoner.CRYSTAL_OF_VICTORY5_ID});
        _230_TestOfSummoner.DROPLIST_SUMMON.put(27107, new Integer[]{_230_TestOfSummoner.CRYSTAL_OF_PROGRESS6_ID, _230_TestOfSummoner.CRYSTAL_OF_INPROGRESS6_ID, _230_TestOfSummoner.CRYSTAL_OF_FOUL6_ID, _230_TestOfSummoner.CRYSTAL_OF_DEFEAT6_ID, _230_TestOfSummoner.CRYSTAL_OF_VICTORY6_ID});
        _230_TestOfSummoner.NAMES.put(27102, "Almors");
        _230_TestOfSummoner.NAMES.put(27103, "Camoniell");
        _230_TestOfSummoner.NAMES.put(27104, "Belthus");
        _230_TestOfSummoner.NAMES.put(27105, "Basilla");
        _230_TestOfSummoner.NAMES.put(27106, "Celestiel");
        _230_TestOfSummoner.NAMES.put(27107, "Brynthea");
    }

    public _230_TestOfSummoner() {
        super(false);
        addStartNpc(_230_TestOfSummoner.Galatea);
        for (final int npcId : _230_TestOfSummoner.npc) {
            addTalkId(npcId);
        }
        for (final int mobId : _230_TestOfSummoner.DROPLIST_LARA.keySet()) {
            addKillId(mobId);
        }
        for (final int mobId : _230_TestOfSummoner.DROPLIST_SUMMON.keySet()) {
            addKillId(mobId);
            addAttackId(mobId);
        }
        for (int i = 3337; i <= 3389; ++i) {
            addQuestItem(i);
        }
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30634-08.htm".equalsIgnoreCase(event)) {
            for (final String var : _230_TestOfSummoner.STATS) {
                if (!"Arcanas".equalsIgnoreCase(var)) {
                    if (!"Lara_Part".equalsIgnoreCase(var)) {
                        if ("cond".equalsIgnoreCase(var)) {
                            st.setCond(1);
                        } else {
                            st.set(var, "1");
                        }
                    }
                }
            }
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            if (!st.getPlayer().getVarB("dd3")) {
                st.giveItems(7562, 122L, false);
                st.getPlayer().setVar("dd3", "1", -1L);
            }
        } else if ("30634-07.htm".equalsIgnoreCase(event)) {
            st.giveItems(_230_TestOfSummoner.GALATEAS_LETTER_ID, 1L, false);
        } else if ("30063-02.htm".equalsIgnoreCase(event)) {
            final int random = Rnd.get(5) + 1;
            st.giveItems(_230_TestOfSummoner.LISTS[random][0], 1L, false);
            st.takeItems(_230_TestOfSummoner.GALATEAS_LETTER_ID, 1L);
            st.set("Lara_Part", str((long) random));
            st.set("step", "2");
            st.setCond(2);
        } else if ("30063-04.htm".equalsIgnoreCase(event)) {
            final int random = Rnd.get(5) + 1;
            st.giveItems(_230_TestOfSummoner.LISTS[random][0], 1L, false);
            st.set("Lara_Part", str((long) random));
        } else if ("30635-02.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(_230_TestOfSummoner.BEGINNERS_ARCANA_ID) > 0L) {
                htmltext = "30635-03.htm";
                st.set("Almors", "2");
            }
        } else if ("30635-04.htm".equalsIgnoreCase(event)) {
            st.giveItems(_230_TestOfSummoner.CRYSTAL_OF_PROGRESS1_ID, 1L, false);
            st.takeItems(_230_TestOfSummoner.CRYSTAL_OF_FOUL1_ID, -1L);
            st.takeItems(_230_TestOfSummoner.CRYSTAL_OF_DEFEAT1_ID, -1L);
            st.takeItems(_230_TestOfSummoner.BEGINNERS_ARCANA_ID, 1L);
        } else if ("30636-02.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(_230_TestOfSummoner.BEGINNERS_ARCANA_ID) > 0L) {
                htmltext = "30636-03.htm";
                st.set("Camoniell", "2");
            }
        } else if ("30636-04.htm".equalsIgnoreCase(event)) {
            st.giveItems(_230_TestOfSummoner.CRYSTAL_OF_PROGRESS2_ID, 1L, false);
            st.takeItems(_230_TestOfSummoner.CRYSTAL_OF_FOUL2_ID, -1L);
            st.takeItems(_230_TestOfSummoner.CRYSTAL_OF_DEFEAT2_ID, -1L);
            st.takeItems(_230_TestOfSummoner.BEGINNERS_ARCANA_ID, 1L);
        } else if ("30637-02.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(_230_TestOfSummoner.BEGINNERS_ARCANA_ID) > 0L) {
                htmltext = "30637-03.htm";
                st.set("Belthus", "2");
            }
        } else if ("30637-04.htm".equalsIgnoreCase(event)) {
            st.giveItems(_230_TestOfSummoner.CRYSTAL_OF_PROGRESS3_ID, 1L, false);
            st.takeItems(_230_TestOfSummoner.CRYSTAL_OF_FOUL3_ID, -1L);
            st.takeItems(_230_TestOfSummoner.CRYSTAL_OF_DEFEAT3_ID, -1L);
            st.takeItems(_230_TestOfSummoner.BEGINNERS_ARCANA_ID, 1L);
        } else if ("30638-02.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(_230_TestOfSummoner.BEGINNERS_ARCANA_ID) > 0L) {
                htmltext = "30638-03.htm";
                st.set("Basilla", "2");
            }
        } else if ("30638-04.htm".equalsIgnoreCase(event)) {
            st.giveItems(_230_TestOfSummoner.CRYSTAL_OF_PROGRESS4_ID, 1L, false);
            st.takeItems(_230_TestOfSummoner.CRYSTAL_OF_FOUL4_ID, -1L);
            st.takeItems(_230_TestOfSummoner.CRYSTAL_OF_DEFEAT4_ID, -1L);
            st.takeItems(_230_TestOfSummoner.BEGINNERS_ARCANA_ID, 1L);
        } else if ("30639-02.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(_230_TestOfSummoner.BEGINNERS_ARCANA_ID) > 0L) {
                htmltext = "30639-03.htm";
                st.set("Celestiel", "2");
            }
        } else if ("30639-04.htm".equalsIgnoreCase(event)) {
            st.giveItems(_230_TestOfSummoner.CRYSTAL_OF_PROGRESS5_ID, 1L, false);
            st.takeItems(_230_TestOfSummoner.CRYSTAL_OF_FOUL5_ID, -1L);
            st.takeItems(_230_TestOfSummoner.CRYSTAL_OF_DEFEAT5_ID, -1L);
            st.takeItems(_230_TestOfSummoner.BEGINNERS_ARCANA_ID, 1L);
        } else if ("30640-02.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(_230_TestOfSummoner.BEGINNERS_ARCANA_ID) > 0L) {
                htmltext = "30640-03.htm";
                st.set("Brynthea", "2");
            }
        } else if ("30640-04.htm".equalsIgnoreCase(event)) {
            st.giveItems(_230_TestOfSummoner.CRYSTAL_OF_PROGRESS6_ID, 1L, false);
            st.takeItems(_230_TestOfSummoner.CRYSTAL_OF_FOUL6_ID, -1L);
            st.takeItems(_230_TestOfSummoner.CRYSTAL_OF_DEFEAT6_ID, -1L);
            st.takeItems(_230_TestOfSummoner.BEGINNERS_ARCANA_ID, 1L);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(_230_TestOfSummoner.MARK_OF_SUMMONER_ID) > 0L) {
            st.exitCurrentQuest(true);
            return "completed";
        }
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        if (id == 1 && npcId == 30634) {
            for (final String var : _230_TestOfSummoner.STATS) {
                if ("cond".equalsIgnoreCase(var)) {
                    st.setCond(0);
                } else {
                    st.set(var, "0");
                }
            }
            if (st.getPlayer().getClassId() == ClassId.wizard || st.getPlayer().getClassId() == ClassId.elvenWizard || st.getPlayer().getClassId() == ClassId.darkWizard) {
                if (st.getPlayer().getLevel() > 38) {
                    htmltext = "30634-03.htm";
                } else {
                    htmltext = "30634-02.htm";
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "30634-01.htm";
                st.exitCurrentQuest(true);
            }
        } else if (id == 2) {
            final int LaraPart = st.getInt("Lara_Part");
            final int Arcanas = st.getInt("Arcanas");
            final int step = st.getInt("step");
            if (npcId == 30634) {
                switch (step) {
                    case 1:
                        htmltext = "30634-09.htm";
                        break;
                    case 2:
                        if (Arcanas == 6) {
                            htmltext = "30634-12.htm";
                            st.playSound("ItemSound.quest_finish");
                            st.takeItems(_230_TestOfSummoner.LARS_LIST1_ID, -1L);
                            st.takeItems(_230_TestOfSummoner.LARS_LIST2_ID, -1L);
                            st.takeItems(_230_TestOfSummoner.LARS_LIST3_ID, -1L);
                            st.takeItems(_230_TestOfSummoner.LARS_LIST4_ID, -1L);
                            st.takeItems(_230_TestOfSummoner.LARS_LIST5_ID, -1L);
                            st.takeItems(_230_TestOfSummoner.ALMORS_ARCANA_ID, -1L);
                            st.takeItems(_230_TestOfSummoner.BASILLIA_ARCANA_ID, -1L);
                            st.takeItems(_230_TestOfSummoner.CAMONIELL_ARCANA_ID, -1L);
                            st.takeItems(_230_TestOfSummoner.CELESTIEL_ARCANA_ID, -1L);
                            st.takeItems(_230_TestOfSummoner.BELTHUS_ARCANA_ID, -1L);
                            st.takeItems(_230_TestOfSummoner.BRYNTHEA_ARCANA_ID, -1L);
                            st.giveItems(_230_TestOfSummoner.MARK_OF_SUMMONER_ID, 1L);
                            if (!st.getPlayer().getVarB("prof2.3")) {
                                st.addExpAndSp(148409L, 30000L);
                                st.getPlayer().setVar("prof2.3", "1", -1L);
                            }
                            st.playSound("ItemSound.quest_finish");
                            st.exitCurrentQuest(true);
                        }
                        break;
                    default:
                        htmltext = "30634-10.htm";
                        break;
                }
            } else if (npcId == _230_TestOfSummoner.Lara) {
                if (step == 1) {
                    htmltext = "30063-01.htm";
                } else if (LaraPart == 0) {
                    htmltext = "30063-03.htm";
                } else {
                    final long ItemCount1 = st.getQuestItemsCount(_230_TestOfSummoner.LISTS[LaraPart][1]);
                    final long ItemCount2 = st.getQuestItemsCount(_230_TestOfSummoner.LISTS[LaraPart][2]);
                    if (ItemCount1 < 30L || ItemCount2 < 30L) {
                        htmltext = "30063-05.htm";
                    } else if (ItemCount1 > 29L && ItemCount2 > 29L) {
                        htmltext = "30063-06.htm";
                        st.giveItems(_230_TestOfSummoner.BEGINNERS_ARCANA_ID, 2L, false);
                        st.takeItems(_230_TestOfSummoner.LISTS[LaraPart][0], 1L);
                        st.takeItems(_230_TestOfSummoner.LISTS[LaraPart][1], -1L);
                        st.takeItems(_230_TestOfSummoner.LISTS[LaraPart][2], -1L);
                        st.setCond(3);
                        st.set("Lara_Part", "0");
                    }
                }
            } else {
                for (final int[] i : _230_TestOfSummoner.SUMMONERS) {
                    if (i[0] == npcId) {
                        final Integer[] k = _230_TestOfSummoner.DROPLIST_SUMMON.get(npcId - 30635 + 27102);
                        final int SummonerStat = st.getInt(_230_TestOfSummoner.NAMES.get(i[0]));
                        if (step > 1) {
                            if (st.getQuestItemsCount(k[0]) > 0L) {
                                htmltext = str((long) npcId) + "-08.htm";
                            } else if (st.getQuestItemsCount(k[1]) > 0L) {
                                st.addNotifyOfDeath(st.getPlayer(), true);
                                htmltext = str((long) npcId) + "-09.htm";
                            } else if (st.getQuestItemsCount(k[3]) > 0L) {
                                htmltext = str((long) npcId) + "-05.htm";
                            } else if (st.getQuestItemsCount(k[2]) > 0L) {
                                htmltext = str((long) npcId) + "-06.htm";
                            } else if (st.getQuestItemsCount(k[4]) > 0L) {
                                htmltext = str((long) npcId) + "-07.htm";
                                st.takeItems(_230_TestOfSummoner.SUMMONERS[npcId - 30635][2], -1L);
                                st.giveItems(_230_TestOfSummoner.SUMMONERS[npcId - 30635][1], 1L, false);
                                if (st.getQuestItemsCount(3354) + st.getQuestItemsCount(3355) + st.getQuestItemsCount(3356) + st.getQuestItemsCount(3357) + st.getQuestItemsCount(3358) + st.getQuestItemsCount(3359) >= 6L) {
                                    st.setCond(4);
                                }
                                st.set(_230_TestOfSummoner.NAMES.get(i[0]), "7");
                                st.set("Arcanas", str((long) (Arcanas + 1)));
                            } else if (SummonerStat == 7) {
                                htmltext = str((long) npcId) + "-10.htm";
                            } else {
                                htmltext = str((long) npcId) + "-01.htm";
                            }
                        }
                    }
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onDeath(final Creature killer, final Creature victim, final QuestState st) {
        if (killer == null || victim == null) {
            return null;
        }
        final int npcId = killer.getNpcId();
        if ((victim == st.getPlayer() || victim == st.getPlayer().getPet()) && npcId >= 27102 && npcId <= 27107) {
            final String[] VARS = {"Almors", "Camoniell", "Belthus", "Basilla", "Celestiel", "Brynthea"};
            final String var = VARS[npcId - 27102];
            final Integer[] i = _230_TestOfSummoner.DROPLIST_SUMMON.get(npcId);
            final int defeat = i[3];
            if (st.getInt(var) == 3) {
                st.set(var, "4");
                st.giveItems(defeat, 1L, false);
            }
        }
        return null;
    }

    @Override
    public String onAttack(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (npcId >= 27102 && npcId <= 27107) {
            final String[] VARS = {"Almors", "Camoniell", "Belthus", "Basilla", "Celestiel", "Brynthea"};
            final String var = VARS[npcId - 27102];
            final Integer[] i = _230_TestOfSummoner.DROPLIST_SUMMON.get(npcId);
            final int start = i[0];
            final int progress = i[1];
            if (st.getInt(var) == 2) {
                st.set(var, "3");
                st.giveItems(progress, 1L, false);
                st.takeItems(start, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
            if (st.getQuestItemsCount(i[2]) != 0L) {
                return null;
            }
            final Summon summon = st.getPlayer().getPet();
            if (summon == null || summon.isPet()) {
                st.giveItems(i[2], 1L, false);
            }
        }
        return null;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (_230_TestOfSummoner.DROPLIST_LARA.containsKey(npcId)) {
            final Integer[] i = _230_TestOfSummoner.DROPLIST_LARA.get(npcId);
            final String var = "Lara_Part";
            final int value = i[0];
            final int chance = i[1];
            final int item = i[2];
            final long count = st.getQuestItemsCount(item);
            if (st.getInt(var) == value && count < 30L && Rnd.chance(chance)) {
                st.giveItems(item, 1L, true);
                if (count == 29L) {
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (_230_TestOfSummoner.DROPLIST_SUMMON.containsKey(npcId)) {
            final String[] VARS = {"Almors", "Camoniell", "Belthus", "Basilla", "Celestiel", "Brynthea"};
            final String var = VARS[npcId - 27102];
            final Integer[] j = _230_TestOfSummoner.DROPLIST_SUMMON.get(npcId);
            final int progress = j[1];
            final int foul = j[2];
            final int victory = j[4];
            if (st.getInt(var) == 3) {
                final boolean isFoul = st.getQuestItemsCount(foul) == 0L;
                int isName = 1;
                for (final Integer item2 : _230_TestOfSummoner.DROPLIST_SUMMON.get(npcId)) {
                    if (isName != 1) {
                        st.takeItems(item2, -1L);
                    }
                    isName = 0;
                }
                st.takeItems(progress, -1L);
                if (isFoul) {
                    st.set(var, "6");
                    st.giveItems(victory, 1L, false);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.set(var, "5");
                }
            }
        }
        return null;
    }
}
