package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _325_GrimCollector extends Quest {
    int ZOMBIE_HEAD;
    int ZOMBIE_HEART;
    int ZOMBIE_LIVER;
    int SKULL;
    int RIB_BONE;
    int SPINE;
    int ARM_BONE;
    int THIGH_BONE;
    int COMPLETE_SKELETON;
    int ANATOMY_DIAGRAM;

    public _325_GrimCollector() {
        super(false);
        ZOMBIE_HEAD = 1350;
        ZOMBIE_HEART = 1351;
        ZOMBIE_LIVER = 1352;
        SKULL = 1353;
        RIB_BONE = 1354;
        SPINE = 1355;
        ARM_BONE = 1356;
        THIGH_BONE = 1357;
        COMPLETE_SKELETON = 1358;
        ANATOMY_DIAGRAM = 1349;
        addStartNpc(30336);
        addTalkId(30336);
        addTalkId(30342);
        addTalkId(30434);
        addKillId(20026);
        addKillId(20029);
        addKillId(20035);
        addKillId(20042);
        addKillId(20045);
        addKillId(20457);
        addKillId(20458);
        addKillId(20051);
        addKillId(20514);
        addKillId(20515);
        addQuestItem(ZOMBIE_HEAD, ZOMBIE_HEART, ZOMBIE_LIVER, SKULL, RIB_BONE, SPINE, ARM_BONE, THIGH_BONE, COMPLETE_SKELETON, ANATOMY_DIAGRAM);
    }

    

    private long pieces(final QuestState st) {
        return st.getQuestItemsCount(ZOMBIE_HEAD) + st.getQuestItemsCount(SPINE) + st.getQuestItemsCount(ARM_BONE) + st.getQuestItemsCount(ZOMBIE_HEART) + st.getQuestItemsCount(ZOMBIE_LIVER) + st.getQuestItemsCount(SKULL) + st.getQuestItemsCount(RIB_BONE) + st.getQuestItemsCount(THIGH_BONE) + st.getQuestItemsCount(COMPLETE_SKELETON);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("guard_curtiz_q0325_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("samed_q0325_03.htm".equalsIgnoreCase(event)) {
            st.giveItems(ANATOMY_DIAGRAM, 1L);
        } else if ("samed_q0325_06.htm".equalsIgnoreCase(event)) {
            if (pieces(st) > 0L) {
                st.giveItems(57, 30L * st.getQuestItemsCount(ZOMBIE_HEAD) + 20L * st.getQuestItemsCount(ZOMBIE_HEART) + 20L * st.getQuestItemsCount(ZOMBIE_LIVER) + 50L * st.getQuestItemsCount(SKULL) + 15L * st.getQuestItemsCount(RIB_BONE) + 10L * st.getQuestItemsCount(SPINE) + 10L * st.getQuestItemsCount(ARM_BONE) + 10L * st.getQuestItemsCount(THIGH_BONE) + 2000L * st.getQuestItemsCount(COMPLETE_SKELETON));
                st.takeItems(ZOMBIE_HEAD, -1L);
                st.takeItems(ZOMBIE_HEART, -1L);
                st.takeItems(ZOMBIE_LIVER, -1L);
                st.takeItems(SKULL, -1L);
                st.takeItems(RIB_BONE, -1L);
                st.takeItems(SPINE, -1L);
                st.takeItems(ARM_BONE, -1L);
                st.takeItems(THIGH_BONE, -1L);
                st.takeItems(COMPLETE_SKELETON, -1L);
            }
            st.takeItems(ANATOMY_DIAGRAM, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("samed_q0325_07.htm".equalsIgnoreCase(event) && pieces(st) > 0L) {
            st.giveItems(57, 30L * st.getQuestItemsCount(ZOMBIE_HEAD) + 20L * st.getQuestItemsCount(ZOMBIE_HEART) + 20L * st.getQuestItemsCount(ZOMBIE_LIVER) + 50L * st.getQuestItemsCount(SKULL) + 15L * st.getQuestItemsCount(RIB_BONE) + 10L * st.getQuestItemsCount(SPINE) + 10L * st.getQuestItemsCount(ARM_BONE) + 10L * st.getQuestItemsCount(THIGH_BONE) + 2000L * st.getQuestItemsCount(COMPLETE_SKELETON));
            st.takeItems(ZOMBIE_HEAD, -1L);
            st.takeItems(ZOMBIE_HEART, -1L);
            st.takeItems(ZOMBIE_LIVER, -1L);
            st.takeItems(SKULL, -1L);
            st.takeItems(RIB_BONE, -1L);
            st.takeItems(SPINE, -1L);
            st.takeItems(ARM_BONE, -1L);
            st.takeItems(THIGH_BONE, -1L);
            st.takeItems(COMPLETE_SKELETON, -1L);
        } else if ("samed_q0325_09.htm".equalsIgnoreCase(event)) {
            st.giveItems(57, 2000L * st.getQuestItemsCount(COMPLETE_SKELETON));
            st.takeItems(COMPLETE_SKELETON, -1L);
        } else if ("varsak_q0325_03.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(SPINE) != 0L && st.getQuestItemsCount(ARM_BONE) != 0L && st.getQuestItemsCount(SKULL) != 0L && st.getQuestItemsCount(RIB_BONE) != 0L && st.getQuestItemsCount(THIGH_BONE) != 0L) {
                st.takeItems(SPINE, 1L);
                st.takeItems(SKULL, 1L);
                st.takeItems(ARM_BONE, 1L);
                st.takeItems(RIB_BONE, 1L);
                st.takeItems(THIGH_BONE, 1L);
                if (Rnd.chance(80)) {
                    st.giveItems(COMPLETE_SKELETON, 1L);
                } else {
                    htmltext = "varsak_q0325_04.htm";
                }
            } else {
                htmltext = "varsak_q0325_02.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        final int cond = st.getCond();
        if (id == 1) {
            st.setCond(0);
        }
        if (npcId == 30336 && cond == 0) {
            if (st.getPlayer().getLevel() >= 15) {
                htmltext = "guard_curtiz_q0325_02.htm";
                return htmltext;
            }
            htmltext = "guard_curtiz_q0325_01.htm";
            st.exitCurrentQuest(true);
        } else if (npcId == 30336 && cond > 0) {
            if (st.getQuestItemsCount(ANATOMY_DIAGRAM) == 0L) {
                htmltext = "guard_curtiz_q0325_04.htm";
            } else {
                htmltext = "guard_curtiz_q0325_05.htm";
            }
        } else if (npcId == 30434 && cond > 0) {
            if (st.getQuestItemsCount(ANATOMY_DIAGRAM) == 0L) {
                htmltext = "samed_q0325_01.htm";
            } else if (st.getQuestItemsCount(ANATOMY_DIAGRAM) != 0L && pieces(st) == 0L) {
                htmltext = "samed_q0325_04.htm";
            } else if (st.getQuestItemsCount(ANATOMY_DIAGRAM) != 0L && pieces(st) > 0L && st.getQuestItemsCount(COMPLETE_SKELETON) == 0L) {
                htmltext = "samed_q0325_05.htm";
            } else if (st.getQuestItemsCount(ANATOMY_DIAGRAM) != 0L && pieces(st) > 0L && st.getQuestItemsCount(COMPLETE_SKELETON) > 0L) {
                htmltext = "samed_q0325_08.htm";
            }
        } else if (npcId == 30342 && cond > 0 && st.getQuestItemsCount(ANATOMY_DIAGRAM) > 0L) {
            htmltext = "varsak_q0325_01.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (st.getQuestItemsCount(ANATOMY_DIAGRAM) == 0L) {
            return null;
        }
        final int n = Rnd.get(100);
        switch (npcId) {
            case 20026:
                if (n < 90) {
                    st.playSound("ItemSound.quest_itemget");
                    if (n < 40) {
                        st.giveItems(ZOMBIE_HEAD, 1L);
                    } else if (n < 60) {
                        st.giveItems(ZOMBIE_HEART, 1L);
                    } else {
                        st.giveItems(ZOMBIE_LIVER, 1L);
                    }
                }
                break;
            case 20029:
                st.playSound("ItemSound.quest_itemget");
                if (n < 44) {
                    st.giveItems(ZOMBIE_HEAD, 1L);
                } else if (n < 66) {
                    st.giveItems(ZOMBIE_HEART, 1L);
                } else {
                    st.giveItems(ZOMBIE_LIVER, 1L);
                }
                break;
            case 20035:
                if (n < 79) {
                    st.playSound("ItemSound.quest_itemget");
                    if (n < 5) {
                        st.giveItems(SKULL, 1L);
                    } else if (n < 15) {
                        st.giveItems(RIB_BONE, 1L);
                    } else if (n < 29) {
                        st.giveItems(SPINE, 1L);
                    } else {
                        st.giveItems(THIGH_BONE, 1L);
                    }
                }
                break;
            case 20042:
                if (n < 86) {
                    st.playSound("ItemSound.quest_itemget");
                    if (n < 6) {
                        st.giveItems(SKULL, 1L);
                    } else if (n < 19) {
                        st.giveItems(RIB_BONE, 1L);
                    } else if (n < 69) {
                        st.giveItems(ARM_BONE, 1L);
                    } else {
                        st.giveItems(THIGH_BONE, 1L);
                    }
                }
                break;
            case 20045:
                if (n < 97) {
                    st.playSound("ItemSound.quest_itemget");
                    if (n < 9) {
                        st.giveItems(SKULL, 1L);
                    } else if (n < 59) {
                        st.giveItems(SPINE, 1L);
                    } else if (n < 77) {
                        st.giveItems(ARM_BONE, 1L);
                    } else {
                        st.giveItems(THIGH_BONE, 1L);
                    }
                }
                break;
            case 20051:
                if (n < 99) {
                    st.playSound("ItemSound.quest_itemget");
                    if (n < 9) {
                        st.giveItems(SKULL, 1L);
                    } else if (n < 59) {
                        st.giveItems(RIB_BONE, 1L);
                    } else if (n < 79) {
                        st.giveItems(SPINE, 1L);
                    } else {
                        st.giveItems(ARM_BONE, 1L);
                    }
                }
                break;
            case 20514:
                if (n < 51) {
                    st.playSound("ItemSound.quest_itemget");
                    if (n < 2) {
                        st.giveItems(SKULL, 1L);
                    } else if (n < 8) {
                        st.giveItems(RIB_BONE, 1L);
                    } else if (n < 17) {
                        st.giveItems(SPINE, 1L);
                    } else if (n < 18) {
                        st.giveItems(ARM_BONE, 1L);
                    } else {
                        st.giveItems(THIGH_BONE, 1L);
                    }
                }
                break;
            case 20515:
                if (n < 60) {
                    st.playSound("ItemSound.quest_itemget");
                    if (n < 3) {
                        st.giveItems(SKULL, 1L);
                    } else if (n < 11) {
                        st.giveItems(RIB_BONE, 1L);
                    } else if (n < 22) {
                        st.giveItems(SPINE, 1L);
                    } else if (n < 24) {
                        st.giveItems(ARM_BONE, 1L);
                    } else {
                        st.giveItems(THIGH_BONE, 1L);
                    }
                }
                break;
            case 20457:
            case 20458:
                st.playSound("ItemSound.quest_itemget");
                if (n < 42) {
                    st.giveItems(ZOMBIE_HEAD, 1L);
                } else if (n < 67) {
                    st.giveItems(ZOMBIE_HEART, 1L);
                } else {
                    st.giveItems(ZOMBIE_LIVER, 1L);
                }
                break;
        }
        return null;
    }
}
