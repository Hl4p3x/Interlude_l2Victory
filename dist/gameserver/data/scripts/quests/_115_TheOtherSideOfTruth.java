package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _115_TheOtherSideOfTruth extends Quest {
    private static final int Rafforty = 32020;
    private static final int Misa = 32018;
    private static final int Kierre = 32022;
    private static final int Ice_Sculpture1 = 32021;
    private static final int Ice_Sculpture2 = 32077;
    private static final int Ice_Sculpture3 = 32078;
    private static final int Ice_Sculpture4 = 32079;
    private static final int Misas_Letter = 8079;
    private static final int Raffortys_Letter = 8080;
    private static final int Piece_of_Tablet = 8081;
    private static final int Report_Piece = 8082;

    public _115_TheOtherSideOfTruth() {
        super(false);
        addStartNpc(Rafforty);
        addTalkId(Misa);
        addTalkId(Kierre);
        addTalkId(Ice_Sculpture1);
        addTalkId(Ice_Sculpture2);
        addTalkId(Ice_Sculpture3);
        addTalkId(Ice_Sculpture4);
        addQuestItem(Misas_Letter);
        addQuestItem(Raffortys_Letter);
        addQuestItem(Piece_of_Tablet);
        addQuestItem(Report_Piece);
    }

    @Override
    public String onEvent(String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        if ("32020-02.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        }
        if (_state != 2) {
            return event;
        }
        if ("32020-06.htm".equalsIgnoreCase(event) || "32020-08a.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("32020-05.htm".equalsIgnoreCase(event)) {
            st.setCond(3);
            st.takeItems(Misas_Letter, 1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("32020-08.htm".equalsIgnoreCase(event) || "32020-07a.htm".equalsIgnoreCase(event)) {
            st.setCond(4);
            st.playSound("ItemSound.quest_middle");
        } else if ("32020-12.htm".equalsIgnoreCase(event)) {
            st.setCond(5);
            st.playSound("ItemSound.quest_middle");
        } else if ("32018-04.htm".equalsIgnoreCase(event)) {
            st.setCond(7);
            st.takeItems(Raffortys_Letter, 1L);
            st.playSound("ItemSound.quest_middle");
        } else {
            if ("Sculpture-04a.htm".equalsIgnoreCase(event)) {
                st.setCond(8);
                st.playSound("ItemSound.quest_middle");
                if (st.getInt("32021") == 0 && st.getInt("32077") == 0) {
                    st.giveItems(Piece_of_Tablet, 1L);
                }
                return "Sculpture-04.htm";
            }
            if ("32022-02.htm".equalsIgnoreCase(event)) {
                st.setCond(9);
                st.giveItems(Report_Piece, 1L);
                st.playSound("ItemSound.quest_middle");
            } else if ("32020-16.htm".equalsIgnoreCase(event)) {
                st.setCond(10);
                st.takeItems(Report_Piece, 1L);
                st.playSound("ItemSound.quest_middle");
            } else if ("32020-18.htm".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(Piece_of_Tablet) <= 0L) {
                    st.setCond(11);
                    st.playSound("ItemSound.quest_middle");
                    return "32020-19.htm";
                }
                st.giveItems(57, 60044L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(false);
            } else if ("32020-19.htm".equalsIgnoreCase(event)) {
                st.setCond(11);
                st.playSound("ItemSound.quest_middle");
            } else if (event.startsWith("32021") || event.startsWith("32077")) {
                if (event.contains("-pick")) {
                    st.set("talk", "1");
                    event = event.replace("-pick", "");
                }
                st.set(event, "1");
                return "Sculpture-05.htm";
            }
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int _state = st.getState();
        if (_state == 3) {
            return "completed";
        }
        final int npcId = npc.getNpcId();
        if (_state != 1) {
            final int cond = st.getCond();
            if (npcId == Rafforty && _state == 2) {
                if (cond == 1) {
                    return "32020-03.htm";
                }
                if (cond == 2) {
                    return "32020-04.htm";
                }
                if (cond == 3) {
                    return "32020-05.htm";
                }
                if (cond == 4) {
                    return "32020-11.htm";
                }
                if (cond == 5) {
                    st.setCond(6);
                    st.giveItems(Raffortys_Letter, 1L);
                    st.playSound("ItemSound.quest_middle");
                    return "32020-13.htm";
                }
                if (cond == 6) {
                    return "32020-14.htm";
                }
                if (cond == 9) {
                    return "32020-15.htm";
                }
                if (cond == 10) {
                    return "32020-17.htm";
                }
                if (cond == 11) {
                    return "32020-20.htm";
                }
                if (cond == 12) {
                    st.giveItems(57, 60040L);
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                    return "32020-18.htm";
                }
            } else if (npcId == Misa && _state == 2) {
                if (cond == 1) {
                    st.setCond(2);
                    st.giveItems(Misas_Letter, 1L);
                    st.playSound("ItemSound.quest_middle");
                    return "32018-01.htm";
                }
                if (cond == 2) {
                    return "32018-02.htm";
                }
                if (cond == 6) {
                    return "32018-03.htm";
                }
                if (cond == 7) {
                    return "32018-05.htm";
                }
            } else if (npcId == Kierre && _state == 2) {
                if (cond == 8) {
                    return "32022-01.htm";
                }
                if (cond == 9) {
                    return "32022-03.htm";
                }
            } else if ((npcId == Ice_Sculpture1 || npcId == Ice_Sculpture2 || npcId == Ice_Sculpture3 || npcId == Ice_Sculpture4) && _state == 2) {
                if (cond == 7) {
                    final String _npcId = String.valueOf(npcId);
                    final int npcId_flag = st.getInt(_npcId);
                    if (npcId == Ice_Sculpture1 || npcId == Ice_Sculpture2) {
                        final int talk_flag = st.getInt("talk");
                        return (npcId_flag == 1) ? "Sculpture-02.htm" : ((talk_flag == 1) ? "Sculpture-06.htm" : ("Sculpture-03-" + _npcId + ".htm"));
                    }
                    if (npcId_flag == 1) {
                        return "Sculpture-02.htm";
                    }
                    st.set(_npcId, "1");
                    return "Sculpture-01.htm";
                } else {
                    if (cond == 8) {
                        return "Sculpture-04.htm";
                    }
                    if (cond == 11) {
                        st.setCond(12);
                        st.giveItems(Piece_of_Tablet, 1L);
                        st.playSound("ItemSound.quest_middle");
                        return "Sculpture-07.htm";
                    }
                    if (cond == 12) {
                        return "Sculpture-08.htm";
                    }
                }
            }
            return "noquest";
        }
        if (npcId != Rafforty) {
            return "noquest";
        }
        if (st.getPlayer().getLevel() >= 53) {
            st.setCond(0);
            return "32020-01.htm";
        }
        st.exitCurrentQuest(true);
        return "32020-00.htm";
    }

    
}
