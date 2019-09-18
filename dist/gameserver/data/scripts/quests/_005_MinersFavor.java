package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _005_MinersFavor extends Quest {
    private final int BOLTER = 30554;
    private final int SHARI = 30517;
    private final int GARITA = 30518;
    private final int REED = 30520;
    private final int BRUNON = 30526;
    private final int BOLTERS_LIST = 1547;
    private final int MINING_BOOTS = 1548;
    private final int MINERS_PICK = 1549;
    private final int BOOMBOOM_POWDER = 1550;
    private final int REDSTONE_BEER = 1551;
    private final int BOLTERS_SMELLY_SOCKS = 1552;
    private final int NECKLACE = 906;

    public _005_MinersFavor() {
        super(false);
        addStartNpc(BOLTER);
        addTalkId(SHARI, GARITA, REED, BRUNON);
        addQuestItem(BOLTERS_LIST, BOLTERS_SMELLY_SOCKS, MINING_BOOTS, MINERS_PICK, BOOMBOOM_POWDER, REDSTONE_BEER);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("miner_bolter_q0005_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(BOLTERS_LIST, 1L, false);
            st.giveItems(BOLTERS_SMELLY_SOCKS, 1L, false);
        } else if ("blacksmith_bronp_q0005_02.htm".equalsIgnoreCase(event)) {
            st.takeItems(BOLTERS_SMELLY_SOCKS, -1L);
            st.giveItems(MINERS_PICK, 1L, false);
            if (st.getQuestItemsCount(BOLTERS_LIST) > 0L && st.getQuestItemsCount(MINING_BOOTS) + st.getQuestItemsCount(MINERS_PICK) + st.getQuestItemsCount(BOOMBOOM_POWDER) + st.getQuestItemsCount(REDSTONE_BEER) == 4L) {
                st.setCond(2);
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == BOLTER) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 2) {
                    htmltext = "miner_bolter_q0005_02.htm";
                } else {
                    htmltext = "miner_bolter_q0005_01.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "miner_bolter_q0005_04.htm";
            } else if (cond == 2 && st.getQuestItemsCount(MINING_BOOTS) + st.getQuestItemsCount(MINERS_PICK) + st.getQuestItemsCount(BOOMBOOM_POWDER) + st.getQuestItemsCount(REDSTONE_BEER) == 4L) {
                htmltext = "miner_bolter_q0005_06.htm";
                st.takeItems(MINING_BOOTS, -1L);
                st.takeItems(MINERS_PICK, -1L);
                st.takeItems(BOOMBOOM_POWDER, -1L);
                st.takeItems(REDSTONE_BEER, -1L);
                st.takeItems(BOLTERS_LIST, -1L);
                st.giveItems(NECKLACE, 1L, false);
                if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1")) {
                    st.getPlayer().sendPacket(new ExShowScreenMessage("  Delivery duty complete.\nGo find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                }
                st.giveItems(57, 2466L);
                st.unset("cond");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(false);
            }
        } else if (cond == 1 && st.getQuestItemsCount(BOLTERS_LIST) > 0L) {
            if (npcId == SHARI) {
                if (st.getQuestItemsCount(BOOMBOOM_POWDER) == 0L) {
                    htmltext = "trader_chali_q0005_01.htm";
                    st.giveItems(BOOMBOOM_POWDER, 1L, false);
                    st.playSound("ItemSound.quest_itemget");
                } else {
                    htmltext = "trader_chali_q0005_02.htm";
                }
            } else if (npcId == GARITA) {
                if (st.getQuestItemsCount(MINING_BOOTS) == 0L) {
                    htmltext = "trader_garita_q0005_01.htm";
                    st.giveItems(MINING_BOOTS, 1L, false);
                    st.playSound("ItemSound.quest_itemget");
                } else {
                    htmltext = "trader_garita_q0005_02.htm";
                }
            } else if (npcId == REED) {
                if (st.getQuestItemsCount(REDSTONE_BEER) == 0L) {
                    htmltext = "warehouse_chief_reed_q0005_01.htm";
                    st.giveItems(REDSTONE_BEER, 1L, false);
                    st.playSound("ItemSound.quest_itemget");
                } else {
                    htmltext = "warehouse_chief_reed_q0005_02.htm";
                }
            } else if (npcId == BRUNON && st.getQuestItemsCount(BOLTERS_SMELLY_SOCKS) > 0L) {
                if (st.getQuestItemsCount(MINERS_PICK) == 0L) {
                    htmltext = "blacksmith_bronp_q0005_01.htm";
                } else {
                    htmltext = "blacksmith_bronp_q0005_03.htm";
                }
            }
            if (st.getQuestItemsCount(BOLTERS_LIST) > 0L && st.getQuestItemsCount(MINING_BOOTS) + st.getQuestItemsCount(MINERS_PICK) + st.getQuestItemsCount(BOOMBOOM_POWDER) + st.getQuestItemsCount(REDSTONE_BEER) == 4L) {
                st.setCond(2);
                st.playSound("ItemSound.quest_middle");
            }
        }
        return htmltext;
    }
}
