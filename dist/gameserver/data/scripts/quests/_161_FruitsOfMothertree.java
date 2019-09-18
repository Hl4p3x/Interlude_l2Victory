package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _161_FruitsOfMothertree extends Quest {
    private static final int ANDELLRIAS_LETTER_ID = 1036;
    private static final int MOTHERTREE_FRUIT_ID = 1037;

    public _161_FruitsOfMothertree() {
        super(false);
        addStartNpc(30362);
        addTalkId(30371);
        addQuestItem(1037, 1036);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            st.set("id", "0");
            htmltext = "30362-04.htm";
            st.giveItems(1036, 1L);
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        if (id == 1) {
            st.setState(2);
            st.setCond(0);
            st.set("id", "0");
        }
        if (npcId == 30362 && st.getCond() == 0) {
            if (st.getCond() < 15) {
                if (st.getPlayer().getRace() != Race.elf) {
                    htmltext = "30362-00.htm";
                } else {
                    if (st.getPlayer().getLevel() >= 3) {
                        return "30362-03.htm";
                    }
                    htmltext = "30362-02.htm";
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "30362-02.htm";
                st.exitCurrentQuest(true);
            }
        } else if (npcId == 30362 && st.getCond() > 0) {
            if (st.getQuestItemsCount(1036) == 1L && st.getQuestItemsCount(1037) == 0L) {
                htmltext = "30362-05.htm";
            } else if (st.getQuestItemsCount(1037) == 1L) {
                htmltext = "30362-06.htm";
                st.giveItems(875, 2L);
                st.addExpAndSp(600L, 0L);
                st.takeItems(1037, 1L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(false);
            }
        } else if (npcId == 30371 && st.getCond() == 1) {
            if (st.getQuestItemsCount(1036) == 1L) {
                if (st.getInt("id") != 161) {
                    st.set("id", "161");
                    htmltext = "30371-01.htm";
                    st.giveItems(1037, 1L);
                    st.takeItems(1036, 1L);
                }
            } else if (st.getQuestItemsCount(1037) == 1L) {
                htmltext = "30371-02.htm";
            }
        }
        return htmltext;
    }
}
