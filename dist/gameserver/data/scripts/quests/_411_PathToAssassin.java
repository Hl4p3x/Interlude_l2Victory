package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _411_PathToAssassin extends Quest {
    public final int TRISKEL = 30416;
    public final int LEIKAN = 30382;
    public final int ARKENIA = 30419;
    public final int MOONSTONE_BEAST = 20369;
    public final int CALPICO = 27036;
    public final int SHILENS_CALL_ID = 1245;
    public final int ARKENIAS_LETTER_ID = 1246;
    public final int LEIKANS_NOTE_ID = 1247;
    public final int ONYX_BEASTS_MOLAR_ID = 1248;
    public final int LEIKANS_KNIFE_ID = 1249;
    public final int SHILENS_TEARS_ID = 1250;
    public final int ARKENIA_RECOMMEND_ID = 1251;
    public final int IRON_HEART_ID = 1252;

    public _411_PathToAssassin() {
        super(false);
        addStartNpc(30416);
        addTalkId(30382);
        addTalkId(30419);
        addKillId(20369);
        addKillId(27036);
        addQuestItem(1245, 1247, 1249, 1251, 1246, 1248, 1250);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equalsIgnoreCase(event)) {
            if (st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 31 && st.getQuestItemsCount(1252) < 1L) {
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                st.giveItems(1245, 1L);
                htmltext = "triskel_q0411_05.htm";
            } else if (st.getPlayer().getClassId().getId() != 31) {
                if (st.getPlayer().getClassId().getId() == 35) {
                    htmltext = "triskel_q0411_02a.htm";
                } else {
                    htmltext = "triskel_q0411_02.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (st.getPlayer().getLevel() < 18 && st.getPlayer().getClassId().getId() == 31) {
                htmltext = "triskel_q0411_03.htm";
                st.exitCurrentQuest(true);
            } else if (st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 31 && st.getQuestItemsCount(1252) > 0L) {
                htmltext = "triskel_q0411_04.htm";
            }
        } else if ("30419_1".equalsIgnoreCase(event)) {
            htmltext = "arkenia_q0411_05.htm";
            st.takeItems(1245, -1L);
            st.giveItems(1246, 1L);
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
        } else if ("30382_1".equalsIgnoreCase(event)) {
            htmltext = "guard_leikan_q0411_03.htm";
            st.takeItems(1246, -1L);
            st.giveItems(1247, 1L);
            st.setCond(3);
            st.playSound("ItemSound.quest_middle");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        switch (npcId) {
            case 30416:
                if (cond < 1) {
                    if (st.getQuestItemsCount(1252) < 1L) {
                        htmltext = "triskel_q0411_01.htm";
                    } else {
                        htmltext = "triskel_q0411_04.htm";
                    }
                } else if (cond == 7) {
                    htmltext = "triskel_q0411_06.htm";
                    st.takeItems(1251, -1L);
                    if (st.getPlayer().getClassId().getLevel() == 1) {
                        st.giveItems(1252, 1L);
                        if (!st.getPlayer().getVarB("prof1")) {
                            st.getPlayer().setVar("prof1", "1", -1L);
                            st.addExpAndSp(3200L, 3930L);
                        }
                    }
                    st.exitCurrentQuest(true);
                    st.playSound("ItemSound.quest_finish");
                } else if (cond == 2) {
                    htmltext = "triskel_q0411_07.htm";
                } else if (cond == 1) {
                    htmltext = "triskel_q0411_11.htm";
                } else if (cond > 2 && cond < 7) {
                    if (cond > 2 && cond < 5) {
                        htmltext = "triskel_q0411_08.htm";
                    } else if (cond > 4 && cond < 7) {
                        if (st.getQuestItemsCount(1250) < 1L) {
                            htmltext = "triskel_q0411_09.htm";
                        } else {
                            htmltext = "triskel_q0411_10.htm";
                        }
                    }
                }
                break;
            case 30419:
                if (cond == 1 && st.getQuestItemsCount(1245) > 0L) {
                    htmltext = "arkenia_q0411_01.htm";
                } else if (cond == 2 && st.getQuestItemsCount(1246) > 0L) {
                    htmltext = "arkenia_q0411_07.htm";
                } else if (cond > 2 && cond < 5 && st.getQuestItemsCount(1247) > 0L) {
                    htmltext = "arkenia_q0411_10.htm";
                } else if (cond == 5 && st.getQuestItemsCount(1249) > 0L) {
                    htmltext = "arkenia_q0411_11.htm";
                } else if (cond == 6 && st.getQuestItemsCount(1250) > 0L) {
                    htmltext = "arkenia_q0411_08.htm";
                    st.takeItems(1250, -1L);
                    st.takeItems(1249, -1L);
                    st.giveItems(1251, 1L);
                    st.setCond(7);
                    st.playSound("ItemSound.quest_middle");
                } else if (cond == 7) {
                    htmltext = "arkenia_q0411_09.htm";
                }
                break;
            case 30382:
                if (cond == 2 && st.getQuestItemsCount(1246) > 0L) {
                    htmltext = "guard_leikan_q0411_01.htm";
                } else if (cond > 2 && cond < 4 && st.getQuestItemsCount(1248) < 1L) {
                    htmltext = "guard_leikan_q0411_05.htm";
                    if (cond == 4) {
                        st.setCond(3);
                    }
                } else if (cond > 2 && cond < 4 && st.getQuestItemsCount(1248) < 10L) {
                    htmltext = "guard_leikan_q0411_06.htm";
                    if (cond == 4) {
                        st.setCond(3);
                    }
                } else if (cond == 4 && st.getQuestItemsCount(1248) > 9L) {
                    htmltext = "guard_leikan_q0411_07.htm";
                    st.takeItems(1248, -1L);
                    st.takeItems(1247, -1L);
                    st.giveItems(1249, 1L);
                    st.setCond(5);
                    st.playSound("ItemSound.quest_middle");
                } else if (cond > 4 && cond < 7 && st.getQuestItemsCount(1250) < 1L) {
                    htmltext = "guard_leikan_q0411_09.htm";
                    if (cond == 6) {
                        st.setCond(5);
                    }
                } else if (cond == 6 && st.getQuestItemsCount(1250) > 0L) {
                    htmltext = "guard_leikan_q0411_08.htm";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 27036) {
            if (cond == 5 && st.getQuestItemsCount(1249) > 0L && st.getQuestItemsCount(1250) < 1L) {
                st.giveItems(1250, 1L);
                st.playSound("ItemSound.quest_middle");
                st.setCond(6);
            }
        } else if (npcId == 20369 && cond == 3 && st.getQuestItemsCount(1247) > 0L && st.getQuestItemsCount(1248) < 10L) {
            st.giveItems(1248, 1L);
            if (st.getQuestItemsCount(1248) > 9L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(4);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
