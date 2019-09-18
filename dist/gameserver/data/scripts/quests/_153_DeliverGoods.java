package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _153_DeliverGoods extends Quest {
    int DELIVERY_LIST;
    int HEAVY_WOOD_BOX;
    int CLOTH_BUNDLE;
    int CLAY_POT;
    int JACKSONS_RECEIPT;
    int SILVIAS_RECEIPT;
    int RANTS_RECEIPT;
    int RING_OF_KNOWLEDGE;

    public _153_DeliverGoods() {
        super(false);
        DELIVERY_LIST = 1012;
        HEAVY_WOOD_BOX = 1013;
        CLOTH_BUNDLE = 1014;
        CLAY_POT = 1015;
        JACKSONS_RECEIPT = 1016;
        SILVIAS_RECEIPT = 1017;
        RANTS_RECEIPT = 1018;
        RING_OF_KNOWLEDGE = 875;
        addStartNpc(30041);
        addTalkId(30002);
        addTalkId(30003);
        addTalkId(30054);
        addQuestItem(HEAVY_WOOD_BOX, CLOTH_BUNDLE, CLAY_POT, DELIVERY_LIST, JACKSONS_RECEIPT, SILVIAS_RECEIPT, RANTS_RECEIPT);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30041-04.htm".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            if (st.getQuestItemsCount(DELIVERY_LIST) == 0L) {
                st.giveItems(DELIVERY_LIST, 1L);
            }
            if (st.getQuestItemsCount(HEAVY_WOOD_BOX) == 0L) {
                st.giveItems(HEAVY_WOOD_BOX, 1L);
            }
            if (st.getQuestItemsCount(CLOTH_BUNDLE) == 0L) {
                st.giveItems(CLOTH_BUNDLE, 1L);
            }
            if (st.getQuestItemsCount(CLAY_POT) == 0L) {
                st.giveItems(CLAY_POT, 1L);
            }
            htmltext = "30041-04.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (npcId) {
            case 30041:
                if (cond == 0) {
                    if (st.getPlayer().getLevel() >= 2) {
                        htmltext = "30041-03.htm";
                        return htmltext;
                    }
                    htmltext = "30041-02.htm";
                    st.exitCurrentQuest(true);
                } else if (cond == 1 && st.getQuestItemsCount(JACKSONS_RECEIPT) + st.getQuestItemsCount(SILVIAS_RECEIPT) + st.getQuestItemsCount(RANTS_RECEIPT) == 0L) {
                    htmltext = "30041-05.htm";
                } else if (cond == 1 && st.getQuestItemsCount(JACKSONS_RECEIPT) + st.getQuestItemsCount(SILVIAS_RECEIPT) + st.getQuestItemsCount(RANTS_RECEIPT) == 3L) {
                    st.giveItems(RING_OF_KNOWLEDGE, 1L);
                    st.takeItems(DELIVERY_LIST, -1L);
                    st.takeItems(JACKSONS_RECEIPT, -1L);
                    st.takeItems(SILVIAS_RECEIPT, -1L);
                    st.takeItems(RANTS_RECEIPT, -1L);
                    st.addExpAndSp(600L, 0L);
                    st.playSound("ItemSound.quest_finish");
                    htmltext = "30041-06.htm";
                    st.exitCurrentQuest(false);
                }
                break;
            case 30002:
                if (cond == 1 && st.getQuestItemsCount(HEAVY_WOOD_BOX) == 1L) {
                    st.takeItems(HEAVY_WOOD_BOX, -1L);
                    if (st.getQuestItemsCount(JACKSONS_RECEIPT) == 0L) {
                        st.giveItems(JACKSONS_RECEIPT, 1L);
                    }
                    htmltext = "30002-01.htm";
                } else if (cond == 1 && st.getQuestItemsCount(JACKSONS_RECEIPT) > 0L) {
                    htmltext = "30002-02.htm";
                }
                break;
            case 30003:
                if (cond == 1 && st.getQuestItemsCount(CLOTH_BUNDLE) == 1L) {
                    st.takeItems(CLOTH_BUNDLE, -1L);
                    if (st.getQuestItemsCount(SILVIAS_RECEIPT) == 0L) {
                        st.giveItems(SILVIAS_RECEIPT, 1L);
                        if (st.getPlayer().getClassId().isMage()) {
                            st.giveItems(2509, 3L);
                        } else {
                            st.giveItems(1835, 6L);
                        }
                    }
                    htmltext = "30003-01.htm";
                } else if (cond == 1 && st.getQuestItemsCount(SILVIAS_RECEIPT) > 0L) {
                    htmltext = "30003-02.htm";
                }
                break;
            case 30054:
                if (cond == 1 && st.getQuestItemsCount(CLAY_POT) == 1L) {
                    st.takeItems(CLAY_POT, -1L);
                    if (st.getQuestItemsCount(RANTS_RECEIPT) == 0L) {
                        st.giveItems(RANTS_RECEIPT, 1L);
                    }
                    htmltext = "30054-01.htm";
                } else if (cond == 1 && st.getQuestItemsCount(RANTS_RECEIPT) > 0L) {
                    htmltext = "30054-02.htm";
                }
                break;
        }
        return htmltext;
    }
}
