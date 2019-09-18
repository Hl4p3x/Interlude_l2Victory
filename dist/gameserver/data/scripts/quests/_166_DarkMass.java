package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _166_DarkMass extends Quest {
    int UNDRES_LETTER_ID;
    int CEREMONIAL_DAGGER_ID;
    int DREVIANT_WINE_ID;
    int GARMIELS_SCRIPTURE_ID;

    public _166_DarkMass() {
        super(false);
        UNDRES_LETTER_ID = 1088;
        CEREMONIAL_DAGGER_ID = 1089;
        DREVIANT_WINE_ID = 1090;
        GARMIELS_SCRIPTURE_ID = 1091;
        addStartNpc(30130);
        addTalkId(30135, 30139, 30143);
        addQuestItem(CEREMONIAL_DAGGER_ID, DREVIANT_WINE_ID, GARMIELS_SCRIPTURE_ID, UNDRES_LETTER_ID);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            htmltext = "30130-04.htm";
            st.giveItems(UNDRES_LETTER_ID, 1L);
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        final int cond = st.getCond();
        switch (npcId) {
            case 30130:
                if (id == 1) {
                    if (st.getPlayer().getRace() != Race.darkelf && st.getPlayer().getRace() != Race.human) {
                        htmltext = "30130-00.htm";
                    } else {
                        if (st.getPlayer().getLevel() >= 2) {
                            htmltext = "30130-03.htm";
                            return htmltext;
                        }
                        htmltext = "30130-02.htm";
                        st.exitCurrentQuest(true);
                    }
                } else if (cond == 1) {
                    htmltext = "30130-05.htm";
                } else if (cond == 2) {
                    htmltext = "30130-06.htm";
                    st.takeItems(UNDRES_LETTER_ID, -1L);
                    st.takeItems(CEREMONIAL_DAGGER_ID, -1L);
                    st.takeItems(DREVIANT_WINE_ID, -1L);
                    st.takeItems(GARMIELS_SCRIPTURE_ID, -1L);
                    st.giveItems(57, 500L);
                    if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1")) {
                        st.getPlayer().sendPacket(new ExShowScreenMessage("  Delivery duty complete.\nGo find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                    }
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                }
                break;
            case 30135:
                if (cond == 1 && st.getQuestItemsCount(CEREMONIAL_DAGGER_ID) == 0L) {
                    giveItem(st, CEREMONIAL_DAGGER_ID);
                    htmltext = "30135-01.htm";
                } else {
                    htmltext = "30135-02.htm";
                }
                break;
            case 30139:
                if (cond == 1 && st.getQuestItemsCount(DREVIANT_WINE_ID) == 0L) {
                    giveItem(st, DREVIANT_WINE_ID);
                    htmltext = "30139-01.htm";
                } else {
                    htmltext = "30139-02.htm";
                }
                break;
            case 30143:
                if (cond == 1 && st.getQuestItemsCount(GARMIELS_SCRIPTURE_ID) == 0L) {
                    giveItem(st, GARMIELS_SCRIPTURE_ID);
                    htmltext = "30143-01.htm";
                } else {
                    htmltext = "30143-02.htm";
                }
                break;
        }
        return htmltext;
    }

    private void giveItem(final QuestState st, final int item) {
        st.giveItems(item, 1L);
        if (st.getQuestItemsCount(CEREMONIAL_DAGGER_ID) >= 1L && st.getQuestItemsCount(DREVIANT_WINE_ID) >= 1L && st.getQuestItemsCount(GARMIELS_SCRIPTURE_ID) >= 1L) {
            st.setCond(2);
        }
    }
}
