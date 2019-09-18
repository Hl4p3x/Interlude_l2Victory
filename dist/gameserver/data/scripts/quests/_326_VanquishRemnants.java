package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _326_VanquishRemnants extends Quest {
    private static final int Leopold = 30435;
    private static final int RedCrossBadge = 1359;
    private static final int BlueCrossBadge = 1360;
    private static final int BlackCrossBadge = 1361;
    private static final int BlackLionMark = 1369;
    private static final int OlMahumPatrol = 30425;
    private static final int OlMahumGuard = 20058;
    private static final int OlMahumStraggler = 20061;
    private static final int OlMahumShooter = 20063;
    private static final int OlMahumCaptain = 20066;
    private static final int OlMahumCommander = 20076;
    private static final int OlMahumSupplier = 20436;
    private static final int OlMahumRecruit = 20437;
    private static final int OlMahumGeneral = 20438;
    public final int[][] DROPLIST_COND;

    public _326_VanquishRemnants() {
        super(false);
        DROPLIST_COND = new int[][]{{30425, 1359}, {20058, 1359}, {20437, 1359}, {20061, 1360}, {20063, 1360}, {20436, 1360}, {20066, 1361}, {20438, 1361}, {20076, 1361}};
        addStartNpc(30435);
        addTalkId(30435);
        for (int[] aDROPLIST_COND : DROPLIST_COND) {
            addKillId(aDROPLIST_COND[0]);
        }
        addQuestItem(1359);
        addQuestItem(1360);
        addQuestItem(1361);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("leopold_q0326_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("leopold_q0326_03.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30435) {
            if (st.getPlayer().getLevel() < 21) {
                htmltext = "leopold_q0326_01.htm";
                st.exitCurrentQuest(true);
            } else if (cond == 0) {
                htmltext = "leopold_q0326_02.htm";
            } else if (cond == 1 && st.getQuestItemsCount(1359) == 0L && st.getQuestItemsCount(1360) == 0L && st.getQuestItemsCount(1361) == 0L) {
                htmltext = "leopold_q0326_04.htm";
            } else if (cond == 1) {
                if (st.getQuestItemsCount(1359) + st.getQuestItemsCount(1360) + st.getQuestItemsCount(1361) >= 100L) {
                    if (st.getQuestItemsCount(1369) == 0L) {
                        htmltext = "leopold_q0326_09.htm";
                        st.giveItems(1369, 1L);
                    } else {
                        htmltext = "leopold_q0326_06.htm";
                    }
                } else {
                    htmltext = "leopold_q0326_05.htm";
                }
                st.giveItems(57, st.getQuestItemsCount(1359) * 46L + st.getQuestItemsCount(1360) * 52L + st.getQuestItemsCount(1361) * 58L, true);
                st.takeItems(1359, -1L);
                st.takeItems(1360, -1L);
                st.takeItems(1361, -1L);
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() == 2) {
            for (int[] aDROPLIST_COND : DROPLIST_COND) {
                if (npc.getNpcId() == aDROPLIST_COND[0]) {
                    st.giveItems(aDROPLIST_COND[1], 1L);
                }
            }
        }
        return null;
    }
}
