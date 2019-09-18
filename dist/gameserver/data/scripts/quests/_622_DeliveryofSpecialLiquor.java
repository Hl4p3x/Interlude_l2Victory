package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _622_DeliveryofSpecialLiquor extends Quest {
    private static final int JEREMY = 31521;
    private static final int LIETTA = 31267;
    private static final int PULIN = 31543;
    private static final int NAFF = 31544;
    private static final int CROCUS = 31545;
    private static final int KUBER = 31546;
    private static final int BEOLIN = 31547;
    private static final int SpecialDrink = 7207;
    private static final int FeeOfSpecialDrink = 7198;
    private static final int RecipeSealedTateossianRing = 6849;
    private static final int RecipeSealedTateossianEarring = 6847;
    private static final int RecipeSealedTateossianNecklace = 6851;
    private static final int HastePotion = 734;
    private static final int Tateossian_CHANCE = 20;

    public _622_DeliveryofSpecialLiquor() {
        super(false);
        addStartNpc(JEREMY);
        addTalkId(LIETTA);
        addTalkId(PULIN);
        addTalkId(NAFF);
        addTalkId(CROCUS);
        addTalkId(KUBER);
        addTalkId(BEOLIN);
        addQuestItem(SpecialDrink);
        addQuestItem(FeeOfSpecialDrink);
    }

    private static void takeDrink(final QuestState st, final int setcond) {
        st.setCond(setcond);
        st.takeItems(SpecialDrink, 1L);
        st.giveItems(FeeOfSpecialDrink, 1L);
        st.playSound("ItemSound.quest_middle");
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        final int cond = st.getCond();
        final long SpecialDrink_count = st.getQuestItemsCount(SpecialDrink);
        if ("jeremy_q0622_0104.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setState(2);
            st.setCond(1);
            st.takeItems(SpecialDrink, -1L);
            st.takeItems(FeeOfSpecialDrink, -1L);
            st.giveItems(SpecialDrink, 5L);
            st.playSound("ItemSound.quest_accept");
        } else if ("beolin_q0622_0201.htm".equalsIgnoreCase(event) && cond == 1 && SpecialDrink_count > 0L) {
            takeDrink(st, 2);
        } else if ("kuber_q0622_0301.htm".equalsIgnoreCase(event) && cond == 2 && SpecialDrink_count > 0L) {
            takeDrink(st, 3);
        } else if ("crocus_q0622_0401.htm".equalsIgnoreCase(event) && cond == 3 && SpecialDrink_count > 0L) {
            takeDrink(st, 4);
        } else if ("naff_q0622_0501.htm".equalsIgnoreCase(event) && cond == 4 && SpecialDrink_count > 0L) {
            takeDrink(st, 5);
        } else if ("pulin_q0622_0601.htm".equalsIgnoreCase(event) && cond == 5 && SpecialDrink_count > 0L) {
            takeDrink(st, 6);
        } else if ("jeremy_q0622_0701.htm".equalsIgnoreCase(event) && cond == 6 && st.getQuestItemsCount(FeeOfSpecialDrink) >= 5L) {
            st.setCond(7);
        } else if ("warehouse_keeper_lietta_q0622_0801.htm".equalsIgnoreCase(event) && cond == 7 && st.getQuestItemsCount(FeeOfSpecialDrink) >= 5L) {
            st.takeItems(SpecialDrink, -1L);
            st.takeItems(FeeOfSpecialDrink, -1L);
            if (Rnd.chance(Tateossian_CHANCE)) {
                if (Rnd.chance(40)) {
                    st.giveItems(RecipeSealedTateossianRing, 1L);
                } else if (Rnd.chance(40)) {
                    st.giveItems(RecipeSealedTateossianEarring, 1L);
                } else {
                    st.giveItems(RecipeSealedTateossianNecklace, 1L);
                }
            } else {
                st.giveItems(57, 18800L);
                st.giveItems(HastePotion, 1L, true);
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
            final long SpecialDrink_count = st.getQuestItemsCount(SpecialDrink);
            final long FeeOfSpecialDrink_count = st.getQuestItemsCount(FeeOfSpecialDrink);
            if (cond == 1 && npcId == BEOLIN && SpecialDrink_count > 0L) {
                htmltext = "beolin_q0622_0101.htm";
            } else if (cond == 2 && npcId == KUBER && SpecialDrink_count > 0L) {
                htmltext = "kuber_q0622_0201.htm";
            } else if (cond == 3 && npcId == CROCUS && SpecialDrink_count > 0L) {
                htmltext = "crocus_q0622_0301.htm";
            } else if (cond == 4 && npcId == NAFF && SpecialDrink_count > 0L) {
                htmltext = "naff_q0622_0401.htm";
            } else if (cond == 5 && npcId == PULIN && SpecialDrink_count > 0L) {
                htmltext = "pulin_q0622_0501.htm";
            } else if (cond == 6 && npcId == JEREMY && FeeOfSpecialDrink_count >= 5L) {
                htmltext = "jeremy_q0622_0601.htm";
            } else if (cond == 7 && npcId == JEREMY && FeeOfSpecialDrink_count >= 5L) {
                htmltext = "jeremy_q0622_0703.htm";
            } else if (cond == 7 && npcId == LIETTA && FeeOfSpecialDrink_count >= 5L) {
                htmltext = "warehouse_keeper_lietta_q0622_0701.htm";
            } else if (cond > 0 && npcId == JEREMY && SpecialDrink_count > 0L) {
                htmltext = "jeremy_q0622_0104.htm";
            }
            return htmltext;
        }
        if (npcId != JEREMY) {
            return htmltext;
        }
        if (st.getPlayer().getLevel() >= 68) {
            st.setCond(0);
            return "jeremy_q0622_0101.htm";
        }
        st.exitCurrentQuest(true);
        return "jeremy_q0622_0103.htm";
    }

    
}
