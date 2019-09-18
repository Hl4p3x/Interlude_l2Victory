package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _211_TrialOfChallenger extends Quest {
    private static final int Filaur = 30535;
    private static final int Kash = 30644;
    private static final int Martien = 30645;
    private static final int Raldo = 30646;
    private static final int ChestOfShyslassys = 30647;
    private static final int Shyslassys = 27110;
    private static final int CaveBasilisk = 27111;
    private static final int Gorr = 27112;
    private static final int Baraham = 27113;
    private static final int SuccubusQueen = 27114;
    private static final int LETTER_OF_KASH_ID = 2628;
    private static final int SCROLL_OF_SHYSLASSY_ID = 2631;
    private static final int WATCHERS_EYE1_ID = 2629;
    private static final int BROKEN_KEY_ID = 2632;
    private static final int MITHRIL_SCALE_GAITERS_MATERIAL_ID = 2918;
    private static final int BRIGANDINE_GAUNTLET_PATTERN_ID = 2927;
    private static final int MANTICOR_SKIN_GAITERS_PATTERN_ID = 1943;
    private static final int GAUNTLET_OF_REPOSE_OF_THE_SOUL_PATTERN_ID = 1946;
    private static final int IRON_BOOTS_DESIGN_ID = 1940;
    private static final int TOME_OF_BLOOD_PAGE_ID = 2030;
    private static final int ELVEN_NECKLACE_BEADS_ID = 1904;
    private static final int WHITE_TUNIC_PATTERN_ID = 1936;
    private static final int MARK_OF_CHALLENGER_ID = 2627;
    private static final int WATCHERS_EYE2_ID = 2630;
    private static final int RewardExp = 72394;
    private static final int RewardSP = 11250;
    public NpcInstance Raldo_Spawn;

    public _211_TrialOfChallenger() {
        super(false);
        addStartNpc(30644);
        addTalkId(30535);
        addTalkId(30645);
        addTalkId(30646);
        addTalkId(30647);
        addKillId(27110);
        addKillId(27111);
        addKillId(27112);
        addKillId(27113);
        addKillId(27114);
        addQuestItem(2631, 2628, 2629, 2632, 2630);
    }

    private void Spawn_Raldo(final QuestState st) {
        if (Raldo_Spawn != null) {
            Raldo_Spawn.deleteMe();
        }
        Raldo_Spawn = addSpawn(30646, st.getPlayer().getLoc(), 100, 300000);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equalsIgnoreCase(event)) {
            htmltext = "kash_q0211_05.htm";
            st.setCond(1);
            st.setState(2);
            if (!st.getPlayer().getVarB("dd1")) {
                st.giveItems(7562, 61L);
                st.getPlayer().setVar("dd1", "1", -1L);
            }
            st.playSound("ItemSound.quest_accept");
        } else if ("30644_1".equalsIgnoreCase(event)) {
            htmltext = "kash_q0211_04.htm";
        } else if ("30645_1".equalsIgnoreCase(event)) {
            htmltext = "martian_q0211_02.htm";
            st.takeItems(2628, 1L);
            st.setCond(4);
        } else if ("30647_1".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(2632) > 0L) {
                st.giveItems(2631, 1L);
                if (Rnd.chance(22)) {
                    htmltext = "chest_of_shyslassys_q0211_03.htm";
                    st.takeItems(2632, 1L);
                    st.playSound("ItemSound.quest_jackpot");
                    final int n = Rnd.get(100);
                    if (n > 90) {
                        st.giveItems(2918, 1L);
                        st.giveItems(2927, 1L);
                        st.giveItems(1943, 1L);
                        st.giveItems(1946, 1L);
                        st.giveItems(1940, 1L);
                    } else if (n > 70) {
                        st.giveItems(2030, 1L);
                        st.giveItems(1904, 1L);
                    } else if (n > 40) {
                        st.giveItems(1936, 1L);
                    } else {
                        st.giveItems(1940, 1L);
                    }
                } else {
                    htmltext = "chest_of_shyslassys_q0211_02.htm";
                    st.takeItems(2632, -1L);
                    st.giveItems(57, (long) (Rnd.get(1000) + 1));
                }
            } else {
                htmltext = "chest_of_shyslassys_q0211_04.htm";
            }
        } else if ("30646_1".equalsIgnoreCase(event)) {
            htmltext = "raldo_q0211_02.htm";
        } else if ("30646_2".equalsIgnoreCase(event)) {
            htmltext = "raldo_q0211_03.htm";
        } else if ("30646_3".equalsIgnoreCase(event)) {
            htmltext = "raldo_q0211_04.htm";
            st.setCond(8);
            st.takeItems(2630, 1L);
        } else if ("30646_4".equalsIgnoreCase(event)) {
            htmltext = "raldo_q0211_06.htm";
            st.setCond(8);
            st.takeItems(2630, 1L);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(2627) > 0L) {
            st.exitCurrentQuest(true);
            return "completed";
        }
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        final int cond = st.getCond();
        if (id == 1) {
            st.setCond(0);
            if (npcId == 30644) {
                if (st.getPlayer().getClassId().ordinal() == 1 || st.getPlayer().getClassId().ordinal() == 19 || st.getPlayer().getClassId().ordinal() == 32 || st.getPlayer().getClassId().ordinal() == 45 || st.getPlayer().getClassId().ordinal() == 47) {
                    if (st.getPlayer().getLevel() >= 35) {
                        htmltext = "kash_q0211_03.htm";
                    } else {
                        htmltext = "kash_q0211_01.htm";
                        st.exitCurrentQuest(true);
                    }
                } else {
                    htmltext = "kash_q0211_02.htm";
                    st.exitCurrentQuest(true);
                }
            }
        } else if (npcId == 30644 && cond == 1) {
            htmltext = "kash_q0211_06.htm";
        } else if (npcId == 30644 && cond == 2 && st.getQuestItemsCount(2631) == 1L) {
            htmltext = "kash_q0211_07.htm";
            st.takeItems(2631, 1L);
            st.giveItems(2628, 1L);
            st.setCond(3);
        } else if (npcId == 30644 && cond == 1 && st.getQuestItemsCount(2628) == 1L) {
            htmltext = "kash_q0211_08.htm";
        } else if (npcId == 30644 && cond >= 7) {
            htmltext = "kash_q0211_09.htm";
        } else if (npcId == 30645 && cond == 3 && st.getQuestItemsCount(2628) == 1L) {
            htmltext = "martian_q0211_01.htm";
        } else if (npcId == 30645 && cond == 4 && st.getQuestItemsCount(2629) == 0L) {
            htmltext = "martian_q0211_03.htm";
        } else if (npcId == 30645 && cond == 5 && st.getQuestItemsCount(2629) > 0L) {
            htmltext = "martian_q0211_04.htm";
            st.takeItems(2629, 1L);
            st.setCond(6);
        } else if (npcId == 30645 && cond == 6) {
            htmltext = "martian_q0211_05.htm";
        } else if (npcId == 30645 && cond >= 7) {
            htmltext = "martian_q0211_06.htm";
        } else if (npcId == 30647 && cond == 2) {
            htmltext = "chest_of_shyslassys_q0211_01.htm";
        } else if (npcId == 30646 && cond == 7 && st.getQuestItemsCount(2630) > 0L) {
            htmltext = "raldo_q0211_01.htm";
        } else if (npcId == 30646 && cond == 8) {
            htmltext = "raldo_q0211_06a.htm";
        } else if (npcId == 30646 && cond == 10) {
            htmltext = "raldo_q0211_07.htm";
            st.takeItems(2632, -1L);
            st.giveItems(2627, 1L);
            if (!st.getPlayer().getVarB("prof2.1")) {
                st.addExpAndSp(72394L, 11250L);
                st.getPlayer().setVar("prof2.1", "1", -1L);
            }
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        } else if (npcId == 30535 && cond == 8) {
            if (st.getPlayer().getLevel() >= 36) {
                htmltext = "elder_filaur_q0211_01.htm";
                st.addRadar(176560, -184969, -3729);
                st.setCond(9);
            } else {
                htmltext = "elder_filaur_q0211_03.htm";
            }
        } else if (npcId == 30535 && cond == 9) {
            htmltext = "elder_filaur_q0211_02.htm";
            st.addRadar(176560, -184969, -3729);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 27110 && cond == 1 && st.getQuestItemsCount(2631) == 0L && st.getQuestItemsCount(2632) == 0L) {
            st.giveItems(2632, 1L);
            st.addSpawn(30647);
            st.playSound("ItemSound.quest_middle");
            st.setCond(2);
        } else if (npcId == 27112 && cond == 4 && st.getQuestItemsCount(2629) == 0L) {
            st.giveItems(2629, 1L);
            st.setCond(5);
            st.playSound("ItemSound.quest_middle");
        } else if (npcId == 27113 && (cond == 6 || cond == 7)) {
            if (st.getQuestItemsCount(2630) == 0L) {
                st.giveItems(2630, 1L);
            }
            st.playSound("ItemSound.quest_middle");
            st.setCond(7);
            Spawn_Raldo(st);
        } else if (npcId == 27114 && (cond == 9 || cond == 10)) {
            st.setCond(10);
            st.playSound("ItemSound.quest_middle");
            Spawn_Raldo(st);
        }
        return null;
    }

    
}
