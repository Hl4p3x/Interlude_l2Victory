package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _267_WrathOfVerdure extends Quest {
    private static final int Treant_Bremec = 31853;
    private static final int Goblin_Raider = 20325;
    private static final int Goblin_Club = 1335;
    private static final int Silvery_Leaf = 1340;
    private static final int Goblin_Club_Chance = 50;

    public _267_WrathOfVerdure() {
        super(false);
        addStartNpc(Treant_Bremec);
        addKillId(Goblin_Raider);
        addQuestItem(Goblin_Club);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        if ("bri_mec_tran_q0267_03.htm".equalsIgnoreCase(event) && _state == 1 && st.getPlayer().getRace() == Race.elf && st.getPlayer().getLevel() >= 4) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("bri_mec_tran_q0267_06.htm".equalsIgnoreCase(event) && _state == 2) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        if (npc.getNpcId() != Treant_Bremec) {
            return htmltext;
        }
        final int _state = st.getState();
        if (_state == 1) {
            if (st.getPlayer().getRace() != Race.elf) {
                htmltext = "bri_mec_tran_q0267_00.htm";
                st.exitCurrentQuest(true);
            } else if (st.getPlayer().getLevel() < 4) {
                htmltext = "bri_mec_tran_q0267_01.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "bri_mec_tran_q0267_02.htm";
                st.setCond(0);
            }
        } else if (_state == 2) {
            final long Goblin_Club_Count = st.getQuestItemsCount(Goblin_Club);
            if (Goblin_Club_Count > 0L) {
                htmltext = "bri_mec_tran_q0267_05.htm";
                st.takeItems(Goblin_Club, -1L);
                st.giveItems(Silvery_Leaf, Goblin_Club_Count);
                st.playSound("ItemSound.quest_middle");
            } else {
                htmltext = "bri_mec_tran_q0267_04.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2) {
            return null;
        }
        if (Rnd.chance(Goblin_Club_Chance)) {
            qs.giveItems(Goblin_Club, 1L);
            qs.playSound("ItemSound.quest_itemget");
        }
        return null;
    }

    
}
