package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _002_WhatWomenWant extends Quest {
    private final int ARUJIEN = 30223;
    private final int MIRABEL = 30146;
    private final int HERBIEL = 30150;
    private final int GREENIS = 30157;
    private final int ARUJIENS_LETTER1 = 1092;
    private final int ARUJIENS_LETTER2 = 1093;
    private final int ARUJIENS_LETTER3 = 1094;
    private final int POETRY_BOOK = 689;
    private final int GREENIS_LETTER = 693;
    private final int MYSTICS_EARRING = 113;

    public _002_WhatWomenWant() {
        super(false);
        addStartNpc(ARUJIEN);
        addTalkId(MIRABEL);
        addTalkId(HERBIEL);
        addTalkId(GREENIS);
        addQuestItem(GREENIS_LETTER, ARUJIENS_LETTER3, ARUJIENS_LETTER1, ARUJIENS_LETTER2, POETRY_BOOK);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            htmltext = "arujien_q0002_04.htm";
            st.giveItems(ARUJIENS_LETTER1, 1L, false);
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("2_1".equalsIgnoreCase(event)) {
            htmltext = "arujien_q0002_08.htm";
            st.takeItems(ARUJIENS_LETTER3, -1L);
            st.giveItems(POETRY_BOOK, 1L, false);
            st.setCond(4);
            st.playSound("ItemSound.quest_middle");
        } else if ("2_2".equalsIgnoreCase(event)) {
            htmltext = "arujien_q0002_09.htm";
            st.takeItems(ARUJIENS_LETTER3, -1L);
            st.giveItems(57, 450L, true);
            if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1")) {
                st.getPlayer().sendPacket(new ExShowScreenMessage("  Delivery duty complete.\nGo find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
            }
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        switch (npcId) {
            case ARUJIEN:
                if (cond == 0) {
                    if (st.getPlayer().getRace() != Race.elf && st.getPlayer().getRace() != Race.human) {
                        htmltext = "arujien_q0002_00.htm";
                    } else if (st.getPlayer().getLevel() >= 2) {
                        htmltext = "arujien_q0002_02.htm";
                    } else {
                        htmltext = "arujien_q0002_01.htm";
                        st.exitCurrentQuest(true);
                    }
                } else if (cond == 1 && st.getQuestItemsCount(ARUJIENS_LETTER1) > 0L) {
                    htmltext = "arujien_q0002_05.htm";
                } else if (cond == 2 && st.getQuestItemsCount(ARUJIENS_LETTER2) > 0L) {
                    htmltext = "arujien_q0002_06.htm";
                } else if (cond == 3 && st.getQuestItemsCount(ARUJIENS_LETTER3) > 0L) {
                    htmltext = "arujien_q0002_07.htm";
                } else if (cond == 4 && st.getQuestItemsCount(POETRY_BOOK) > 0L) {
                    htmltext = "arujien_q0002_11.htm";
                } else if (cond == 5 && st.getQuestItemsCount(GREENIS_LETTER) > 0L) {
                    htmltext = "arujien_q0002_09.htm";
                    st.takeItems(GREENIS_LETTER, -1L);
                    st.giveItems(MYSTICS_EARRING, 1L, false);
                    if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1")) {
                        st.getPlayer().sendPacket(new ExShowScreenMessage("  Delivery duty complete.\nGo find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                    }
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                }
                break;
            case MIRABEL:
                if (cond == 1 && st.getQuestItemsCount(ARUJIENS_LETTER1) > 0L) {
                    htmltext = "mint_q0002_01.htm";
                    st.takeItems(ARUJIENS_LETTER1, -1L);
                    st.giveItems(ARUJIENS_LETTER2, 1L, false);
                    st.setCond(2);
                    st.playSound("ItemSound.quest_middle");
                } else if (cond == 2) {
                    htmltext = "mint_q0002_02.htm";
                }
                break;
            case HERBIEL:
                if (cond == 2 && st.getQuestItemsCount(ARUJIENS_LETTER2) > 0L) {
                    htmltext = "green_q0002_01.htm";
                    st.takeItems(ARUJIENS_LETTER2, -1L);
                    st.giveItems(ARUJIENS_LETTER3, 1L, false);
                    st.setCond(3);
                    st.playSound("ItemSound.quest_middle");
                } else if (cond == 3) {
                    htmltext = "green_q0002_02.htm";
                }
                break;
            case GREENIS:
                if (cond == 4 && st.getQuestItemsCount(POETRY_BOOK) > 0L) {
                    htmltext = "grain_q0002_02.htm";
                    st.takeItems(POETRY_BOOK, -1L);
                    st.giveItems(GREENIS_LETTER, 1L, false);
                    st.setCond(5);
                    st.playSound("ItemSound.quest_middle");
                } else if (cond == 5 && st.getQuestItemsCount(GREENIS_LETTER) > 0L) {
                    htmltext = "grain_q0002_03.htm";
                } else {
                    htmltext = "grain_q0002_01.htm";
                }
                break;
        }
        return htmltext;
    }
}
