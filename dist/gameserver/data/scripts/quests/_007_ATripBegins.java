package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _007_ATripBegins extends Quest {
    private final int MIRABEL = 30146;
    private final int ARIEL = 30148;
    private final int ASTERIOS = 30154;
    private final int ARIELS_RECOMMENDATION = 7572;
    private final int SCROLL_OF_ESCAPE_GIRAN = 7126;
    private final int MARK_OF_TRAVELER = 7570;

    public _007_ATripBegins() {
        super(false);
        addStartNpc(MIRABEL);
        addTalkId(MIRABEL, ARIEL, ASTERIOS);
        addQuestItem(ARIELS_RECOMMENDATION);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("mint_q0007_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("ariel_q0007_0201.htm".equalsIgnoreCase(event)) {
            st.giveItems(ARIELS_RECOMMENDATION, 1L);
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
        } else if ("ozzy_q0007_0301.htm".equalsIgnoreCase(event)) {
            st.takeItems(ARIELS_RECOMMENDATION, -1L);
            st.setCond(3);
            st.playSound("ItemSound.quest_middle");
        } else if ("mint_q0007_0401.htm".equalsIgnoreCase(event)) {
            st.giveItems(SCROLL_OF_ESCAPE_GIRAN, 1L);
            st.giveItems(MARK_OF_TRAVELER, 1L);
            st.setCond(0);
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
        if (npcId == MIRABEL) {
            switch (cond) {
                case 0:
                    if (st.getPlayer().getRace() == Race.elf && st.getPlayer().getLevel() >= 3) {
                        htmltext = "mint_q0007_0101.htm";
                    } else {
                        htmltext = "mint_q0007_0102.htm";
                        st.exitCurrentQuest(true);
                    }
                    break;
                case 1:
                    htmltext = "mint_q0007_0105.htm";
                    break;
                case 3:
                    htmltext = "mint_q0007_0301.htm";
                    break;
            }
        } else if (npcId == ARIEL) {
            if (cond == 1 && st.getQuestItemsCount(ARIELS_RECOMMENDATION) == 0L) {
                htmltext = "ariel_q0007_0101.htm";
            } else if (cond == 2) {
                htmltext = "ariel_q0007_0202.htm";
            }
        } else if (npcId == ASTERIOS) {
            if (cond == 2 && st.getQuestItemsCount(ARIELS_RECOMMENDATION) > 0L) {
                htmltext = "ozzy_q0007_0201.htm";
            } else if (cond == 2 && st.getQuestItemsCount(ARIELS_RECOMMENDATION) == 0L) {
                htmltext = "ozzy_q0007_0302.htm";
            } else if (cond == 3) {
                htmltext = "ozzy_q0007_0303.htm";
            }
        }
        return htmltext;
    }
}
