package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _432_BirthdayPartySong extends Quest {
    private static final int MELODY_MAESTRO_OCTAVIA = 31043;
    private static final int ROUGH_HEWN_ROCK_GOLEMS = 21103;
    private static final int RED_CRYSTALS = 7541;
    private static final int BIRTHDAY_ECHO_CRYSTAL = 7061;

    public _432_BirthdayPartySong() {
        super(false);
        addStartNpc(MELODY_MAESTRO_OCTAVIA);
        addKillId(ROUGH_HEWN_ROCK_GOLEMS);
        addQuestItem(RED_CRYSTALS);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("muzyko_q0432_0104.htm".equalsIgnoreCase(event)) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("muzyko_q0432_0201.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(RED_CRYSTALS) == 50L) {
                st.takeItems(RED_CRYSTALS, -1L);
                st.giveItems(BIRTHDAY_ECHO_CRYSTAL, 25L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            } else {
                htmltext = "muzyko_q0432_0202.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int condition = st.getCond();
        final int npcId = npc.getNpcId();
        if (npcId == MELODY_MAESTRO_OCTAVIA) {
            if (condition == 0) {
                if (st.getPlayer().getLevel() >= 31) {
                    htmltext = "muzyko_q0432_0101.htm";
                } else {
                    htmltext = "muzyko_q0432_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (condition == 1) {
                htmltext = "muzyko_q0432_0106.htm";
            } else if (condition == 2 && st.getQuestItemsCount(RED_CRYSTALS) == 50L) {
                htmltext = "muzyko_q0432_0105.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() != 2) {
            return null;
        }
        final int npcId = npc.getNpcId();
        if (npcId == ROUGH_HEWN_ROCK_GOLEMS && st.getCond() == 1 && st.getQuestItemsCount(RED_CRYSTALS) < 50L) {
            st.giveItems(RED_CRYSTALS, 1L);
            if (st.getQuestItemsCount(RED_CRYSTALS) == 50L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
