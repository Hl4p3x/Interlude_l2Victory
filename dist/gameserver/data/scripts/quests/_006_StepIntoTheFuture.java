package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _006_StepIntoTheFuture extends Quest {
    public static final int Roxxy = 30006;
    public static final int Baulro = 30033;
    public static final int Windawood = 30311;
    public static final int BaulrosLetter = 7571;
    public static final int ScrollOfEscapeGiran = 7126;
    public static final int MarkOfTraveler = 7570;

    public _006_StepIntoTheFuture() {
        super(false);
        addStartNpc(Roxxy);
        addTalkId(Baulro, Windawood);
        addQuestItem(BaulrosLetter);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("rapunzel_q0006_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("baul_q0006_0201.htm".equalsIgnoreCase(event)) {
            st.giveItems(BaulrosLetter, 1L, false);
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
        } else if ("sir_collin_windawood_q0006_0301.htm".equalsIgnoreCase(event)) {
            st.takeItems(BaulrosLetter, -1L);
            st.setCond(3);
            st.playSound("ItemSound.quest_middle");
        } else if ("rapunzel_q0006_0401.htm".equalsIgnoreCase(event)) {
            st.giveItems(ScrollOfEscapeGiran, 1L, false);
            st.giveItems(MarkOfTraveler, 1L, false);
            st.unset("cond");
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
        switch (npcId) {
            case Roxxy:
                switch (cond) {
                    case 0:
                        if (st.getPlayer().getRace() == Race.human && st.getPlayer().getLevel() >= 3) {
                            htmltext = "rapunzel_q0006_0101.htm";
                        } else {
                            htmltext = "rapunzel_q0006_0102.htm";
                            st.exitCurrentQuest(true);
                        }
                        break;
                    case 1:
                        htmltext = "rapunzel_q0006_0105.htm";
                        break;
                    case 3:
                        htmltext = "rapunzel_q0006_0301.htm";
                        break;
                }
                break;
            case Baulro:
                if (cond == 1) {
                    htmltext = "baul_q0006_0101.htm";
                } else if (cond == 2 && st.getQuestItemsCount(BaulrosLetter) > 0L) {
                    htmltext = "baul_q0006_0202.htm";
                }
                break;
            case Windawood:
                if (cond == 2 && st.getQuestItemsCount(BaulrosLetter) > 0L) {
                    htmltext = "sir_collin_windawood_q0006_0201.htm";
                } else if (cond == 2 && st.getQuestItemsCount(BaulrosLetter) == 0L) {
                    htmltext = "sir_collin_windawood_q0006_0302.htm";
                } else if (cond == 3) {
                    htmltext = "sir_collin_windawood_q0006_0303.htm";
                }
                break;
        }
        return htmltext;
    }
}
