package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _164_BloodFiend extends Quest {
    private static final int Creamees = 30149;
    private static final int KirunakSkull = 1044;
    private static final int Kirunak = 27021;

    public _164_BloodFiend() {
        super(false);
        addStartNpc(30149);
        addTalkId(30149);
        addKillId(27021);
        addQuestItem(1044);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("30149-04.htm".equalsIgnoreCase(event)) {
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
        if (npcId == 30149) {
            switch (cond) {
                case 0:
                    if (st.getPlayer().getRace() == Race.darkelf) {
                        htmltext = "30149-00.htm";
                        st.exitCurrentQuest(true);
                    } else if (st.getPlayer().getLevel() < 21) {
                        htmltext = "30149-02.htm";
                        st.exitCurrentQuest(true);
                    } else {
                        htmltext = "30149-03.htm";
                    }
                    break;
                case 1:
                    htmltext = "30149-05.htm";
                    break;
                case 2:
                    st.takeItems(1044, -1L);
                    st.giveItems(57, 42130L, true);
                    htmltext = "30149-06.htm";
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
        if (cond == 1 && npcId == 27021) {
            if (st.getQuestItemsCount(1044) == 0L) {
                st.giveItems(1044, 1L);
            }
            st.playSound("ItemSound.quest_middle");
            st.setCond(2);
            st.setState(2);
        }
        return null;
    }
}
