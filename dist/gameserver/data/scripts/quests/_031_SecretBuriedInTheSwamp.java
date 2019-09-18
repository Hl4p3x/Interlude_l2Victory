package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _031_SecretBuriedInTheSwamp extends Quest {
    int ABERCROMBIE;
    int FORGOTTEN_MONUMENT_1;
    int FORGOTTEN_MONUMENT_2;
    int FORGOTTEN_MONUMENT_3;
    int FORGOTTEN_MONUMENT_4;
    int CORPSE_OF_DWARF;
    int KRORINS_JOURNAL;

    public _031_SecretBuriedInTheSwamp() {
        super(false);
        ABERCROMBIE = 31555;
        FORGOTTEN_MONUMENT_1 = 31661;
        FORGOTTEN_MONUMENT_2 = 31662;
        FORGOTTEN_MONUMENT_3 = 31663;
        FORGOTTEN_MONUMENT_4 = 31664;
        CORPSE_OF_DWARF = 31665;
        KRORINS_JOURNAL = 7252;
        addStartNpc(ABERCROMBIE);
        for (int i = 31661; i <= 31665; ++i) {
            addTalkId(i);
        }
        addQuestItem(KRORINS_JOURNAL);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int cond = st.getCond();
        if ("31555-1.htm".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("31665-1.htm".equals(event) && cond == 1) {
            st.setCond(2);
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(KRORINS_JOURNAL, 1L);
        } else if ("31555-4.htm".equals(event) && cond == 2) {
            st.setCond(3);
        } else if ("31661-1.htm".equals(event) && cond == 3) {
            st.setCond(4);
        } else if ("31662-1.htm".equals(event) && cond == 4) {
            st.setCond(5);
        } else if ("31663-1.htm".equals(event) && cond == 5) {
            st.setCond(6);
        } else if ("31664-1.htm".equals(event) && cond == 6) {
            st.setCond(7);
            st.playSound("ItemSound.quest_middle");
        } else if ("31555-7.htm".equals(event) && cond == 7) {
            st.takeItems(KRORINS_JOURNAL, -1L);
            st.giveItems(57, 40000L);
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
        if (npcId == ABERCROMBIE) {
            switch (cond) {
                case 0:
                    if (st.getPlayer().getLevel() >= 66) {
                        htmltext = "31555-0.htm";
                    } else {
                        htmltext = "31555-0a.htm";
                        st.exitCurrentQuest(true);
                    }
                    break;
                case 1:
                    htmltext = "31555-2.htm";
                    break;
                case 2:
                    htmltext = "31555-3.htm";
                    break;
                case 3:
                    htmltext = "31555-5.htm";
                    break;
                case 7:
                    htmltext = "31555-6.htm";
                    break;
            }
        } else if (npcId == CORPSE_OF_DWARF) {
            if (cond == 1) {
                htmltext = "31665-0.htm";
            } else if (cond == 2) {
                htmltext = "31665-2.htm";
            }
        } else if (npcId == FORGOTTEN_MONUMENT_1) {
            if (cond == 3) {
                htmltext = "31661-0.htm";
            } else if (cond > 3) {
                htmltext = "31661-2.htm";
            }
        } else if (npcId == FORGOTTEN_MONUMENT_2) {
            if (cond == 4) {
                htmltext = "31662-0.htm";
            } else if (cond > 4) {
                htmltext = "31662-2.htm";
            }
        } else if (npcId == FORGOTTEN_MONUMENT_3) {
            if (cond == 5) {
                htmltext = "31663-0.htm";
            } else if (cond > 5) {
                htmltext = "31663-2.htm";
            }
        } else if (npcId == FORGOTTEN_MONUMENT_4) {
            if (cond == 6) {
                htmltext = "31664-0.htm";
            } else if (cond > 6) {
                htmltext = "31664-2.htm";
            }
        }
        return htmltext;
    }
}
