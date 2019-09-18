package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _296_SilkOfTarantula extends Quest {
    private static final int TARANTULA_SPIDER_SILK = 1493;
    private static final int TARANTULA_SPINNERETTE = 1494;
    private static final int RING_OF_RACCOON = 1508;
    private static final int RING_OF_FIREFLY = 1509;

    public _296_SilkOfTarantula() {
        super(false);
        addStartNpc(30519);
        addTalkId(30548);
        addKillId(20394);
        addKillId(20403);
        addKillId(20508);
        addQuestItem(1493);
        addQuestItem(1494);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("trader_mion_q0296_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("quit".equalsIgnoreCase(event)) {
            htmltext = "trader_mion_q0296_06.htm";
            st.takeItems(1494, -1L);
            st.exitCurrentQuest(true);
            st.playSound("ItemSound.quest_finish");
        } else if ("exchange".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(1494) >= 1L) {
                htmltext = "defender_nathan_q0296_03.htm";
                st.giveItems(1493, 17L);
                st.takeItems(1494, -1L);
            } else {
                htmltext = "defender_nathan_q0296_02.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 30519) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 15) {
                    if (st.getQuestItemsCount(1508) <= 0L && st.getQuestItemsCount(1509) <= 0L) {
                        htmltext = "trader_mion_q0296_08.htm";
                        return htmltext;
                    }
                    htmltext = "trader_mion_q0296_02.htm";
                } else {
                    htmltext = "trader_mion_q0296_01.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                if (st.getQuestItemsCount(1493) < 1L) {
                    htmltext = "trader_mion_q0296_04.htm";
                } else if (st.getQuestItemsCount(1493) >= 1L) {
                    htmltext = "trader_mion_q0296_05.htm";
                    st.giveItems(57, st.getQuestItemsCount(1493) * 23L);
                    st.takeItems(1493, -1L);
                    if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q4")) {
                        st.getPlayer().setVar("p1q4", "1", -1L);
                        st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                    }
                }
            }
        } else if (npcId == 30548 && cond == 1) {
            htmltext = "defender_nathan_q0296_01.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1) {
            if (Rnd.chance(50)) {
                st.rollAndGive(1494, 1, 45.0);
            } else {
                st.rollAndGive(1493, 1, 45.0);
            }
        }
        return null;
    }
}
