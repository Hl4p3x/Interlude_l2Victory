package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.data.xml.holder.MultiSellHolder;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.HashMap;
import java.util.Map;

public class _382_KailsMagicCoin extends Quest {
    private static final Map<Integer, int[]> MOBS = new HashMap<>();
    private static final int ROYAL_MEMBERSHIP = 5898;
    private static final int VERGARA = 30687;

    static {
        MOBS.put(21017, new int[]{5961});
        MOBS.put(21019, new int[]{5962});
        MOBS.put(21020, new int[]{5963});
        MOBS.put(21022, new int[]{5961, 5962, 5963});
    }

    public _382_KailsMagicCoin() {
        super(false);
        addStartNpc(VERGARA);
        MOBS.keySet().forEach(this::addKillId);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("head_blacksmith_vergara_q0382_03.htm".equalsIgnoreCase(event)) {
            if (st.getPlayer().getLevel() >= 55 && st.getQuestItemsCount(ROYAL_MEMBERSHIP) > 0L) {
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "head_blacksmith_vergara_q0382_01.htm";
                st.exitCurrentQuest(true);
            }
        } else if ("list".equalsIgnoreCase(event)) {
            MultiSellHolder.getInstance().SeparateAndSend(382, st.getPlayer(), 0.0);
            htmltext = null;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext;
        final int cond = st.getCond();
        if (st.getQuestItemsCount(ROYAL_MEMBERSHIP) == 0L || st.getPlayer().getLevel() < 55) {
            htmltext = "head_blacksmith_vergara_q0382_01.htm";
            st.exitCurrentQuest(true);
        } else if (cond == 0) {
            htmltext = "head_blacksmith_vergara_q0382_02.htm";
        } else {
            htmltext = "head_blacksmith_vergara_q0382_04.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() != 2 || st.getQuestItemsCount(ROYAL_MEMBERSHIP) == 0L) {
            return null;
        }
        final int[] droplist = MOBS.get(npc.getNpcId());
        st.rollAndGive(droplist[Rnd.get(droplist.length)], 1, 10.0);
        return null;
    }
}
