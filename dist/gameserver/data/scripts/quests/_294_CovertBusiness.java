package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _294_CovertBusiness extends Quest {
    public static int BatFang = 1491;
    public static int RingOfRaccoon = 1508;
    public static int BarbedBat = 20370;
    public static int BladeBat = 20480;
    public static int Keef = 30534;

    public _294_CovertBusiness() {
        super(false);
        addStartNpc(_294_CovertBusiness.Keef);
        addTalkId(_294_CovertBusiness.Keef);
        addKillId(_294_CovertBusiness.BarbedBat);
        addKillId(_294_CovertBusiness.BladeBat);
        addQuestItem(_294_CovertBusiness.BatFang);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("elder_keef_q0294_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext;
        final int id = st.getState();
        if (id == 1) {
            if (st.getPlayer().getRace() != Race.dwarf) {
                htmltext = "elder_keef_q0294_00.htm";
                st.exitCurrentQuest(true);
            } else {
                if (st.getPlayer().getLevel() >= 10) {
                    htmltext = "elder_keef_q0294_02.htm";
                    return htmltext;
                }
                htmltext = "elder_keef_q0294_01.htm";
                st.exitCurrentQuest(true);
            }
        } else if (st.getQuestItemsCount(_294_CovertBusiness.BatFang) < 100L) {
            htmltext = "elder_keef_q0294_04.htm";
        } else {
            if (st.getQuestItemsCount(_294_CovertBusiness.RingOfRaccoon) < 1L) {
                st.giveItems(_294_CovertBusiness.RingOfRaccoon, 1L);
                htmltext = "elder_keef_q0294_05.htm";
            } else {
                st.giveItems(57, 2400L);
                htmltext = "elder_keef_q0294_06.htm";
            }
            st.addExpAndSp(0L, 600L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1) {
            st.rollAndGive(_294_CovertBusiness.BatFang, 1, 2, 100, 100.0);
        }
        return null;
    }
}
