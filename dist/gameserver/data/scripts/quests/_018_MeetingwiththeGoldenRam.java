package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _018_MeetingwiththeGoldenRam extends Quest {
    private static final int SUPPLY_BOX = 7245;

    public _018_MeetingwiththeGoldenRam() {
        super(false);
        addStartNpc(31314);
        addTalkId(31314);
        addTalkId(31315);
        addTalkId(31555);
        addQuestItem(7245);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        switch (event) {
            case "warehouse_chief_donal_q0018_0104.htm":
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                break;
            case "freighter_daisy_q0018_0201.htm":
                st.setCond(2);
                st.giveItems(7245, 1L);
                st.playSound("ItemSound.quest_accept");
                break;
            case "supplier_abercrombie_q0018_0301.htm":
                st.takeItems(7245, -1L);
                st.giveItems(57, 15000L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(false);
                break;
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 31314) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 66) {
                    htmltext = "warehouse_chief_donal_q0018_0101.htm";
                } else {
                    htmltext = "warehouse_chief_donal_q0018_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "warehouse_chief_donal_q0018_0105.htm";
            }
        } else if (npcId == 31315) {
            if (cond == 1) {
                htmltext = "freighter_daisy_q0018_0101.htm";
            } else if (cond == 2) {
                htmltext = "freighter_daisy_q0018_0202.htm";
            }
        } else if (npcId == 31555 && cond == 2 && st.getQuestItemsCount(7245) == 1L) {
            htmltext = "supplier_abercrombie_q0018_0201.htm";
        }
        return htmltext;
    }
}
