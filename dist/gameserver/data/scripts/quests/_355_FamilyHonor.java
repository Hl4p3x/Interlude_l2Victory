package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _355_FamilyHonor extends Quest {
    private static final int galicbredo = 30181;
    private static final int patrin = 30929;
    private static final int timak_orc_troop_leader = 20767;
    private static final int timak_orc_troop_shaman = 20768;
    private static final int timak_orc_troop_warrior = 20769;
    private static final int timak_orc_troop_archer = 20770;
    private static final int q_ancient_portrait = 4252;
    private static final int q_beronas_sculpture_0 = 4350;
    private static final int q_beronas_sculpture_s = 4351;
    private static final int q_beronas_sculpture_a = 4352;
    private static final int q_beronas_sculpture_b = 4353;
    private static final int q_beronas_sculpture_c = 4354;

    public _355_FamilyHonor() {
        super(false);
        addStartNpc(30181);
        addTalkId(30929);
        addKillId(20767, 20768, 20769, 20770);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int npcId = npc.getNpcId();
        if (npcId == 30181) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "galicbredo_q0355_04.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                htmltext = "galicbredo_q0355_03.htm";
            } else if ("reply_2".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(4252) < 1L) {
                    htmltext = "galicbredo_q0355_07.htm";
                } else if (st.getQuestItemsCount(4252) >= 100L) {
                    final long cnt = 7800L + st.getQuestItemsCount(4252) * 120L;
                    st.takeItems(4252, -1L);
                    st.giveItems(57, cnt);
                    htmltext = "galicbredo_q0355_07b.htm";
                } else if (st.getQuestItemsCount(4252) >= 1L && st.getQuestItemsCount(4252) < 100L) {
                    st.giveItems(57, st.getQuestItemsCount(4252) * 120L + 2800L);
                    st.takeItems(4252, -1L);
                    htmltext = "galicbredo_q0355_07a.htm";
                }
            } else if ("reply_3".equalsIgnoreCase(event)) {
                htmltext = "galicbredo_q0355_08.htm";
            } else if ("reply_4".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(4252) > 0L) {
                    st.giveItems(57, st.getQuestItemsCount(4252) * 120L);
                }
                st.takeItems(4252, -1L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                htmltext = "galicbredo_q0355_09.htm";
            }
        } else if (npcId == 30929 && "reply_1".equalsIgnoreCase(event)) {
            final int i0 = Rnd.get(100);
            if (st.getQuestItemsCount(4350) < 1L) {
                htmltext = "patrin_q0355_02.htm";
            } else if (st.getQuestItemsCount(4350) >= 1L && i0 < 2) {
                st.giveItems(4351, 1L);
                st.takeItems(4350, 1L);
                htmltext = "patrin_q0355_03.htm";
            } else if (st.getQuestItemsCount(4350) >= 1L && i0 < 32) {
                st.giveItems(4352, 1L);
                st.takeItems(4350, 1L);
                htmltext = "patrin_q0355_04.htm";
            } else if (st.getQuestItemsCount(4350) >= 1L && i0 < 62) {
                st.giveItems(4353, 1L);
                st.takeItems(4350, 1L);
                htmltext = "patrin_q0355_05.htm";
            } else if (st.getQuestItemsCount(4350) >= 1L && i0 < 77) {
                st.giveItems(4354, 1L);
                st.takeItems(4350, 1L);
                htmltext = "patrin_q0355_06.htm";
            } else {
                st.takeItems(4350, 1L);
                htmltext = "patrin_q0355_07.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 30181) {
                    break;
                }
                if (st.getPlayer().getLevel() < 36) {
                    st.exitCurrentQuest(true);
                    htmltext = "galicbredo_q0355_01.htm";
                    break;
                }
                htmltext = "galicbredo_q0355_02.htm";
                break;
            }
            case 2: {
                if (npcId == 30181) {
                    if (st.getQuestItemsCount(4350) < 1L) {
                        htmltext = "galicbredo_q0355_05.htm";
                        break;
                    }
                    if (st.getQuestItemsCount(4350) >= 1L) {
                        htmltext = "galicbredo_q0355_06.htm";
                        break;
                    }
                    break;
                } else {
                    if (npcId == 30929 && id == 2) {
                        htmltext = "patrin_q0355_01.htm";
                        break;
                    }
                    break;
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        if (id == 2) {
            switch (npcId) {
                case 20767: {
                    final int i0 = Rnd.get(1000);
                    if (i0 < 560) {
                        st.giveItems(4252, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    } else if (i0 < 684) {
                        st.giveItems(4350, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 20768: {
                    final int i0 = Rnd.get(100);
                    if (i0 < 53) {
                        st.giveItems(4252, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    } else if (i0 < 65) {
                        st.giveItems(4350, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 20769: {
                    final int i0 = Rnd.get(1000);
                    if (i0 < 420) {
                        st.giveItems(4252, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    } else if (i0 < 516) {
                        st.giveItems(4350, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 20770: {
                    final int i0 = Rnd.get(100);
                    if (i0 < 44) {
                        st.giveItems(4252, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    } else if (i0 < 56) {
                        st.giveItems(4350, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
            }
        }
        return null;
    }
}
