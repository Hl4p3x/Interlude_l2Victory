package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _154_SacrificeToSea extends Quest {
    private static final int FOX_FUR_ID = 1032;
    private static final int FOX_FUR_YARN_ID = 1033;
    private static final int MAIDEN_DOLL_ID = 1034;
    private static final int MYSTICS_EARRING_ID = 113;

    public _154_SacrificeToSea() {
        super(false);
        addStartNpc(30312);
        addTalkId(30051);
        addTalkId(30055);
        addKillId(20481);
        addKillId(20544);
        addKillId(20545);
        addQuestItem(1032, 1033, 1034);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            st.set("id", "0");
            htmltext = "30312-04.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        if (id == 1) {
            st.setState(2);
            st.setCond(0);
            st.set("id", "0");
        }
        if (npcId == 30312 && st.getCond() == 0) {
            if (st.getCond() < 15) {
                if (st.getPlayer().getLevel() >= 2) {
                    htmltext = "30312-03.htm";
                    return htmltext;
                }
                htmltext = "30312-02.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "30312-02.htm";
                st.exitCurrentQuest(true);
            }
        } else if (npcId == 30312 && st.getCond() == 0) {
            htmltext = "completed";
        } else if (npcId == 30312 && st.getCond() == 1 && st.getQuestItemsCount(1033) == 0L && st.getQuestItemsCount(1034) == 0L && st.getQuestItemsCount(1032) < 10L) {
            htmltext = "30312-05.htm";
        } else if (npcId == 30312 && st.getCond() == 1 && st.getQuestItemsCount(1032) >= 10L) {
            htmltext = "30312-08.htm";
        } else if (npcId == 30051 && st.getCond() == 1 && st.getQuestItemsCount(1032) < 10L && st.getQuestItemsCount(1032) > 0L) {
            htmltext = "30051-01.htm";
        } else if (npcId == 30051 && st.getCond() == 1 && st.getQuestItemsCount(1032) >= 10L && st.getQuestItemsCount(1033) == 0L && st.getQuestItemsCount(1034) == 0L && st.getQuestItemsCount(1034) < 10L) {
            htmltext = "30051-02.htm";
            st.giveItems(1033, 1L);
            st.takeItems(1032, st.getQuestItemsCount(1032));
        } else if (npcId == 30051 && st.getCond() == 1 && st.getQuestItemsCount(1033) >= 1L) {
            htmltext = "30051-03.htm";
        } else if (npcId == 30051 && st.getCond() == 1 && st.getQuestItemsCount(1034) == 1L) {
            htmltext = "30051-04.htm";
        } else if (npcId == 30312 && st.getCond() == 1 && st.getQuestItemsCount(1033) >= 1L) {
            htmltext = "30312-06.htm";
        } else if (npcId == 30055 && st.getCond() == 1 && st.getQuestItemsCount(1033) >= 1L) {
            htmltext = "30055-01.htm";
            st.giveItems(1034, 1L);
            st.takeItems(1033, st.getQuestItemsCount(1033));
        } else if (npcId == 30055 && st.getCond() == 1 && st.getQuestItemsCount(1034) >= 1L) {
            htmltext = "30055-02.htm";
        } else if (npcId == 30055 && st.getCond() == 1 && st.getQuestItemsCount(1033) == 0L && st.getQuestItemsCount(1034) == 0L) {
            htmltext = "30055-03.htm";
        } else if (npcId == 30312 && st.getCond() == 1 && st.getQuestItemsCount(1034) >= 1L && st.getInt("id") != 154) {
            st.set("id", "154");
            htmltext = "30312-07.htm";
            st.takeItems(1034, st.getQuestItemsCount(1034));
            st.giveItems(113, 1L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1 && st.getQuestItemsCount(1033) == 0L) {
            st.rollAndGive(1032, 1, 1, 10, 14.0);
        }
        return null;
    }
}
