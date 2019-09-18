package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.stream.IntStream;

public class _626_ADarkTwilight extends Quest {
    private static final int Hierarch = 31517;
    private static final int BloodOfSaint = 7169;

    public _626_ADarkTwilight() {
        super(true);
        addStartNpc(31517);
        IntStream.rangeClosed(21520, 21542).forEach(this::addKillId);
        addQuestItem(BloodOfSaint);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("dark_presbyter_q0626_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("dark_presbyter_q0626_0201.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(BloodOfSaint) < 300L) {
                htmltext = "dark_presbyter_q0626_0203.htm";
            }
        } else if ("rew_exp".equalsIgnoreCase(event)) {
            st.takeItems(BloodOfSaint, -1L);
            st.addExpAndSp(162773L, 12500L);
            htmltext = "dark_presbyter_q0626_0202.htm";
            st.exitCurrentQuest(true);
        } else if ("rew_adena".equalsIgnoreCase(event)) {
            st.takeItems(BloodOfSaint, -1L);
            st.giveItems(57, 100000L, true);
            htmltext = "dark_presbyter_q0626_0202.htm";
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        final int npcId = npc.getNpcId();
        if (npcId == 31517) {
            switch (cond) {
                case 0:
                    if (st.getPlayer().getLevel() < 60) {
                        htmltext = "dark_presbyter_q0626_0103.htm";
                        st.exitCurrentQuest(true);
                    } else {
                        htmltext = "dark_presbyter_q0626_0101.htm";
                    }
                    break;
                case 1:
                    htmltext = "dark_presbyter_q0626_0106.htm";
                    break;
                case 2:
                    htmltext = "dark_presbyter_q0626_0105.htm";
                    break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1 && Rnd.chance(70)) {
            st.giveItems(BloodOfSaint, 1L);
            if (st.getQuestItemsCount(BloodOfSaint) == 300L) {
                st.setCond(2);
            }
        }
        return null;
    }
}
