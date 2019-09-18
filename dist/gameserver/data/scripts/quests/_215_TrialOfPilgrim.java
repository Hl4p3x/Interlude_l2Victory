package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _215_TrialOfPilgrim extends Quest {
    private static final int MARK_OF_PILGRIM_ID = 2721;
    private static final int BOOK_OF_SAGE_ID = 2722;
    private static final int VOUCHER_OF_TRIAL_ID = 2723;
    private static final int SPIRIT_OF_FLAME_ID = 2724;
    private static final int ESSENSE_OF_FLAME_ID = 2725;
    private static final int BOOK_OF_GERALD_ID = 2726;
    private static final int GREY_BADGE_ID = 2727;
    private static final int PICTURE_OF_NAHIR_ID = 2728;
    private static final int HAIR_OF_NAHIR_ID = 2729;
    private static final int STATUE_OF_EINHASAD_ID = 2730;
    private static final int BOOK_OF_DARKNESS_ID = 2731;
    private static final int DEBRIS_OF_WILLOW_ID = 2732;
    private static final int TAG_OF_RUMOR_ID = 2733;
    private static final int RewardExp = 77382;
    private static final int RewardSP = 16000;

    public _215_TrialOfPilgrim() {
        super(false);
        addStartNpc(30648);
        addTalkId(30648);
        addTalkId(30036);
        addTalkId(30117);
        addTalkId(30362);
        addTalkId(30550);
        addTalkId(30571);
        addTalkId(30612);
        addTalkId(30648);
        addTalkId(30649);
        addTalkId(30650);
        addTalkId(30651);
        addTalkId(30652);
        addKillId(27116);
        addKillId(27117);
        addKillId(27118);
        addQuestItem(2722, 2723, 2725, 2726, 2733, 2728, 2729, 2731, 2732, 2727, 2724, 2730);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            htmltext = "hermit_santiago_q0215_04.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2723, 1L);
        }
        if (!st.getPlayer().getVarB("dd1")) {
            st.giveItems(7562, 49L);
            st.getPlayer().setVar("dd1", "1", -1L);
        } else if ("30648_1".equals(event)) {
            htmltext = "hermit_santiago_q0215_05.htm";
        } else if ("30648_2".equals(event)) {
            htmltext = "hermit_santiago_q0215_06.htm";
        } else if ("30648_3".equals(event)) {
            htmltext = "hermit_santiago_q0215_07.htm";
        } else if ("30648_4".equals(event)) {
            htmltext = "hermit_santiago_q0215_08.htm";
        } else if ("30648_5".equals(event)) {
            htmltext = "hermit_santiago_q0215_05.htm";
        } else if ("30649_1".equals(event)) {
            htmltext = "ancestor_martankus_q0215_04.htm";
            st.giveItems(2724, 1L);
            st.takeItems(2725, 1L);
            st.setCond(5);
        } else if ("30650_1".equals(event)) {
            if (st.getQuestItemsCount(57) >= 100000L) {
                htmltext = "gerald_priest_of_earth_q0215_02.htm";
                st.giveItems(2726, 1L);
                st.takeItems(57, 100000L);
                st.setCond(7);
            } else {
                htmltext = "gerald_priest_of_earth_q0215_03.htm";
            }
        } else if ("30650_2".equals(event)) {
            htmltext = "gerald_priest_of_earth_q0215_03.htm";
        } else if ("30362_1".equals(event)) {
            htmltext = "andellria_q0215_05.htm";
            st.takeItems(2731, 1L);
            st.setCond(16);
        } else if ("30362_2".equals(event)) {
            htmltext = "andellria_q0215_04.htm";
            st.setCond(16);
        } else if ("30652_1".equals(event)) {
            htmltext = "uruha_q0215_02.htm";
            st.giveItems(2731, 1L);
            st.takeItems(2732, 1L);
            st.setCond(15);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(2721) > 0L) {
            st.exitCurrentQuest(true);
            return "completed";
        }
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        if (id == 1) {
            st.setState(2);
            st.setCond(0);
            st.set("id", "0");
        }
        if (npcId == 30648 && st.getCond() == 0) {
            if (st.getPlayer().getClassId().getId() == 15 || st.getPlayer().getClassId().getId() == 29 || st.getPlayer().getClassId().getId() == 42 || st.getPlayer().getClassId().getId() == 50) {
                if (st.getPlayer().getLevel() >= 35) {
                    htmltext = "hermit_santiago_q0215_03.htm";
                } else {
                    htmltext = "hermit_santiago_q0215_01.htm";
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "hermit_santiago_q0215_02.htm";
                st.exitCurrentQuest(true);
            }
        } else if (npcId == 30648 && st.getCond() == 1 && st.getQuestItemsCount(2723) > 0L) {
            htmltext = "hermit_santiago_q0215_09.htm";
        } else if (npcId == 30648 && st.getCond() == 17 && st.getQuestItemsCount(2722) > 0L) {
            htmltext = "hermit_santiago_q0215_10.htm";
            st.takeItems(2722, -1L);
            st.giveItems(2721, 1L);
            if (!st.getPlayer().getVarB("prof2.1")) {
                st.addExpAndSp(77382L, 16000L);
                st.getPlayer().setVar("prof2.1", "1", -1L);
            }
            st.playSound("ItemSound.quest_finish");
            st.unset("cond");
            st.exitCurrentQuest(false);
        } else if (npcId == 30571 && st.getCond() == 1 && st.getQuestItemsCount(2723) > 0L) {
            htmltext = "seer_tanapi_q0215_01.htm";
            st.takeItems(2723, 1L);
            st.setCond(2);
        } else if (npcId == 30571 && st.getCond() == 2) {
            htmltext = "seer_tanapi_q0215_02.htm";
        } else if (npcId == 30571 && st.getCond() == 5 && st.getQuestItemsCount(2724) > 0L) {
            htmltext = "seer_tanapi_q0215_03.htm";
        } else if (npcId == 30649 && st.getCond() == 2) {
            htmltext = "ancestor_martankus_q0215_01.htm";
            st.setCond(3);
        } else if (npcId == 30649 && st.getCond() == 3) {
            htmltext = "ancestor_martankus_q0215_02.htm";
        } else if (npcId == 30649 && st.getCond() == 4 && st.getQuestItemsCount(2725) > 0L) {
            htmltext = "ancestor_martankus_q0215_03.htm";
        } else if (npcId == 30550 && st.getCond() == 5 && st.getQuestItemsCount(2724) > 0L) {
            htmltext = "gauri_twinklerock_q0215_01.htm";
            st.giveItems(2733, 1L);
            st.setCond(6);
        } else if (npcId == 30550 && st.getCond() == 6) {
            htmltext = "gauri_twinklerock_q0215_02.htm";
        } else if (npcId == 30650 && st.getCond() == 6 && st.getQuestItemsCount(2733) > 0L) {
            htmltext = "gerald_priest_of_earth_q0215_01.htm";
        } else if (npcId == 30650 && st.getCond() >= 8 && st.getQuestItemsCount(2727) > 0L && st.getQuestItemsCount(2726) > 0L) {
            htmltext = "gerald_priest_of_earth_q0215_04.htm";
            st.giveItems(57, 100000L, false);
            st.takeItems(2726, 1L);
        } else if (npcId == 30651 && st.getCond() == 6 && st.getQuestItemsCount(2733) > 0L) {
            htmltext = "wanderer_dorf_q0215_01.htm";
            st.giveItems(2727, 1L);
            st.takeItems(2733, 1L);
            st.setCond(8);
        } else if (npcId == 30651 && st.getCond() == 7 && st.getQuestItemsCount(2733) > 0L) {
            htmltext = "wanderer_dorf_q0215_02.htm";
            st.giveItems(2727, 1L);
            st.takeItems(2733, 1L);
            st.setCond(8);
        } else if (npcId == 30651 && st.getCond() == 8) {
            htmltext = "wanderer_dorf_q0215_03.htm";
        } else if (npcId == 30117 && st.getCond() == 8) {
            htmltext = "primoz_q0215_01.htm";
            st.setCond(9);
        } else if (npcId == 30117 && st.getCond() == 9) {
            htmltext = "primoz_q0215_02.htm";
        } else if (npcId == 30036 && st.getCond() == 9) {
            htmltext = "potter_q0215_01.htm";
            st.giveItems(2728, 1L);
            st.setCond(10);
        } else if (npcId == 30036 && st.getCond() == 10) {
            htmltext = "potter_q0215_02.htm";
        } else if (npcId == 30036 && st.getCond() == 11) {
            htmltext = "potter_q0215_03.htm";
            st.giveItems(2730, 1L);
            st.takeItems(2728, 1L);
            st.takeItems(2729, 1L);
            st.setCond(12);
        } else if (npcId == 30036 && st.getCond() == 12 && st.getQuestItemsCount(2730) > 0L) {
            htmltext = "potter_q0215_04.htm";
        } else if (npcId == 30362 && st.getCond() == 12) {
            htmltext = "andellria_q0215_01.htm";
            st.setCond(13);
        } else if (npcId == 30362 && st.getCond() == 13) {
            htmltext = "andellria_q0215_02.htm";
        } else if (npcId == 30362 && st.getCond() == 15 && st.getQuestItemsCount(2731) > 0L) {
            htmltext = "andellria_q0215_03.htm";
        } else if (npcId == 30362 && st.getCond() == 16) {
            htmltext = "andellria_q0215_06.htm";
        } else if (npcId == 30362 && st.getCond() == 15 && st.getQuestItemsCount(2731) == 0L) {
            htmltext = "andellria_q0215_07.htm";
        } else if (npcId == 30652 && st.getCond() == 14 && st.getQuestItemsCount(2732) > 0L) {
            htmltext = "uruha_q0215_01.htm";
        } else if (npcId == 30652 && st.getCond() == 15 && st.getQuestItemsCount(2731) > 0L) {
            htmltext = "uruha_q0215_03.htm";
        } else if (npcId == 30612 && st.getCond() == 16) {
            htmltext = "sage_kasian_q0215_01.htm";
            st.giveItems(2722, 1L);
            if (st.getQuestItemsCount(2731) > 0L) {
                st.takeItems(2731, 1L);
            }
            if (st.getQuestItemsCount(2726) > 0L) {
                st.takeItems(2726, 1L);
            }
            st.setCond(17);
            st.takeItems(2727, 1L);
            st.takeItems(2724, 1L);
            st.takeItems(2730, 1L);
        } else if (npcId == 30612 && st.getCond() == 17) {
            htmltext = "sage_kasian_q0215_02.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (npcId == 27116) {
            if (st.getCond() == 3 && st.getQuestItemsCount(2725) == 0L && Rnd.chance(30)) {
                st.giveItems(2725, 1L);
                st.setCond(4);
                st.playSound("ItemSound.quest_middle");
            }
        } else if (npcId == 27117) {
            if (st.getCond() == 10 && st.getQuestItemsCount(2729) == 0L) {
                st.giveItems(2729, 1L);
                st.setCond(11);
                st.playSound("ItemSound.quest_middle");
            }
        } else if (npcId == 27118 && st.getCond() == 13 && st.getQuestItemsCount(2732) == 0L && Rnd.chance(20)) {
            st.giveItems(2732, 1L);
            st.setCond(14);
            st.playSound("ItemSound.quest_middle");
        }
        return null;
    }
}
