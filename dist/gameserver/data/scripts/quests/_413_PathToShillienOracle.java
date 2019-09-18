package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _413_PathToShillienOracle extends Quest {
    public final int SIDRA = 30330;
    public final int ADONIUS = 30375;
    public final int TALBOT = 30377;
    public final int ZOMBIE_SOLDIER = 20457;
    public final int ZOMBIE_WARRIOR = 20458;
    public final int SHIELD_SKELETON = 20514;
    public final int SKELETON_INFANTRYMAN = 20515;
    public final int DARK_SUCCUBUS = 20776;
    public final int SIDRAS_LETTER1_ID = 1262;
    public final int BLANK_SHEET1_ID = 1263;
    public final int BLOODY_RUNE1_ID = 1264;
    public final int GARMIEL_BOOK_ID = 1265;
    public final int PRAYER_OF_ADON_ID = 1266;
    public final int PENITENTS_MARK_ID = 1267;
    public final int ASHEN_BONES_ID = 1268;
    public final int ANDARIEL_BOOK_ID = 1269;
    public final int ORB_OF_ABYSS_ID = 1270;
    public final int[] ASHEN_BONES_DROP;

    public _413_PathToShillienOracle() {
        super(false);
        ASHEN_BONES_DROP = new int[]{20457, 20458, 20514, 20515};
        addStartNpc(30330);
        addTalkId(30375);
        addTalkId(30377);
        addKillId(20776);
        for (final int i : ASHEN_BONES_DROP) {
            addKillId(i);
        }
        addQuestItem(1268);
        addQuestItem(1262);
        addQuestItem(1269);
        addQuestItem(1267);
        addQuestItem(1265);
        addQuestItem(1266);
        addQuestItem(1263);
        addQuestItem(1264);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equalsIgnoreCase(event)) {
            htmltext = "master_sidra_q0413_06.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1262, 1L);
        } else if ("413_1".equalsIgnoreCase(event)) {
            if (st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 38 && st.getQuestItemsCount(1270) < 1L) {
                htmltext = "master_sidra_q0413_05.htm";
            } else if (st.getPlayer().getClassId().getId() != 38) {
                if (st.getPlayer().getClassId().getId() == 42) {
                    htmltext = "master_sidra_q0413_02a.htm";
                } else {
                    htmltext = "master_sidra_q0413_03.htm";
                }
            } else if (st.getPlayer().getLevel() < 18 && st.getPlayer().getClassId().getId() == 38) {
                htmltext = "master_sidra_q0413_02.htm";
            } else if (st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 38 && st.getQuestItemsCount(1270) > 0L) {
                htmltext = "master_sidra_q0413_04.htm";
            }
        } else if ("30377_1".equalsIgnoreCase(event)) {
            htmltext = "magister_talbot_q0413_02.htm";
            st.takeItems(1262, -1L);
            st.giveItems(1263, 5L);
            st.playSound("ItemSound.quest_itemget");
            st.setCond(2);
        } else if ("30375_1".equalsIgnoreCase(event)) {
            htmltext = "priest_adonius_q0413_02.htm";
        } else if ("30375_2".equalsIgnoreCase(event)) {
            htmltext = "priest_adonius_q0413_03.htm";
        } else if ("30375_3".equalsIgnoreCase(event)) {
            htmltext = "priest_adonius_q0413_04.htm";
            st.takeItems(1266, -1L);
            st.giveItems(1267, 1L);
            st.playSound("ItemSound.quest_itemget");
            st.setCond(5);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        switch (npcId) {
            case 30330:
                if (cond < 1) {
                    htmltext = "master_sidra_q0413_01.htm";
                } else if (cond == 1) {
                    htmltext = "master_sidra_q0413_07.htm";
                } else if (cond == 2 | cond == 3) {
                    htmltext = "master_sidra_q0413_08.htm";
                } else if (cond > 3 && cond < 7) {
                    htmltext = "master_sidra_q0413_09.htm";
                } else if (cond == 7 && st.getQuestItemsCount(1269) > 0L && st.getQuestItemsCount(1265) > 0L) {
                    htmltext = "master_sidra_q0413_10.htm";
                    st.exitCurrentQuest(true);
                    if (st.getPlayer().getClassId().getLevel() == 1) {
                        st.giveItems(1270, 1L);
                        if (!st.getPlayer().getVarB("prof1")) {
                            st.getPlayer().setVar("prof1", "1", -1L);
                            st.addExpAndSp(3200L, 3120L);
                        }
                    }
                    st.playSound("ItemSound.quest_finish");
                }
                break;
            case 30377:
                if (cond == 1 && st.getQuestItemsCount(1262) > 0L) {
                    htmltext = "magister_talbot_q0413_01.htm";
                } else if (cond == 2) {
                    if (st.getQuestItemsCount(1264) < 1L) {
                        htmltext = "magister_talbot_q0413_03.htm";
                    } else if (st.getQuestItemsCount(1264) > 0L) {
                        htmltext = "magister_talbot_q0413_04.htm";
                    }
                } else if (cond == 3 && st.getQuestItemsCount(1264) > 4L) {
                    htmltext = "magister_talbot_q0413_05.htm";
                    st.takeItems(1264, -1L);
                    st.giveItems(1265, 1L);
                    st.giveItems(1266, 1L);
                    st.playSound("ItemSound.quest_itemget");
                    st.setCond(4);
                } else if (cond > 3 && cond < 7) {
                    htmltext = "magister_talbot_q0413_06.htm";
                } else if (cond == 7) {
                    htmltext = "magister_talbot_q0413_07.htm";
                }
                break;
            case 30375:
                if (cond == 4 && st.getQuestItemsCount(1266) > 0L) {
                    htmltext = "priest_adonius_q0413_01.htm";
                } else if (cond == 5 && st.getQuestItemsCount(1268) < 1L) {
                    htmltext = "priest_adonius_q0413_05.htm";
                } else if (cond == 5 && st.getQuestItemsCount(1268) < 10L) {
                    htmltext = "priest_adonius_q0413_06.htm";
                } else if (cond == 6 && st.getQuestItemsCount(1268) > 9L) {
                    htmltext = "priest_adonius_q0413_07.htm";
                    st.takeItems(1268, -1L);
                    st.takeItems(1267, -1L);
                    st.giveItems(1269, 1L);
                    st.playSound("ItemSound.quest_itemget");
                    st.setCond(7);
                } else if (cond == 7 && st.getQuestItemsCount(1269) > 0L) {
                    htmltext = "priest_adonius_q0413_08.htm";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 20776 && cond == 2 && st.getQuestItemsCount(1263) > 0L) {
            st.giveItems(1264, 1L);
            st.takeItems(1263, 1L);
            if (st.getQuestItemsCount(1263) < 1L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(3);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        for (final int i : ASHEN_BONES_DROP) {
            if (npcId == i && cond == 5 && st.getQuestItemsCount(1268) < 10L) {
                st.giveItems(1268, 1L);
                if (st.getQuestItemsCount(1268) > 9L) {
                    st.playSound("ItemSound.quest_middle");
                    st.setCond(6);
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
