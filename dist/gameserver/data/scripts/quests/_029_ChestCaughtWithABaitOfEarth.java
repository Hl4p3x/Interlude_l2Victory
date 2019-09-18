package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _029_ChestCaughtWithABaitOfEarth extends Quest {
    int Willie;
    int Anabel;
    int SmallPurpleTreasureChest;
    int SmallGlassBox;
    int PlatedLeatherGloves;

    public _029_ChestCaughtWithABaitOfEarth() {
        super(false);
        Willie = 31574;
        Anabel = 30909;
        SmallPurpleTreasureChest = 6507;
        SmallGlassBox = 7627;
        PlatedLeatherGloves = 2455;
        addStartNpc(Willie);
        addTalkId(Anabel);
        addQuestItem(SmallGlassBox);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "fisher_willeri_q0029_0104.htm":
                st.setState(2);
                st.setCond(1);
                st.playSound("ItemSound.quest_accept");
                break;
            case "fisher_willeri_q0029_0201.htm":
                if (st.getQuestItemsCount(SmallPurpleTreasureChest) > 0L) {
                    st.setCond(2);
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(SmallPurpleTreasureChest, 1L);
                    st.giveItems(SmallGlassBox, 1L);
                } else {
                    htmltext = "fisher_willeri_q0029_0202.htm";
                }
                break;
            case "29_GiveGlassBox":
                if (st.getQuestItemsCount(SmallGlassBox) == 1L) {
                    htmltext = "magister_anabel_q0029_0301.htm";
                    st.takeItems(SmallGlassBox, -1L);
                    st.giveItems(PlatedLeatherGloves, 1L);
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                } else {
                    htmltext = "magister_anabel_q0029_0302.htm";
                    st.exitCurrentQuest(true);
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        final int cond = st.getCond();
        if (npcId == Willie) {
            if (id == 1) {
                if (st.getPlayer().getLevel() < 48) {
                    htmltext = "fisher_willeri_q0029_0102.htm";
                    st.exitCurrentQuest(true);
                } else {
                    final QuestState WilliesSpecialBait = st.getPlayer().getQuestState(_052_WilliesSpecialBait.class);
                    if (WilliesSpecialBait != null) {
                        if (WilliesSpecialBait.isCompleted()) {
                            htmltext = "fisher_willeri_q0029_0101.htm";
                        } else {
                            htmltext = "fisher_willeri_q0029_0102.htm";
                            st.exitCurrentQuest(true);
                        }
                    } else {
                        htmltext = "fisher_willeri_q0029_0103.htm";
                        st.exitCurrentQuest(true);
                    }
                }
            } else if (cond == 1) {
                htmltext = "fisher_willeri_q0029_0105.htm";
                if (st.getQuestItemsCount(SmallPurpleTreasureChest) == 0L) {
                    htmltext = "fisher_willeri_q0029_0106.htm";
                }
            } else if (cond == 2) {
                htmltext = "fisher_willeri_q0029_0203.htm";
            }
        } else if (npcId == Anabel) {
            if (cond == 2) {
                htmltext = "magister_anabel_q0029_0201.htm";
            } else {
                htmltext = "magister_anabel_q0029_0302.htm";
            }
        }
        return htmltext;
    }
}
