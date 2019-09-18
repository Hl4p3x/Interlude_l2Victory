package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _341_HuntingForWildBeasts extends Quest {
    private static final int PANO = 30078;
    private static final int Red_Bear = 20021;
    private static final int Dion_Grizzly = 20203;
    private static final int Brown_Bear = 20310;
    private static final int Grizzly_Bear = 20335;
    private static final int BEAR_SKIN = 4259;
    private static final int BEAR_SKIN_CHANCE = 40;

    public _341_HuntingForWildBeasts() {
        super(false);
        addStartNpc(PANO);
        addKillId(Red_Bear);
        addKillId(Dion_Grizzly);
        addKillId(Brown_Bear);
        addKillId(Grizzly_Bear);
        addQuestItem(BEAR_SKIN);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event) && st.getState() == 1) {
            htmltext = "pano_q0341_04.htm";
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        if (npc.getNpcId() != PANO) {
            return htmltext;
        }
        final int _state = st.getState();
        if (_state == 1) {
            if (st.getPlayer().getLevel() >= 20) {
                htmltext = "pano_q0341_01.htm";
                st.setCond(0);
            } else {
                htmltext = "pano_q0341_02.htm";
                st.exitCurrentQuest(true);
            }
        } else if (_state == 2) {
            if (st.getQuestItemsCount(BEAR_SKIN) >= 20L) {
                htmltext = "pano_q0341_05.htm";
                st.takeItems(BEAR_SKIN, -1L);
                st.giveItems(57, 3710L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            } else {
                htmltext = "pano_q0341_06.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2) {
            return null;
        }
        final long BEAR_SKIN_COUNT = qs.getQuestItemsCount(BEAR_SKIN);
        if (BEAR_SKIN_COUNT < 20L && Rnd.chance(BEAR_SKIN_CHANCE)) {
            qs.giveItems(BEAR_SKIN, 1L);
            if (BEAR_SKIN_COUNT == 19L) {
                qs.setCond(2);
                qs.playSound("ItemSound.quest_middle");
            } else {
                qs.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }

    
}
