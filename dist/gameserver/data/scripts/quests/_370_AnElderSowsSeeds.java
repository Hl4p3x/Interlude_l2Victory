package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.Arrays;

public class _370_AnElderSowsSeeds extends Quest {
    private static final int CASIAN = 30612;
    private static final int[] MOBS = {20082, 20084, 20086, 20089, 20090};
    private static final int SPB_PAGE = 5916;
    private static final int TELEPORT_SCROLL = 736;
    private static final int[] CHAPTERS = {5917, 5918, 5919, 5920};

    public _370_AnElderSowsSeeds() {
        super(false);
        addStartNpc(CASIAN);
        Arrays.stream(MOBS).forEach(this::addKillId);
        addQuestItem(SPB_PAGE);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30612-1.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("30612-6.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(CHAPTERS[0]) > 0L && st.getQuestItemsCount(CHAPTERS[1]) > 0L && st.getQuestItemsCount(CHAPTERS[2]) > 0L && st.getQuestItemsCount(CHAPTERS[3]) > 0L) {
                long mincount = st.getQuestItemsCount(CHAPTERS[0]);
                for (final int itemId : CHAPTERS) {
                    mincount = Math.min(mincount, st.getQuestItemsCount(itemId));
                }
                for (final int itemId : CHAPTERS) {
                    st.takeItems(itemId, mincount);
                }
                st.giveItems(57, 3600L * mincount);
                htmltext = "30612-8.htm";
            } else {
                htmltext = "30612-4.htm";
            }
        } else if ("30612-9.htm".equalsIgnoreCase(event)) {
            st.giveItems(TELEPORT_SCROLL, 1L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (st.getState() == 1) {
            if (st.getPlayer().getLevel() < 28) {
                htmltext = "30612-0a.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "30612-0.htm";
            }
        } else if (cond == 1) {
            htmltext = "30612-4.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() != 2) {
            return null;
        }
        if (Rnd.chance(Math.min((int) (15.0 * st.getRateQuestsReward()), 100))) {
            st.giveItems(SPB_PAGE, 1L);
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
