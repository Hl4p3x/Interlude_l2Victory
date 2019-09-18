package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _364_JovialAccordion extends Quest {
    private static final int BARBADO = 30959;
    private static final int SWAN = 30957;
    private static final int SABRIN = 30060;
    private static final int BEER_CHEST = 30960;
    private static final int CLOTH_CHEST = 30961;
    private static final int KEY_1 = 4323;
    private static final int KEY_2 = 4324;
    private static final int BEER = 4321;
    private static final int ECHO = 4421;

    public _364_JovialAccordion() {
        super(false);
        addStartNpc(BARBADO);
        addTalkId(SWAN);
        addTalkId(SABRIN);
        addTalkId(BEER_CHEST);
        addTalkId(CLOTH_CHEST);
        addQuestItem(KEY_1);
        addQuestItem(KEY_2);
        addQuestItem(BEER);
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        if (st.getState() == 1) {
            if (npcId != BARBADO) {
                return htmltext;
            }
            st.setCond(0);
            st.set("ok", "0");
        }
        final int cond = st.getCond();
        if (npcId == BARBADO) {
            if (cond == 0) {
                htmltext = "30959-01.htm";
            } else if (cond == 3) {
                htmltext = "30959-03.htm";
                st.giveItems(ECHO, 1L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            } else if (cond > 0) {
                htmltext = "30959-02.htm";
            }
        } else if (npcId == SWAN) {
            switch (cond) {
                case 1:
                    htmltext = "30957-01.htm";
                    break;
                case 3:
                    htmltext = "30957-05.htm";
                    break;
                case 2:
                    if (st.getInt("ok") == 1 && st.getQuestItemsCount(KEY_1) == 0L) {
                        st.setCond(3);
                        htmltext = "30957-04.htm";
                    } else {
                        htmltext = "30957-03.htm";
                    }
                    break;
            }
        } else if (npcId == SABRIN && cond == 2 && st.getQuestItemsCount(BEER) > 0L) {
            st.set("ok", "1");
            st.takeItems(BEER, -1L);
            htmltext = "30060-01.htm";
        } else if (npcId == BEER_CHEST && cond == 2) {
            htmltext = "30960-01.htm";
        } else if (npcId == CLOTH_CHEST && cond == 2) {
            htmltext = "30961-01.htm";
        }
        return htmltext;
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int _state = st.getState();
        final int cond = st.getCond();
        if ("30959-02.htm".equalsIgnoreCase(event) && _state == 1 && cond == 0) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("30957-02.htm".equalsIgnoreCase(event) && _state == 2 && cond == 1) {
            st.setCond(2);
            st.giveItems(KEY_1, 1L);
            st.giveItems(KEY_2, 1L);
        } else if ("30960-03.htm".equalsIgnoreCase(event) && cond == 2 && st.getQuestItemsCount(KEY_2) > 0L) {
            st.takeItems(KEY_2, -1L);
            st.giveItems(BEER, 1L);
            htmltext = "30960-02.htm";
        } else if ("30961-03.htm".equalsIgnoreCase(event) && cond == 2 && st.getQuestItemsCount(KEY_1) > 0L) {
            st.takeItems(KEY_1, -1L);
            htmltext = "30961-02.htm";
        }
        return htmltext;
    }

    
}
