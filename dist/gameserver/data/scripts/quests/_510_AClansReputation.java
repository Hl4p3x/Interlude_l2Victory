package quests;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

public class _510_AClansReputation extends Quest {
    private static final int VALDIS = 31331;
    private static final int CLAW = 8767;
    private static final int CLAN_POINTS_REWARD = 50;

    public _510_AClansReputation() {
        super(2);
        addStartNpc(31331);
        for (int npc = 22215; npc <= 22217; ++npc) {
            addKillId(npc);
        }
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int cond = st.getCond();
        if ("31331-3.htm".equals(event)) {
            if (cond == 0) {
                st.setCond(1);
                st.setState(2);
            }
        } else if ("31331-6.htm".equals(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final Player player = st.getPlayer();
        final Clan clan = player.getClan();
        if (player.getClan() == null || !player.isClanLeader()) {
            st.exitCurrentQuest(true);
            htmltext = "31331-0.htm";
        } else if (player.getClan().getLevel() < 5) {
            st.exitCurrentQuest(true);
            htmltext = "31331-0.htm";
        } else {
            final int cond = st.getCond();
            final int id = st.getState();
            if (id == 1 && cond == 0) {
                htmltext = "31331-1.htm";
            } else if (id == 2 && cond == 1) {
                final long count = st.getQuestItemsCount(8767);
                if (count == 0L) {
                    htmltext = "31331-4.htm";
                } else if (count >= 1L) {
                    htmltext = "31331-7.htm";
                    st.takeItems(8767, -1L);
                    final int pointsCount = 50 * (int) count * (int) Config.RATE_CLAN_REP_SCORE;
                    final int increasedPoints = clan.incReputation(pointsCount, true, "_510_AClansReputation");
                    player.sendPacket(new SystemMessage(1777).addNumber(increasedPoints));
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (!st.getPlayer().isClanLeader()) {
            st.exitCurrentQuest(true);
        } else if (st.getState() == 2) {
            final int npcId = npc.getNpcId();
            if (npcId >= 22215 && npcId <= 22218) {
                st.giveItems(8767, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
