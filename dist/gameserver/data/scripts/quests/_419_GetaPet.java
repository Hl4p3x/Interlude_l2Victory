package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _419_GetaPet extends Quest {
    private static final int PET_MANAGER_MARTIN = 30731;
    private static final int GK_BELLA = 30256;
    private static final int MC_ELLIE = 30091;
    private static final int GD_METTY = 30072;
    private static final int SPIDER_H1 = 20103;
    private static final int SPIDER_H2 = 20106;
    private static final int SPIDER_H3 = 20108;
    private static final int SPIDER_LE1 = 20460;
    private static final int SPIDER_LE2 = 20308;
    private static final int SPIDER_LE3 = 20466;
    private static final int SPIDER_DE1 = 20025;
    private static final int SPIDER_DE2 = 20105;
    private static final int SPIDER_DE3 = 20034;
    private static final int SPIDER_O1 = 20474;
    private static final int SPIDER_O2 = 20476;
    private static final int SPIDER_O3 = 20478;
    private static final int SPIDER_D1 = 20403;
    private static final int SPIDER_D2 = 20508;
    private static final int SPIDER_K1 = 22244;
    private static final int REQUIRED_SPIDER_LEGS = 50;
    private static final int ANIMAL_LOVERS_LIST1 = 3417;
    private static final int ANIMAL_SLAYER_LIST1 = 3418;
    private static final int ANIMAL_SLAYER_LIST2 = 3419;
    private static final int ANIMAL_SLAYER_LIST3 = 3420;
    private static final int ANIMAL_SLAYER_LIST4 = 3421;
    private static final int ANIMAL_SLAYER_LIST5 = 3422;
    private static final int SPIDER_LEG1 = 3423;
    private static final int SPIDER_LEG2 = 3424;
    private static final int SPIDER_LEG3 = 3425;
    private static final int SPIDER_LEG4 = 3426;
    private static final int SPIDER_LEG5 = 3427;
    private static final int WOLF_COLLAR = 2375;
    private static final int DROP_CHANCE_BUGBEAR_BLOOD_ID = 25;
    private static final int DROP_CHANCE_FORBIDDEN_LOVE_SCROLL_ID = 3;
    private static final int DROP_CHANCE_NECKLACE_OF_GRACE_ID = 4;
    private static final int DROP_CHANCE_GOLD_BAR_ID = 10;
    private static final int[][] DROPLIST_COND = {{1, 0, 20103, 3418, 3423, 50, 100, 1}, {1, 0, 20106, 3418, 3423, 50, 100, 1}, {1, 0, 20108, 3418, 3423, 50, 100, 1}, {1, 0, 20460, 3419, 3424, 50, 100, 1}, {1, 0, 20308, 3419, 3424, 50, 100, 1}, {1, 0, 20466, 3419, 3424, 50, 100, 1}, {1, 0, 20025, 3420, 3425, 50, 100, 1}, {1, 0, 20105, 3420, 3425, 50, 100, 1}, {1, 0, 20034, 3420, 3425, 50, 100, 1}, {1, 0, 20474, 3421, 3426, 50, 100, 1}, {1, 0, 20476, 3421, 3426, 50, 100, 1}, {1, 0, 20478, 3421, 3426, 50, 100, 1}, {1, 0, 20403, 3422, 3427, 50, 100, 1}, {1, 0, 20508, 3422, 3427, 50, 100, 1}};

    public _419_GetaPet() {
        super(false);
        addStartNpc(30731);
        addTalkId(30731);
        addTalkId(30256);
        addTalkId(30091);
        addTalkId(30072);
        addQuestItem(3417);
        addQuestItem(3419);
        addQuestItem(3420);
        addQuestItem(3421);
        addQuestItem(3422);
        addQuestItem(3423);
        addQuestItem(3424);
        addQuestItem(3425);
        addQuestItem(3426);
        addQuestItem(3427);
        for (int i = 0; i < _419_GetaPet.DROPLIST_COND.length; ++i) {
            addKillId(_419_GetaPet.DROPLIST_COND[i][2]);
        }
    }

    

    public long getCount_proof(final QuestState st) {
        long counts = 0L;
        switch (st.getPlayer().getRace()) {
            case human: {
                counts = st.getQuestItemsCount(3423);
                break;
            }
            case elf: {
                counts = st.getQuestItemsCount(3424);
                break;
            }
            case darkelf: {
                counts = st.getQuestItemsCount(3425);
                break;
            }
            case orc: {
                counts = st.getQuestItemsCount(3426);
                break;
            }
            case dwarf: {
                counts = st.getQuestItemsCount(3427);
                break;
            }
        }
        return counts;
    }

    public String check_questions(final QuestState st) {
        String htmltext;
        final int answers = st.getInt("answers");
        final int question = st.getInt("question");
        if (question > 0) {
            htmltext = "419_q" + String.valueOf(question) + ".htm";
        } else if (answers < 10) {
            final String[] ANS = st.get("quiz").split(" ");
            final int GetQuestion = Rnd.get(ANS.length);
            final String index = ANS[GetQuestion];
            st.set("question", index);
            StringBuilder quiz = new StringBuilder();
            if (GetQuestion + 1 == ANS.length) {
                for (int i = 0; i < ANS.length - 2; ++i) {
                    quiz.append(ANS[i]).append(" ");
                }
                quiz.append(ANS[ANS.length - 2]);
            } else {
                for (int i = 0; i < ANS.length - 1; ++i) {
                    if (i != GetQuestion) {
                        quiz.append(ANS[i]).append(" ");
                    }
                }
                quiz.append(ANS[ANS.length - 1]);
            }
            st.set("quiz", quiz.toString());
            htmltext = "419_q" + index + ".htm";
        } else {
            st.giveItems(2375, 1L);
            st.playSound("ItemSound.quest_finish");
            htmltext = "Completed.htm";
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int StateId = st.getInt("id");
        if ("details".equalsIgnoreCase(event)) {
            htmltext = "419_confirm.htm";
        } else if ("agree".equalsIgnoreCase(event)) {
            st.setState(2);
            st.setCond(1);
            switch (st.getPlayer().getRace()) {
                case human: {
                    st.giveItems(3418, 1L);
                    htmltext = "419_slay_0.htm";
                    break;
                }
                case elf: {
                    st.giveItems(3419, 1L);
                    htmltext = "419_slay_1.htm";
                    break;
                }
                case darkelf: {
                    st.giveItems(3420, 1L);
                    htmltext = "419_slay_2.htm";
                    break;
                }
                case orc: {
                    st.giveItems(3421, 1L);
                    htmltext = "419_slay_3.htm";
                    break;
                }
                case dwarf: {
                    st.giveItems(3422, 1L);
                    htmltext = "419_slay_4.htm";
                    break;
                }
            }
            st.playSound("ItemSound.quest_accept");
        } else if ("disagree".equalsIgnoreCase(event)) {
            htmltext = "419_cancelled.htm";
            st.exitCurrentQuest(true);
        } else if (StateId == 1) {
            if ("talk".equalsIgnoreCase(event)) {
                htmltext = "419_talk.htm";
            } else if ("talk1".equalsIgnoreCase(event)) {
                htmltext = "419_bella_2.htm";
            } else if ("talk2".equalsIgnoreCase(event)) {
                st.set("progress", String.valueOf(st.getInt("progress") | 0x1));
                htmltext = "419_bella_3.htm";
            } else if ("talk3".equalsIgnoreCase(event)) {
                st.set("progress", String.valueOf(st.getInt("progress") | 0x2));
                htmltext = "419_ellie_2.htm";
            } else if ("talk4".equalsIgnoreCase(event)) {
                st.set("progress", String.valueOf(st.getInt("progress") | 0x4));
                htmltext = "419_metty_2.htm";
            }
        } else if (StateId == 2) {
            if ("tryme".equalsIgnoreCase(event)) {
                htmltext = check_questions(st);
            } else if ("wrong".equalsIgnoreCase(event)) {
                st.set("id", "1");
                st.set("progress", "0");
                st.unset("quiz");
                st.unset("answers");
                st.unset("question");
                st.giveItems(3417, 1L);
                htmltext = "419_failed.htm";
            } else if ("right".equalsIgnoreCase(event)) {
                st.set("answers", String.valueOf(st.getInt("answers") + 1));
                st.set("question", "0");
                htmltext = check_questions(st);
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int StateId = st.getInt("id");
        final int cond = st.getCond();
        if (cond == 0) {
            if (npcId == 30731) {
                if (st.getPlayer().getLevel() < 15) {
                    htmltext = "419_low_level.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "Start.htm";
                }
            }
        } else if (cond == 1) {
            if (npcId == 30731) {
                switch (StateId) {
                    case 0:
                        final long counts = getCount_proof(st);
                        if (counts == 0L) {
                            htmltext = "419_no_slay.htm";
                        } else if (counts < 50L) {
                            htmltext = "419_pending_slay.htm";
                        } else {
                            switch (st.getPlayer().getRace()) {
                                case human: {
                                    st.takeItems(3418, -1L);
                                    st.takeItems(3423, -1L);
                                    break;
                                }
                                case elf: {
                                    st.takeItems(3419, -1L);
                                    st.takeItems(3424, -1L);
                                    break;
                                }
                                case darkelf: {
                                    st.takeItems(3420, -1L);
                                    st.takeItems(3425, -1L);
                                    break;
                                }
                                case orc: {
                                    st.takeItems(3421, -1L);
                                    st.takeItems(3426, -1L);
                                    break;
                                }
                                case dwarf: {
                                    st.takeItems(3422, -1L);
                                    st.takeItems(3427, -1L);
                                    break;
                                }
                            }
                            st.set("id", "1");
                            st.giveItems(3417, 1L);
                            htmltext = "Slayed.htm";
                        }
                        break;
                    case 1:
                        if (st.getInt("progress") == 7) {
                            st.takeItems(3417, -1L);
                            st.set("quiz", "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
                            st.set("answers", "0");
                            st.set("id", "2");
                            htmltext = "Talked.htm";
                        } else {
                            htmltext = "419_pending_talk.htm";
                        }
                        break;
                    case 2:
                        htmltext = "Talked.htm";
                        break;
                }
            } else if (StateId == 1) {
                switch (npcId) {
                    case 30256:
                        htmltext = "419_bella_1.htm";
                        break;
                    case 30091:
                        htmltext = "419_ellie_1.htm";
                        break;
                    case 30072:
                        htmltext = "419_metty_1.htm";
                        break;
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _419_GetaPet.DROPLIST_COND.length; ++i) {
            if (cond == _419_GetaPet.DROPLIST_COND[i][0] && npcId == _419_GetaPet.DROPLIST_COND[i][2] && (_419_GetaPet.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_419_GetaPet.DROPLIST_COND[i][3]) > 0L)) {
                if (_419_GetaPet.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_419_GetaPet.DROPLIST_COND[i][4], _419_GetaPet.DROPLIST_COND[i][7], (double) _419_GetaPet.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_419_GetaPet.DROPLIST_COND[i][4], _419_GetaPet.DROPLIST_COND[i][7], _419_GetaPet.DROPLIST_COND[i][7], _419_GetaPet.DROPLIST_COND[i][5], (double) _419_GetaPet.DROPLIST_COND[i][6]) && _419_GetaPet.DROPLIST_COND[i][1] != cond && _419_GetaPet.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_419_GetaPet.DROPLIST_COND[i][1]);
                    st.setState(2);
                }
            }
        }
        return null;
    }
}
