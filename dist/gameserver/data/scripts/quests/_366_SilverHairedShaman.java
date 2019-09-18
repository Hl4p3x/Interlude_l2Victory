package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _366_SilverHairedShaman extends Quest {
    private static final int DIETER = 30111;
    private static final int SAIRON = 20986;
    private static final int SAIRONS_DOLL = 20987;
    private static final int SAIRONS_PUPPET = 20988;
    private static final int ADENA_PER_ONE = 500;
    private static final int START_ADENA = 12070;
    private static final int SAIRONS_SILVER_HAIR = 5874;

    public _366_SilverHairedShaman() {
        super(false);
        addStartNpc(30111);
        addKillId(20986);
        addKillId(20987);
        addKillId(20988);
        addQuestItem(5874);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("30111-02.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("30111-quit.htm".equalsIgnoreCase(event)) {
            st.takeItems(5874, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        int cond = st.getCond();
        if (id == 1) {
            st.setCond(0);
        } else {
            cond = st.getCond();
        }
        if (npcId == 30111) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 48) {
                    htmltext = "30111-01.htm";
                } else {
                    htmltext = "30111-00.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1 && st.getQuestItemsCount(5874) == 0L) {
                htmltext = "30111-03.htm";
            } else if (cond == 1 && st.getQuestItemsCount(5874) >= 1L) {
                st.giveItems(57, st.getQuestItemsCount(5874) * 500L + 12070L);
                st.takeItems(5874, -1L);
                htmltext = "30111-have.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int cond = st.getCond();
        if (cond == 1 && Rnd.chance(66)) {
            st.giveItems(5874, 1L);
            st.playSound("ItemSound.quest_middle");
        }
        return null;
    }
}
