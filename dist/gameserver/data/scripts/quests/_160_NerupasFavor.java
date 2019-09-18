package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _160_NerupasFavor extends Quest {
    private static final int SILVERY_SPIDERSILK = 1026;
    private static final int UNOS_RECEIPT = 1027;
    private static final int CELS_TICKET = 1028;
    private static final int NIGHTSHADE_LEAF = 1029;
    private static final int LESSER_HEALING_POTION = 1060;
    private static final int NERUPA = 30370;
    private static final int UNOREN = 30147;
    private static final int CREAMEES = 30149;
    private static final int JULIA = 30152;
    private static final int COND1 = 1;
    private static final int COND2 = 2;
    private static final int COND3 = 3;
    private static final int COND4 = 4;

    public _160_NerupasFavor() {
        super(false);
        addStartNpc(NERUPA);
        addTalkId(UNOREN, CREAMEES, JULIA);
        addQuestItem(SILVERY_SPIDERSILK, UNOS_RECEIPT, CELS_TICKET, NIGHTSHADE_LEAF);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("30370-04.htm".equals(event)) {
            st.setCond(COND1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(SILVERY_SPIDERSILK, 1L);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (npcId) {
            case NERUPA:
                if (st.getState() == 1) {
                    if (st.getPlayer().getRace() != Race.elf) {
                        htmltext = "30370-00.htm";
                    } else if (st.getPlayer().getLevel() < 3) {
                        htmltext = "30370-02.htm";
                        st.exitCurrentQuest(true);
                    } else {
                        htmltext = "30370-03.htm";
                    }
                } else if (cond == COND1) {
                    htmltext = "30370-04.htm";
                } else if (cond == COND4 && st.getQuestItemsCount(NIGHTSHADE_LEAF) > 0L) {
                    st.takeItems(NIGHTSHADE_LEAF, -1L);
                    st.giveItems(LESSER_HEALING_POTION, 5L);
                    st.addExpAndSp(1000L, 0L);
                    st.playSound("ItemSound.quest_finish");
                    htmltext = "30370-06.htm";
                    st.exitCurrentQuest(false);
                } else {
                    htmltext = "30370-05.htm";
                }
                break;
            case UNOREN:
                switch (cond) {
                    case COND1:
                        st.takeItems(SILVERY_SPIDERSILK, -1L);
                        st.giveItems(UNOS_RECEIPT, 1L);
                        st.setCond(COND2);
                        htmltext = "30147-01.htm";
                        break;
                    case COND2:
                    case COND3:
                        htmltext = "30147-02.htm";
                        break;
                    case COND4:
                        htmltext = "30147-03.htm";
                        break;
                }
                break;
            case CREAMEES:
                switch (cond) {
                    case COND2:
                        st.takeItems(UNOS_RECEIPT, -1L);
                        st.giveItems(CELS_TICKET, 1L);
                        st.setCond(COND3);
                        htmltext = "30149-01.htm";
                        break;
                    case COND3:
                        htmltext = "30149-02.htm";
                        break;
                    case COND4:
                        htmltext = "30149-03.htm";
                        break;
                }
                break;
            case JULIA:
                if (cond == COND3) {
                    st.takeItems(CELS_TICKET, -1L);
                    st.giveItems(NIGHTSHADE_LEAF, 1L);
                    htmltext = "30152-01.htm";
                    st.setCond(COND4);
                } else if (cond == COND4) {
                    htmltext = "30152-02.htm";
                }
                break;
        }
        return htmltext;
    }
}
