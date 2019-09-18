package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _295_DreamsOfTheSkies extends Quest {
    public static int FLOATING_STONE = 1492;
    public static int RING_OF_FIREFLY = 1509;
    public static int Arin = 30536;
    public static int MagicalWeaver = 20153;

    public _295_DreamsOfTheSkies() {
        super(false);
        addStartNpc(_295_DreamsOfTheSkies.Arin);
        addTalkId(_295_DreamsOfTheSkies.Arin);
        addKillId(_295_DreamsOfTheSkies.MagicalWeaver);
        addQuestItem(_295_DreamsOfTheSkies.FLOATING_STONE);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("elder_arin_q0295_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int id = st.getState();
        if (id == 1) {
            st.setCond(0);
        }
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getLevel() >= 11) {
                htmltext = "elder_arin_q0295_02.htm";
                return htmltext;
            }
            htmltext = "elder_arin_q0295_01.htm";
            st.exitCurrentQuest(true);
        } else if (cond == 1 || st.getQuestItemsCount(_295_DreamsOfTheSkies.FLOATING_STONE) < 50L) {
            htmltext = "elder_arin_q0295_04.htm";
        } else if (cond == 2 && st.getQuestItemsCount(_295_DreamsOfTheSkies.FLOATING_STONE) == 50L) {
            st.addExpAndSp(0L, 500L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
            if (st.getQuestItemsCount(_295_DreamsOfTheSkies.RING_OF_FIREFLY) < 1L) {
                htmltext = "elder_arin_q0295_05.htm";
                st.giveItems(_295_DreamsOfTheSkies.RING_OF_FIREFLY, 1L);
            } else {
                htmltext = "elder_arin_q0295_06.htm";
                st.giveItems(57, 2400L);
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1 && st.getQuestItemsCount(_295_DreamsOfTheSkies.FLOATING_STONE) < 50L) {
            if (Rnd.chance(25)) {
                st.giveItems(_295_DreamsOfTheSkies.FLOATING_STONE, 1L);
                if (st.getQuestItemsCount(_295_DreamsOfTheSkies.FLOATING_STONE) == 50L) {
                    st.playSound("ItemSound.quest_middle");
                    st.setCond(2);
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            } else if (st.getQuestItemsCount(_295_DreamsOfTheSkies.FLOATING_STONE) >= 48L) {
                st.giveItems(_295_DreamsOfTheSkies.FLOATING_STONE, 50L - st.getQuestItemsCount(_295_DreamsOfTheSkies.FLOATING_STONE));
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.giveItems(_295_DreamsOfTheSkies.FLOATING_STONE, 2L);
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
