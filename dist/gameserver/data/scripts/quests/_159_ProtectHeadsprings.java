package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _159_ProtectHeadsprings extends Quest {
    int PLAGUE_DUST_ID;
    int HYACINTH_CHARM1_ID;
    int HYACINTH_CHARM2_ID;

    public _159_ProtectHeadsprings() {
        super(false);
        PLAGUE_DUST_ID = 1035;
        HYACINTH_CHARM1_ID = 1071;
        HYACINTH_CHARM2_ID = 1072;
        addStartNpc(30154);
        addKillId(27017);
        addQuestItem(PLAGUE_DUST_ID, HYACINTH_CHARM1_ID, HYACINTH_CHARM2_ID);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            if (st.getQuestItemsCount(HYACINTH_CHARM1_ID) == 0L) {
                st.giveItems(HYACINTH_CHARM1_ID, 1L);
                htmltext = "30154-04.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (cond) {
            case 0:
                if (st.getPlayer().getRace() != Race.elf) {
                    htmltext = "30154-00.htm";
                    st.exitCurrentQuest(true);
                } else {
                    if (st.getPlayer().getLevel() >= 12) {
                        htmltext = "30154-03.htm";
                        return htmltext;
                    }
                    htmltext = "30154-02.htm";
                    st.exitCurrentQuest(true);
                }
                break;
            case 1:
                htmltext = "30154-05.htm";
                break;
            case 2:
                st.takeItems(PLAGUE_DUST_ID, -1L);
                st.takeItems(HYACINTH_CHARM1_ID, -1L);
                st.giveItems(HYACINTH_CHARM2_ID, 1L);
                st.setCond(3);
                htmltext = "30154-06.htm";
                break;
            case 3:
                htmltext = "30154-07.htm";
                break;
            case 4:
                st.takeItems(PLAGUE_DUST_ID, -1L);
                st.takeItems(HYACINTH_CHARM2_ID, -1L);
                st.giveItems(57, 18250L);
                st.playSound("ItemSound.quest_finish");
                htmltext = "30154-08.htm";
                st.exitCurrentQuest(false);
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int cond = st.getCond();
        if (cond == 1 && Rnd.chance(60)) {
            st.giveItems(PLAGUE_DUST_ID, 1L);
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
        } else if (cond == 3 && Rnd.chance(60)) {
            if (st.getQuestItemsCount(PLAGUE_DUST_ID) == 4L) {
                st.giveItems(PLAGUE_DUST_ID, 1L);
                st.setCond(4);
                st.playSound("ItemSound.quest_middle");
            } else {
                st.giveItems(PLAGUE_DUST_ID, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
