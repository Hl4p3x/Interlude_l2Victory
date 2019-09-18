package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _363_SorrowfulSoundofFlute extends Quest {
    public final int NANARIN = 30956;
    public final int BARBADO = 30959;
    public final int POITAN = 30458;
    public final int HOLVAS = 30058;
    public final int MUSICAL_SCORE = 4420;
    public final int EVENT_CLOTHES = 4318;
    public final int NANARINS_FLUTE = 4319;
    public final int SABRINS_BLACK_BEER = 4320;
    public final int Musical_Score = 4420;

    public _363_SorrowfulSoundofFlute() {
        super(false);
        addStartNpc(30956);
        addTalkId(30956);
        addTalkId(30458);
        addTalkId(30058);
        addTalkId(30959);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30956_2.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.takeItems(4318, -1L);
            st.takeItems(4319, -1L);
            st.takeItems(4320, -1L);
        } else if ("30956_4.htm".equalsIgnoreCase(event)) {
            st.giveItems(4319, 1L);
            st.playSound("ItemSound.quest_middle");
            st.setCond(3);
        } else if ("answer1".equalsIgnoreCase(event)) {
            st.giveItems(4318, 1L);
            st.playSound("ItemSound.quest_middle");
            st.setCond(3);
            htmltext = "30956_6.htm";
        } else if ("answer2".equalsIgnoreCase(event)) {
            st.giveItems(4320, 1L);
            st.playSound("ItemSound.quest_middle");
            st.setCond(3);
            htmltext = "30956_6.htm";
        } else if ("30956_7.htm".equalsIgnoreCase(event)) {
            st.giveItems(4420, 1L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30956) {
            switch (cond) {
                case 0:
                    if (st.getPlayer().getLevel() < 15) {
                        htmltext = "30956-00.htm";
                        st.exitCurrentQuest(true);
                    } else {
                        htmltext = "30956_1.htm";
                    }
                    break;
                case 1:
                    htmltext = "30956_8.htm";
                    break;
                case 2:
                    htmltext = "30956_3.htm";
                    break;
                case 3:
                    htmltext = "30956_6.htm";
                    break;
                case 4:
                    htmltext = "30956_5.htm";
                    break;
            }
        } else if (npcId == 30959) {
            if (cond == 3) {
                if (st.getQuestItemsCount(4318) > 0L) {
                    st.takeItems(4318, -1L);
                    htmltext = "30959_2.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getQuestItemsCount(4320) > 0L) {
                    st.takeItems(4320, -1L);
                    htmltext = "30959_2.htm";
                    st.exitCurrentQuest(true);
                } else {
                    st.takeItems(4319, -1L);
                    st.setCond(4);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "30959_1.htm";
                }
            } else if (cond == 4) {
                htmltext = "30959_3.htm";
            }
        } else if (npcId == 30058 && (cond == 1 || cond == 2)) {
            st.setCond(2);
            if (Rnd.chance(60)) {
                htmltext = "30058_2.htm";
            } else {
                htmltext = "30058_1.htm";
            }
        } else if (npcId == 30458 && (cond == 1 || cond == 2)) {
            st.setCond(2);
            if (Rnd.chance(60)) {
                htmltext = "30458_2.htm";
            } else {
                htmltext = "30458_1.htm";
            }
        }
        return htmltext;
    }
}
