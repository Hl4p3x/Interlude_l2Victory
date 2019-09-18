package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _407_PathToElvenScout extends Quest {
    public final int REISA = 30328;
    public final int MORETTI = 30337;
    public final int PIPPEN = 30426;
    public final int OL_MAHUM_SENTRY = 27031;
    public final int OL_MAHUM_PATROL = 20053;
    public final int REORIA_LETTER2_ID = 1207;
    public final int PRIGUNS_TEAR_LETTER1_ID = 1208;
    public final int PRIGUNS_TEAR_LETTER2_ID = 1209;
    public final int PRIGUNS_TEAR_LETTER3_ID = 1210;
    public final int PRIGUNS_TEAR_LETTER4_ID = 1211;
    public final int MORETTIS_HERB_ID = 1212;
    public final int MORETTIS_LETTER_ID = 1214;
    public final int PRIGUNS_LETTER_ID = 1215;
    public final int MONORARY_GUARD_ID = 1216;
    public final int REORIA_RECOMMENDATION_ID = 1217;
    public final int RUSTED_KEY_ID = 1293;
    public final int HONORARY_GUARD_ID = 1216;

    public _407_PathToElvenScout() {
        super(false);
        addStartNpc(30328);
        addTalkId(30337);
        addTalkId(30426);
        addKillId(27031);
        addKillId(20053);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equalsIgnoreCase(event)) {
            switch (st.getPlayer().getClassId().getId()) {
                case 18:
                    if (st.getPlayer().getLevel() >= 18) {
                        if (st.getQuestItemsCount(1217) > 0L) {
                            htmltext = "master_reoria_q0407_04.htm";
                            st.exitCurrentQuest(true);
                        } else {
                            htmltext = "master_reoria_q0407_05.htm";
                            st.giveItems(1207, 1L);
                            st.setCond(1);
                            st.setState(2);
                            st.playSound("ItemSound.quest_accept");
                        }
                    } else {
                        htmltext = "master_reoria_q0407_03.htm";
                        st.exitCurrentQuest(true);
                    }
                    break;
                case 22:
                    htmltext = "master_reoria_q0407_02a.htm";
                    st.exitCurrentQuest(true);
                    break;
                default:
                    htmltext = "master_reoria_q0407_02.htm";
                    st.exitCurrentQuest(true);
                    break;
            }
        } else if ("30337_1".equalsIgnoreCase(event)) {
            st.takeItems(1207, 1L);
            st.setCond(2);
            htmltext = "guard_moretti_q0407_03.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        int cond = 0;
        if (id != 1) {
            cond = st.getCond();
        }
        switch (npcId) {
            case 30328:
                if (cond == 0) {
                    htmltext = "master_reoria_q0407_01.htm";
                } else if (cond == 1) {
                    htmltext = "master_reoria_q0407_06.htm";
                } else if (cond > 1 && st.getQuestItemsCount(1216) == 0L) {
                    htmltext = "master_reoria_q0407_08.htm";
                } else if (cond == 8 && st.getQuestItemsCount(1216) == 1L) {
                    htmltext = "master_reoria_q0407_07.htm";
                    st.takeItems(1216, 1L);
                    if (st.getPlayer().getClassId().getLevel() == 1) {
                        st.giveItems(1217, 1L);
                        if (!st.getPlayer().getVarB("prof1")) {
                            st.getPlayer().setVar("prof1", "1", -1L);
                            st.addExpAndSp(3200L, 1000L);
                        }
                    }
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                }
                break;
            case 30337:
                if (cond == 1) {
                    htmltext = "guard_moretti_q0407_01.htm";
                } else if (cond == 2) {
                    htmltext = "guard_moretti_q0407_04.htm";
                } else if (cond == 3) {
                    if (st.getQuestItemsCount(1208) == 1L && st.getQuestItemsCount(1209) == 1L && st.getQuestItemsCount(1210) == 1L && st.getQuestItemsCount(1211) == 1L) {
                        htmltext = "guard_moretti_q0407_06.htm";
                        st.takeItems(1208, 1L);
                        st.takeItems(1209, 1L);
                        st.takeItems(1210, 1L);
                        st.takeItems(1211, 1L);
                        st.giveItems(1212, 1L);
                        st.giveItems(1214, 1L);
                        st.setCond(4);
                    } else {
                        htmltext = "guard_moretti_q0407_05.htm";
                    }
                } else if (cond == 7 && st.getQuestItemsCount(1215) == 1L) {
                    htmltext = "guard_moretti_q0407_07.htm";
                    st.takeItems(1215, 1L);
                    st.giveItems(1216, 1L);
                    st.setCond(8);
                } else if (cond > 8) {
                    htmltext = "guard_moretti_q0407_08.htm";
                }
                break;
            case 30426:
                if (cond == 4) {
                    htmltext = "prigun_q0407_01.htm";
                    st.setCond(5);
                } else if (cond == 5) {
                    htmltext = "prigun_q0407_01.htm";
                } else if (cond == 6 && st.getQuestItemsCount(1293) == 1L && st.getQuestItemsCount(1212) == 1L && st.getQuestItemsCount(1214) == 1L) {
                    htmltext = "prigun_q0407_02.htm";
                    st.takeItems(1293, 1L);
                    st.takeItems(1212, 1L);
                    st.takeItems(1214, 1L);
                    st.giveItems(1215, 1L);
                    st.setCond(7);
                } else if (cond == 7) {
                    htmltext = "prigun_q0407_04.htm";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 20053 && cond == 2) {
            if (st.getQuestItemsCount(1208) == 0L) {
                st.giveItems(1208, 1L);
                st.playSound("ItemSound.quest_itemget");
                return null;
            }
            if (st.getQuestItemsCount(1209) == 0L) {
                st.giveItems(1209, 1L);
                st.playSound("ItemSound.quest_itemget");
                return null;
            }
            if (st.getQuestItemsCount(1210) == 0L) {
                st.giveItems(1210, 1L);
                st.playSound("ItemSound.quest_itemget");
                return null;
            }
            if (st.getQuestItemsCount(1211) == 0L) {
                st.giveItems(1211, 1L);
                st.playSound("ItemSound.quest_middle");
                st.setCond(3);
                return null;
            }
        } else if (npcId == 27031 && cond == 5 && Rnd.chance(60)) {
            st.giveItems(1293, 1L);
            st.playSound("ItemSound.quest_middle");
            st.setCond(6);
        }
        return null;
    }
}
