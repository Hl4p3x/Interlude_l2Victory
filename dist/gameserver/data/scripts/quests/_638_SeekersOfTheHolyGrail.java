package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _638_SeekersOfTheHolyGrail extends Quest {
    private static final int DROP_CHANCE = 10;
    private static final int INNOCENTIN = 31328;
    private static final int TOTEM = 8068;
    private static final int EAS = 960;
    private static final int EWS = 959;

    public _638_SeekersOfTheHolyGrail() {
        super(true);
        addStartNpc(31328);
        addQuestItem(8068);
        for (int i = 22137; i <= 22176; ++i) {
            addKillId(i);
        }
        addKillId(22194);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("highpriest_innocentin_q0638_03.htm".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("highpriest_innocentin_q0638_09.htm".equals(event)) {
            st.playSound("ItemSound.quest_giveup");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext;
        final int id = st.getState();
        if (id == 1) {
            if (st.getPlayer().getLevel() >= 73) {
                htmltext = "highpriest_innocentin_q0638_01.htm";
            } else {
                htmltext = "highpriest_innocentin_q0638_02.htm";
            }
        } else {
            htmltext = tryRevard(st);
        }
        return htmltext;
    }

    private String tryRevard(final QuestState st) {
        boolean ok = false;
        while (st.getQuestItemsCount(8068) >= 2000L) {
            st.takeItems(8068, 2000L);
            final int rnd = Rnd.get(100);
            if (rnd < 50) {
                st.giveItems(57, 3576000L, false);
            } else if (rnd < 85) {
                st.giveItems(960, 1L, false);
            } else {
                st.giveItems(959, 1L, false);
            }
            ok = true;
        }
        if (ok) {
            st.playSound("ItemSound.quest_middle");
            return "highpriest_innocentin_q0638_10.htm";
        }
        return "highpriest_innocentin_q0638_05.htm";
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        st.rollAndGive(8068, 1, 10.0 * npc.getTemplate().rateHp);
        if ((npc.getNpcId() == 22146 || npc.getNpcId() == 22151) && Rnd.chance(10)) {
            npc.dropItem(st.getPlayer(), 8275, 1L);
        }
        if ((npc.getNpcId() == 22140 || npc.getNpcId() == 22149) && Rnd.chance(10)) {
            npc.dropItem(st.getPlayer(), 8273, 1L);
        }
        if ((npc.getNpcId() == 22142 || npc.getNpcId() == 22143) && Rnd.chance(10)) {
            npc.dropItem(st.getPlayer(), 8274, 1L);
        }
        return null;
    }
}
