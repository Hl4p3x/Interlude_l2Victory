package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _039_RedEyedInvaders extends Quest {
    int BBN;
    int RBN;
    int IP;
    int GML;
    int[] REW;

    public _039_RedEyedInvaders() {
        super(false);
        BBN = 7178;
        RBN = 7179;
        IP = 7180;
        GML = 7181;
        REW = new int[]{6521, 6529, 6535};
        addStartNpc(30334);
        addTalkId(30332);
        addKillId(20919);
        addKillId(20920);
        addKillId(20921);
        addKillId(20925);
        addQuestItem(BBN, IP, RBN, GML);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "guard_babenco_q0039_0104.htm":
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                break;
            case "captain_bathia_q0039_0201.htm":
                st.setCond(2);
                st.playSound("ItemSound.quest_accept");
                break;
            case "captain_bathia_q0039_0301.htm":
                if (st.getQuestItemsCount(BBN) == 100L && st.getQuestItemsCount(RBN) == 100L) {
                    st.setCond(4);
                    st.takeItems(BBN, -1L);
                    st.takeItems(RBN, -1L);
                    st.playSound("ItemSound.quest_accept");
                } else {
                    htmltext = "captain_bathia_q0039_0203.htm";
                }
                break;
            case "captain_bathia_q0039_0401.htm":
                if (st.getQuestItemsCount(IP) == 30L && st.getQuestItemsCount(GML) == 30L) {
                    st.takeItems(IP, -1L);
                    st.takeItems(GML, -1L);
                    st.giveItems(REW[0], 60L);
                    st.giveItems(REW[1], 1L);
                    st.giveItems(REW[2], 500L);
                    st.setCond(0);
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                } else {
                    htmltext = "captain_bathia_q0039_0304.htm";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30334) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() < 20) {
                    htmltext = "guard_babenco_q0039_0102.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getPlayer().getLevel() >= 20) {
                    htmltext = "guard_babenco_q0039_0101.htm";
                }
            } else if (cond == 1) {
                htmltext = "guard_babenco_q0039_0105.htm";
            }
        } else if (npcId == 30332) {
            if (cond == 1) {
                htmltext = "captain_bathia_q0039_0101.htm";
            } else if (cond == 2 && (st.getQuestItemsCount(BBN) < 100L || st.getQuestItemsCount(RBN) < 100L)) {
                htmltext = "captain_bathia_q0039_0203.htm";
            } else if (cond == 3 && st.getQuestItemsCount(BBN) == 100L && st.getQuestItemsCount(RBN) == 100L) {
                htmltext = "captain_bathia_q0039_0202.htm";
            } else if (cond == 4 && (st.getQuestItemsCount(IP) < 30L || st.getQuestItemsCount(GML) < 30L)) {
                htmltext = "captain_bathia_q0039_0304.htm";
            } else if (cond == 5 && st.getQuestItemsCount(IP) == 30L && st.getQuestItemsCount(GML) == 30L) {
                htmltext = "captain_bathia_q0039_0303.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (cond == 2) {
            if ((npcId == 20919 || npcId == 20920) && st.getQuestItemsCount(BBN) <= 99L) {
                st.giveItems(BBN, 1L);
            } else if (npcId == 20921 && st.getQuestItemsCount(RBN) <= 99L) {
                st.giveItems(RBN, 1L);
            }
            st.playSound("ItemSound.quest_itemget");
            if (st.getQuestItemsCount(BBN) + st.getQuestItemsCount(RBN) == 200L) {
                st.setCond(3);
                st.playSound("ItemSound.quest_middle");
            }
        }
        if (cond == 4) {
            if ((npcId == 20920 || npcId == 20921) && st.getQuestItemsCount(IP) <= 29L) {
                st.giveItems(IP, 1L);
            } else if (npcId == 20925 && st.getQuestItemsCount(GML) <= 29L) {
                st.giveItems(GML, 1L);
            }
            st.playSound("ItemSound.quest_itemget");
            if (st.getQuestItemsCount(IP) + st.getQuestItemsCount(GML) == 60L) {
                st.setCond(5);
                st.playSound("ItemSound.quest_middle");
            }
        }
        return null;
    }
}
