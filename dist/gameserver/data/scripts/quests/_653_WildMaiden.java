package quests;

import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Spawner;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.templates.spawn.PeriodOfDay;

import java.util.ArrayList;
import java.util.List;

public class _653_WildMaiden extends Quest {
    public final int SUKI = 32013;
    public final int GALIBREDO = 30181;
    public final int SOE = 736;

    public _653_WildMaiden() {
        super(false);
        addStartNpc(32013);
        addTalkId(32013);
        addTalkId(30181);
    }

    private NpcInstance findNpc(final int npcId, final Player player) {
        NpcInstance instance = null;
        final List<NpcInstance> npclist = new ArrayList<>();
        for (final Spawner spawn : SpawnManager.getInstance().getSpawners(PeriodOfDay.ALL.name())) {
            if (spawn.getCurrentNpcId() == npcId) {
                instance = spawn.getLastSpawn();
                npclist.add(instance);
            }
        }
        for (final NpcInstance npc : npclist) {
            if (player.isInRange(npc, 1600L)) {
                return npc;
            }
        }
        return instance;
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final Player player = st.getPlayer();
        if ("spring_girl_sooki_q0653_03.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(736) > 0L) {
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                st.takeItems(736, 1L);
                htmltext = "spring_girl_sooki_q0653_04a.htm";
                final NpcInstance n = findNpc(32013, player);
                n.broadcastPacket(new MagicSkillUse(n, n, 2013, 1, 20000, 0L));
                st.startQuestTimer("suki_timer", 20000L);
            }
        } else if ("spring_girl_sooki_q0653_03.htm".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(false);
            st.playSound("ItemSound.quest_giveup");
        } else if ("suki_timer".equalsIgnoreCase(event)) {
            final NpcInstance n = findNpc(32013, player);
            n.deleteMe();
            htmltext = null;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        if (npcId == 32013 && id == 1) {
            if (st.getPlayer().getLevel() >= 36) {
                htmltext = "spring_girl_sooki_q0653_01.htm";
            } else {
                htmltext = "spring_girl_sooki_q0653_01a.htm";
                st.exitCurrentQuest(false);
            }
        } else if (npcId == 30181 && st.getCond() == 1) {
            htmltext = "galicbredo_q0653_01.htm";
            st.giveItems(57, 2553L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return htmltext;
    }
}
