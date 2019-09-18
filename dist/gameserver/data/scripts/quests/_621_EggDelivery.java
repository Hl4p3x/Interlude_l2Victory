package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _621_EggDelivery extends Quest {
    private static final int BoiledEgg = 7206;
    private static final int FeeOfBoiledEgg = 7196;
    private static final int HastePotion = 734;
    private static final int RecipeSealedTateossianRing = 6849;
    private static final int RecipeSealedTateossianEarring = 6847;
    private static final int RecipeSealedTateossianNecklace = 6851;
    private static final int JEREMY = 31521;
    private static final int VALENTINE = 31584;
    private static final int PULIN = 31543;
    private static final int NAFF = 31544;
    private static final int CROCUS = 31545;
    private static final int KUBER = 31546;
    private static final int BEOLIN = 31547;
    private static final int Tateossian_CHANCE = 20;

    public _621_EggDelivery() {
        super(false);
        addStartNpc(JEREMY);
        addTalkId(VALENTINE);
        addTalkId(PULIN);
        addTalkId(NAFF);
        addTalkId(CROCUS);
        addTalkId(KUBER);
        addTalkId(BEOLIN);
        addQuestItem(7206);
        addQuestItem(7196);
    }

    private static void takeEgg(final QuestState st, final int setcond) {
        st.setCond(setcond);
        st.takeItems(7206, 1L);
        st.giveItems(7196, 1L);
        st.playSound("ItemSound.quest_middle");
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        final int cond = st.getCond();
        final long BoiledEgg_count = st.getQuestItemsCount(7206);
        if ("jeremy_q0621_0104.htm".equalsIgnoreCase(event) && _state == 1) {
            st.takeItems(7206, -1L);
            st.takeItems(7196, -1L);
            st.setState(2);
            st.setCond(1);
            st.giveItems(7206, 5L);
            st.playSound("ItemSound.quest_accept");
        } else if ("pulin_q0621_0201.htm".equalsIgnoreCase(event) && cond == 1 && BoiledEgg_count > 0L) {
            takeEgg(st, 2);
        } else if ("naff_q0621_0301.htm".equalsIgnoreCase(event) && cond == 2 && BoiledEgg_count > 0L) {
            takeEgg(st, 3);
        } else if ("crocus_q0621_0401.htm".equalsIgnoreCase(event) && cond == 3 && BoiledEgg_count > 0L) {
            takeEgg(st, 4);
        } else if ("kuber_q0621_0501.htm".equalsIgnoreCase(event) && cond == 4 && BoiledEgg_count > 0L) {
            takeEgg(st, 5);
        } else if ("beolin_q0621_0601.htm".equalsIgnoreCase(event) && cond == 5 && BoiledEgg_count > 0L) {
            takeEgg(st, 6);
        } else if ("jeremy_q0621_0701.htm".equalsIgnoreCase(event) && cond == 6 && st.getQuestItemsCount(7196) >= 5L) {
            st.setCond(7);
        } else if ("brewer_valentine_q0621_0801.htm".equalsIgnoreCase(event) && cond == 7 && st.getQuestItemsCount(7196) >= 5L) {
            st.takeItems(7206, -1L);
            st.takeItems(7196, -1L);
            if (Rnd.chance(Tateossian_CHANCE)) {
                if (Rnd.chance(40)) {
                    st.giveItems(6849, 1L);
                } else if (Rnd.chance(40)) {
                    st.giveItems(6847, 1L);
                } else {
                    st.giveItems(6851, 1L);
                }
            } else {
                st.giveItems(57, 18800L);
                st.giveItems(734, 1L, true);
            }
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        if (st.getState() != 1) {
            final int cond = st.getCond();
            final long BoiledEgg_count = st.getQuestItemsCount(7206);
            final long FeeOfBoiledEgg_count = st.getQuestItemsCount(7196);
            if (cond == 1 && npcId == PULIN && BoiledEgg_count > 0L) {
                htmltext = "pulin_q0621_0101.htm";
            }
            if (cond == 2 && npcId == NAFF && BoiledEgg_count > 0L) {
                htmltext = "naff_q0621_0201.htm";
            }
            if (cond == 3 && npcId == CROCUS && BoiledEgg_count > 0L) {
                htmltext = "crocus_q0621_0301.htm";
            }
            if (cond == 4 && npcId == KUBER && BoiledEgg_count > 0L) {
                htmltext = "kuber_q0621_0401.htm";
            }
            if (cond == 5 && npcId == BEOLIN && BoiledEgg_count > 0L) {
                htmltext = "beolin_q0621_0501.htm";
            }
            if (cond == 6 && npcId == JEREMY && FeeOfBoiledEgg_count >= 5L) {
                htmltext = "jeremy_q0621_0601.htm";
            }
            if (cond == 7 && npcId == JEREMY && FeeOfBoiledEgg_count >= 5L) {
                htmltext = "jeremy_q0621_0703.htm";
            }
            if (cond == 7 && npcId == VALENTINE && FeeOfBoiledEgg_count >= 5L) {
                htmltext = "brewer_valentine_q0621_0701.htm";
            } else if (cond > 0 && npcId == JEREMY && BoiledEgg_count > 0L) {
                htmltext = "jeremy_q0621_0104.htm";
            }
            return htmltext;
        }
        if (npcId != JEREMY) {
            return htmltext;
        }
        if (st.getPlayer().getLevel() >= 68) {
            st.setCond(0);
            return "jeremy_q0621_0101.htm";
        }
        st.exitCurrentQuest(true);
        return "jeremy_q0621_0103.htm";
    }

    
}
