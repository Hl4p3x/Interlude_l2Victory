package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _011_SecretMeetingWithKetraOrcs extends Quest {
    private final int CADMON = 31296;
    private final int LEON = 31256;
    private final int WAHKAN = 31371;
    private final int MUNITIONS_BOX = 7231;

    public _011_SecretMeetingWithKetraOrcs() {
        super(false);
        addStartNpc(CADMON);
        addTalkId(LEON, WAHKAN);
        addQuestItem(MUNITIONS_BOX);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("guard_cadmon_q0011_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("trader_leon_q0011_0201.htm".equalsIgnoreCase(event)) {
            st.giveItems(MUNITIONS_BOX, 1L);
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
        } else if ("herald_wakan_q0011_0301.htm".equalsIgnoreCase(event)) {
            st.takeItems(MUNITIONS_BOX, 1L);
            st.addExpAndSp(82045L, 6047L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == CADMON) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 74) {
                    htmltext = "guard_cadmon_q0011_0101.htm";
                } else {
                    htmltext = "guard_cadmon_q0011_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "guard_cadmon_q0011_0105.htm";
            }
        } else if (npcId == LEON) {
            if (cond == 1) {
                htmltext = "trader_leon_q0011_0101.htm";
            } else if (cond == 2) {
                htmltext = "trader_leon_q0011_0202.htm";
            }
        } else if (npcId == WAHKAN && cond == 2 && st.getQuestItemsCount(MUNITIONS_BOX) > 0L) {
            htmltext = "herald_wakan_q0011_0201.htm";
        }
        return htmltext;
    }
}
