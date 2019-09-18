package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _106_ForgottenTruth extends Quest {
    int ONYX_TALISMAN1;
    int ONYX_TALISMAN2;
    int ANCIENT_SCROLL;
    int ANCIENT_CLAY_TABLET;
    int KARTAS_TRANSLATION;
    int ELDRITCH_DAGGER;
    int ELDRITCH_STAFF;

    public _106_ForgottenTruth() {
        super(false);
        ONYX_TALISMAN1 = 984;
        ONYX_TALISMAN2 = 985;
        ANCIENT_SCROLL = 986;
        ANCIENT_CLAY_TABLET = 987;
        KARTAS_TRANSLATION = 988;
        ELDRITCH_DAGGER = 989;
        ELDRITCH_STAFF = 2373;
        addStartNpc(30358);
        addTalkId(30133);
        addKillId(27070);
        addQuestItem(KARTAS_TRANSLATION, ONYX_TALISMAN1, ONYX_TALISMAN2, ANCIENT_SCROLL, ANCIENT_CLAY_TABLET);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("tetrarch_thifiell_q0106_05.htm".equals(event)) {
            st.giveItems(ONYX_TALISMAN1, 1L);
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30358) {
            if (cond == 0) {
                if (st.getPlayer().getRace() != Race.darkelf) {
                    htmltext = "tetrarch_thifiell_q0106_00.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getPlayer().getLevel() >= 10) {
                    htmltext = "tetrarch_thifiell_q0106_03.htm";
                } else {
                    htmltext = "tetrarch_thifiell_q0106_02.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond > 0 && (st.getQuestItemsCount(ONYX_TALISMAN1) > 0L || st.getQuestItemsCount(ONYX_TALISMAN2) > 0L) && st.getQuestItemsCount(KARTAS_TRANSLATION) == 0L) {
                htmltext = "tetrarch_thifiell_q0106_06.htm";
            } else if (cond == 4 && st.getQuestItemsCount(KARTAS_TRANSLATION) > 0L) {
                htmltext = "tetrarch_thifiell_q0106_07.htm";
                st.takeItems(KARTAS_TRANSLATION, -1L);
                if (st.getPlayer().isMageClass()) {
                    st.giveItems(ELDRITCH_STAFF, 1L);
                } else {
                    st.giveItems(ELDRITCH_DAGGER, 1L);
                }
                st.giveItems(57, 10266L, false);
                st.getPlayer().addExpAndSp(24195L, 2074L);
                if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q3")) {
                    st.getPlayer().setVar("p1q3", "1", -1L);
                    st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                    st.giveItems(1060, 100L);
                    for (int item = 4412; item <= 4417; ++item) {
                        st.giveItems(item, 10L);
                    }
                    if (st.getPlayer().getClassId().isMage()) {
                        st.playTutorialVoice("tutorial_voice_027");
                        st.giveItems(5790, 3000L);
                    } else {
                        st.playTutorialVoice("tutorial_voice_026");
                        st.giveItems(5789, 6000L);
                    }
                }
                st.exitCurrentQuest(false);
                st.playSound("ItemSound.quest_finish");
            }
        } else if (npcId == 30133) {
            if (cond == 1 && st.getQuestItemsCount(ONYX_TALISMAN1) > 0L) {
                htmltext = "karta_q0106_01.htm";
                st.takeItems(ONYX_TALISMAN1, -1L);
                st.giveItems(ONYX_TALISMAN2, 1L);
                st.setCond(2);
            } else if (cond == 2 && st.getQuestItemsCount(ONYX_TALISMAN2) > 0L && (st.getQuestItemsCount(ANCIENT_SCROLL) == 0L || st.getQuestItemsCount(ANCIENT_CLAY_TABLET) == 0L)) {
                htmltext = "karta_q0106_02.htm";
            } else if (cond == 3 && st.getQuestItemsCount(ANCIENT_SCROLL) > 0L && st.getQuestItemsCount(ANCIENT_CLAY_TABLET) > 0L) {
                htmltext = "karta_q0106_03.htm";
                st.takeItems(ONYX_TALISMAN2, -1L);
                st.takeItems(ANCIENT_SCROLL, -1L);
                st.takeItems(ANCIENT_CLAY_TABLET, -1L);
                st.giveItems(KARTAS_TRANSLATION, 1L);
                st.setCond(4);
            } else if (cond == 4 && st.getQuestItemsCount(KARTAS_TRANSLATION) > 0L) {
                htmltext = "karta_q0106_04.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (npcId == 27070 && st.getCond() == 2 && st.getQuestItemsCount(ONYX_TALISMAN2) > 0L) {
            if (Rnd.chance(20) && st.getQuestItemsCount(ANCIENT_SCROLL) == 0L) {
                st.giveItems(ANCIENT_SCROLL, 1L);
                st.playSound("ItemSound.quest_middle");
            } else if (Rnd.chance(10) && st.getQuestItemsCount(ANCIENT_CLAY_TABLET) == 0L) {
                st.giveItems(ANCIENT_CLAY_TABLET, 1L);
                st.playSound("ItemSound.quest_middle");
            }
        }
        if (st.getQuestItemsCount(ANCIENT_SCROLL) > 0L && st.getQuestItemsCount(ANCIENT_CLAY_TABLET) > 0L) {
            st.setCond(3);
        }
        return null;
    }
}
