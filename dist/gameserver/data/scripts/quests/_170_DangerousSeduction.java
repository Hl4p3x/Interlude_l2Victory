package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _170_DangerousSeduction extends Quest {
    private static final int Vellior = 30305;
    private static final int NightmareCrystal = 1046;
    private static final int Merkenis = 27022;

    public _170_DangerousSeduction() {
        super(false);
        addStartNpc(30305);
        addTalkId(30305);
        addKillId(27022);
        addQuestItem(1046);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("30305-04.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30305) {
            switch (cond) {
                case 0:
                    if (st.getPlayer().getRace() != Race.darkelf) {
                        htmltext = "30305-00.htm";
                        st.exitCurrentQuest(true);
                    } else if (st.getPlayer().getLevel() < 21) {
                        htmltext = "30305-02.htm";
                        st.exitCurrentQuest(true);
                    } else {
                        htmltext = "30305-03.htm";
                    }
                    break;
                case 1:
                    htmltext = "30305-05.htm";
                    break;
                case 2:
                    st.takeItems(1046, -1L);
                    st.giveItems(57, 102680L, true);
                    htmltext = "30305-06.htm";
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                    break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (cond == 1 && npcId == 27022) {
            if (st.getQuestItemsCount(1046) == 0L) {
                st.giveItems(1046, 1L);
            }
            st.playSound("ItemSound.quest_middle");
            st.setCond(2);
            st.setState(2);
        }
        return null;
    }
}
