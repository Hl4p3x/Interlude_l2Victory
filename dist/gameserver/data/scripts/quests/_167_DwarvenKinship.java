package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _167_DwarvenKinship extends Quest {
    private static final int Carlon = 30350;
    private static final int Haprock = 30255;
    private static final int Norman = 30210;
    private static final int CarlonsLetter = 1076;
    private static final int NormansLetter = 1106;

    public _167_DwarvenKinship() {
        super(false);
        addStartNpc(30350);
        addTalkId(30255);
        addTalkId(30210);
        addQuestItem(1076, 1106);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("30350-04.htm".equalsIgnoreCase(event)) {
            st.giveItems(1076, 1L);
            st.playSound("ItemSound.quest_accept");
            st.setCond(1);
            st.setState(2);
        } else if ("30255-03.htm".equalsIgnoreCase(event)) {
            st.takeItems(1076, -1L);
            st.giveItems(57, 2000L);
            st.giveItems(1106, 1L);
            st.setCond(2);
            st.setState(2);
        } else if ("30255-04.htm".equalsIgnoreCase(event)) {
            st.takeItems(1076, -1L);
            st.giveItems(57, 2000L);
            st.playSound("ItemSound.quest_giveup");
            st.exitCurrentQuest(false);
        } else if ("30210-02.htm".equalsIgnoreCase(event)) {
            st.takeItems(1106, -1L);
            st.giveItems(57, 22000L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30350) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 15) {
                    htmltext = "30350-03.htm";
                } else {
                    htmltext = "30350-02.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond > 0) {
                htmltext = "30350-05.htm";
            }
        } else if (npcId == 30255) {
            if (cond == 1) {
                htmltext = "30255-01.htm";
            } else if (cond > 1) {
                htmltext = "30255-05.htm";
            }
        } else if (npcId == 30210 && cond == 2) {
            htmltext = "30210-01.htm";
        }
        return htmltext;
    }
}
