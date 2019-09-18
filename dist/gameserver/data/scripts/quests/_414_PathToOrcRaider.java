package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _414_PathToOrcRaider extends Quest {
    public final int KARUKIA = 30570;
    public final int KASMAN = 30501;
    public final int TAZEER = 31978;
    public final int GOBLIN_TOMB_RAIDER_LEADER = 20320;
    public final int KURUKA_RATMAN_LEADER = 27045;
    public final int UMBAR_ORC = 27054;
    public final int TIMORA_ORC = 27320;
    public final int GREEN_BLOOD = 1578;
    public final int GOBLIN_DWELLING_MAP = 1579;
    public final int KURUKA_RATMAN_TOOTH = 1580;
    public final int BETRAYER_UMBAR_REPORT = 1589;
    public final int HEAD_OF_BETRAYER = 1591;
    public final int TIMORA_ORCS_HEAD = 8544;
    public final int MARK_OF_RAIDER = 1592;

    public _414_PathToOrcRaider() {
        super(false);
        addStartNpc(30570);
        addTalkId(30501);
        addTalkId(31978);
        addKillId(20320);
        addKillId(27045);
        addKillId(27054);
        addKillId(27320);
        addQuestItem(1580);
        addQuestItem(1579);
        addQuestItem(1578);
        addQuestItem(1591);
        addQuestItem(1589);
        addQuestItem(8544);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("prefect_karukia_q0414_05.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.giveItems(1579, 1L);
            st.playSound("ItemSound.quest_accept");
        } else if ("to_Gludin".equalsIgnoreCase(event)) {
            htmltext = "prefect_karukia_q0414_07a.htm";
            st.takeItems(1580, -1L);
            st.takeItems(1579, -1L);
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1589, 1L);
            st.addRadar(-74490, 83275, -3374);
            st.setCond(3);
        } else if ("to_Schuttgart".equalsIgnoreCase(event)) {
            htmltext = "prefect_karukia_q0414_07b.htm";
            st.takeItems(1580, -1L);
            st.takeItems(1579, -1L);
            st.addRadar(90000, -143286, -1520);
            st.playSound("ItemSound.quest_middle");
            st.setCond(5);
        } else if ("prefect_tazar_q0414_02.htm".equalsIgnoreCase(event)) {
            st.addRadar(57502, -117576, -3700);
            st.setCond(6);
            st.playSound("ItemSound.quest_middle");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        final int playerClassID = st.getPlayer().getClassId().getId();
        final int playerLvl = st.getPlayer().getLevel();
        if (npcId == 30570) {
            if (cond < 1) {
                if (playerLvl >= 18 && playerClassID == 44 && st.getQuestItemsCount(1592) == 0L && st.getQuestItemsCount(1579) == 0L) {
                    htmltext = "prefect_karukia_q0414_01.htm";
                } else if (playerClassID != 44) {
                    if (playerClassID == 45) {
                        htmltext = "prefect_karukia_q0414_02a.htm";
                    } else {
                        htmltext = "prefect_karukia_q0414_03.htm";
                    }
                } else if (playerLvl < 18 && playerClassID == 44) {
                    htmltext = "prefect_karukia_q0414_02.htm";
                } else if (playerLvl >= 18 && playerClassID == 44 && st.getQuestItemsCount(1592) > 0L) {
                    htmltext = "prefect_karukia_q0414_04.htm";
                } else {
                    htmltext = "prefect_karukia_q0414_02.htm";
                }
            } else if (cond == 1 && st.getQuestItemsCount(1579) > 0L && st.getQuestItemsCount(1580) < 10L) {
                htmltext = "prefect_karukia_q0414_06.htm";
            } else if (cond == 2 && st.getQuestItemsCount(1579) > 0L && st.getQuestItemsCount(1580) > 9L) {
                htmltext = "prefect_karukia_q0414_07.htm";
            } else if (cond == 3 && st.getQuestItemsCount(1589) > 0L && st.getQuestItemsCount(1591) < 2L) {
                htmltext = "prefect_karukia_q0414_08.htm";
            } else if (cond == 4 && st.getQuestItemsCount(1589) > 0L && st.getQuestItemsCount(1591) == 2L) {
                htmltext = "prefect_karukia_q0414_09.htm";
            }
        } else if (npcId == 30501 && cond > 0) {
            if (cond == 3 && st.getQuestItemsCount(1589) > 0L && st.getQuestItemsCount(1591) < 1L) {
                htmltext = "prefect_kasman_q0414_01.htm";
            } else if (cond == 3 && st.getQuestItemsCount(1591) > 0L && st.getQuestItemsCount(1591) < 2L) {
                htmltext = "prefect_kasman_q0414_02.htm";
            } else if (cond == 4 && st.getQuestItemsCount(1591) > 1L) {
                htmltext = "prefect_kasman_q0414_03.htm";
                st.exitCurrentQuest(true);
                if (st.getPlayer().getClassId().getLevel() == 1) {
                    st.giveItems(1592, 1L);
                    if (!st.getPlayer().getVarB("prof1")) {
                        st.getPlayer().setVar("prof1", "1", -1L);
                        st.addExpAndSp(3200L, 3120L);
                    }
                }
                st.playSound("ItemSound.quest_finish");
            }
        } else if (npcId == 31978) {
            if (cond == 5) {
                htmltext = "prefect_tazar_q0414_01b.htm";
            } else if (cond == 6 && st.getQuestItemsCount(8544) < 1L) {
                htmltext = "prefect_tazar_q0414_03.htm";
            } else if (cond == 7 && st.getQuestItemsCount(8544) > 0L) {
                htmltext = "prefect_tazar_q0414_05.htm";
                st.exitCurrentQuest(true);
                if (st.getPlayer().getClassId().getLevel() == 1) {
                    st.giveItems(1592, 1L);
                    if (!st.getPlayer().getVarB("prof1")) {
                        st.getPlayer().setVar("prof1", "1", -1L);
                        st.addExpAndSp(228064L, 16455L);
                        st.giveItems(57, 81900L);
                    }
                }
                st.playSound("ItemSound.quest_finish");
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 20320 && cond == 1) {
            if (st.getQuestItemsCount(1579) == 1L && st.getQuestItemsCount(1580) < 10L && st.getQuestItemsCount(1578) < 40L) {
                if (st.getQuestItemsCount(1578) > 20L && Rnd.chance((double) ((st.getQuestItemsCount(1578) - 20L) * 5L))) {
                    st.takeItems(1578, -1L);
                    st.addSpawn(27045);
                } else {
                    st.giveItems(1578, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 27045 && cond == 1) {
            if (st.getQuestItemsCount(1579) > 0L && st.getQuestItemsCount(1580) < 10L) {
                st.giveItems(1580, 1L);
                if (st.getQuestItemsCount(1580) > 9L) {
                    st.setCond(2);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 27054 && cond == 3) {
            if (st.getQuestItemsCount(1589) > 0L && st.getQuestItemsCount(1591) < 2L) {
                st.giveItems(1591, 1L);
                if (st.getQuestItemsCount(1591) > 1L) {
                    st.setCond(4);
                    st.addRadar(-80450, 153410, -3175);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 27320 && cond == 6 && st.getQuestItemsCount(8544) < 1L && Rnd.chance(50)) {
            st.giveItems(8544, 1L);
            st.addRadar(90000, -143286, -1520);
            st.setCond(7);
            st.playSound("ItemSound.quest_middle");
        }
        return null;
    }
}
