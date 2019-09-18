package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _004_LongLivethePaagrioLord extends Quest {
    private final int HONEY_KHANDAR = 1541;
    private final int BEAR_FUR_CLOAK = 1542;
    private final int BLOODY_AXE = 1543;
    private final int ANCESTOR_SKULL = 1544;
    private final int SPIDER_DUST = 1545;
    private final int DEEP_SEA_ORB = 1546;
    private final int CLUB = 14;
    private final int[][] NPC_GIFTS = {{30585, BEAR_FUR_CLOAK}, {30566, HONEY_KHANDAR}, {30562, BLOODY_AXE}, {30560, ANCESTOR_SKULL}, {30559, SPIDER_DUST}, {30587, DEEP_SEA_ORB}};

    public _004_LongLivethePaagrioLord() {
        super(false);
        addStartNpc(30578);
        addTalkId(30559, 30560, 30562, 30566, 30578, 30585, 30587);
        addQuestItem(SPIDER_DUST, ANCESTOR_SKULL, BLOODY_AXE, HONEY_KHANDAR, BEAR_FUR_CLOAK, DEEP_SEA_ORB);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("30578-03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 30578) {
            switch (cond) {
                case 0:
                    if (st.getPlayer().getRace() != Race.orc) {
                        htmltext = "30578-00.htm";
                        st.exitCurrentQuest(true);
                    } else if (st.getPlayer().getLevel() >= 2) {
                        htmltext = "30578-02.htm";
                    } else {
                        htmltext = "30578-01.htm";
                        st.exitCurrentQuest(true);
                    }
                    break;
                case 1:
                    htmltext = "30578-04.htm";
                    break;
                case 2:
                    htmltext = "30578-06.htm";
                    for (final int[] item : NPC_GIFTS) {
                        st.takeItems(item[1], -1L);
                    }
                    st.giveItems(CLUB, 1L, false);
                    if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1")) {
                        st.getPlayer().sendPacket(new ExShowScreenMessage("  Delivery duty complete.\nGo find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                    }
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                    break;
            }
        } else if (cond == 1) {
            for (final int[] Id : NPC_GIFTS) {
                if (Id[0] == npcId) {
                    final int item2 = Id[1];
                    if (st.getQuestItemsCount(item2) > 0L) {
                        htmltext = npcId + "-02.htm";
                    } else {
                        st.giveItems(item2, 1L, false);
                        htmltext = npcId + "-01.htm";
                        int count = 0;
                        for (final int[] item3 : NPC_GIFTS) {
                            count += (int) st.getQuestItemsCount(item3[1]);
                        }
                        if (count == 6) {
                            st.setCond(2);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    return htmltext;
                }
            }
        }
        return htmltext;
    }
}
