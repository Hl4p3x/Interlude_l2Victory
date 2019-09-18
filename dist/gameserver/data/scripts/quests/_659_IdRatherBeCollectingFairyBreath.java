package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _659_IdRatherBeCollectingFairyBreath extends Quest {
    public final int GALATEA = 30634;
    public final int[] MOBS;
    public final int FAIRY_BREATH = 8286;

    public _659_IdRatherBeCollectingFairyBreath() {
        super(false);
        MOBS = new int[]{20078, 21026, 21025, 21024, 21023};
        addStartNpc(30634);
        addTalkId(30634);
        addTalkId(30634);
        addTalkId(30634);
        for (final int i : MOBS) {
            addKillId(i);
        }
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("high_summoner_galatea_q0659_0103.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("high_summoner_galatea_q0659_0203.htm".equalsIgnoreCase(event)) {
            final long count = st.getQuestItemsCount(8286);
            if (count > 0L) {
                long reward;
                if (count < 10L) {
                    reward = count * 50L;
                } else {
                    reward = count * 50L + 5365L;
                }
                st.takeItems(8286, -1L);
                st.giveItems(57, reward);
            }
        } else if ("high_summoner_galatea_q0659_0204.htm".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        int cond = 0;
        if (id != 1) {
            cond = st.getCond();
        }
        if (npcId == 30634) {
            if (st.getPlayer().getLevel() < 26) {
                htmltext = "high_summoner_galatea_q0659_0102.htm";
                st.exitCurrentQuest(true);
            } else if (cond == 0) {
                htmltext = "high_summoner_galatea_q0659_0101.htm";
            } else if (cond == 1) {
                if (st.getQuestItemsCount(8286) == 0L) {
                    htmltext = "high_summoner_galatea_q0659_0105.htm";
                } else {
                    htmltext = "high_summoner_galatea_q0659_0105.htm";
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (cond == 1) {
            for (final int i : MOBS) {
                if (npcId == i && Rnd.chance(30)) {
                    st.giveItems(8286, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
