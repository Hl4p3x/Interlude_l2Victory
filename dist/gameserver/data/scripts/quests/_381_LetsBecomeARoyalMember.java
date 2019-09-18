package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _381_LetsBecomeARoyalMember extends Quest {
    private static final int KAILS_COIN = 5899;
    private static final int COIN_ALBUM = 5900;
    private static final int MEMBERSHIP_1 = 3813;
    private static final int CLOVER_COIN = 7569;
    private static final int ROYAL_MEMBERSHIP = 5898;
    private static final int SORINT = 30232;
    private static final int SANDRA = 30090;
    private static final int ANCIENT_GARGOYLE = 21018;
    private static final int VEGUS = 27316;
    private static final int GARGOYLE_CHANCE = 5;
    private static final int VEGUS_CHANCE = 100;

    public _381_LetsBecomeARoyalMember() {
        super(false);
        addStartNpc(SORINT);
        addTalkId(SANDRA);
        addKillId(ANCIENT_GARGOYLE);
        addKillId(VEGUS);
        addQuestItem(KAILS_COIN);
        addQuestItem(COIN_ALBUM);
        addQuestItem(CLOVER_COIN);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("warehouse_keeper_sorint_q0381_02.htm".equalsIgnoreCase(event)) {
            if (st.getPlayer().getLevel() >= 55 && st.getQuestItemsCount(MEMBERSHIP_1) > 0L) {
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "warehouse_keeper_sorint_q0381_03.htm";
            } else {
                htmltext = "warehouse_keeper_sorint_q0381_02.htm";
                st.exitCurrentQuest(true);
            }
        } else if ("sandra_q0381_02.htm".equalsIgnoreCase(event) && st.getCond() == 1) {
            st.set("id", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        final int npcId = npc.getNpcId();
        final long album = st.getQuestItemsCount(COIN_ALBUM);
        if (npcId == SORINT) {
            if (cond == 0) {
                htmltext = "warehouse_keeper_sorint_q0381_01.htm";
            } else if (cond == 1) {
                final long coin = st.getQuestItemsCount(KAILS_COIN);
                if (coin > 0L && album > 0L) {
                    st.takeItems(KAILS_COIN, -1L);
                    st.takeItems(COIN_ALBUM, -1L);
                    st.giveItems(ROYAL_MEMBERSHIP, 1L);
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                    htmltext = "warehouse_keeper_sorint_q0381_06.htm";
                } else if (album == 0L) {
                    htmltext = "warehouse_keeper_sorint_q0381_05.htm";
                } else if (coin == 0L) {
                    htmltext = "warehouse_keeper_sorint_q0381_04.htm";
                }
            }
        } else {
            final long clover = st.getQuestItemsCount(CLOVER_COIN);
            if (album > 0L) {
                htmltext = "sandra_q0381_05.htm";
            } else if (clover > 0L) {
                st.takeItems(CLOVER_COIN, -1L);
                st.giveItems(COIN_ALBUM, 1L);
                st.playSound("ItemSound.quest_itemget");
                htmltext = "sandra_q0381_04.htm";
            } else if (st.getInt("id") == 0) {
                htmltext = "sandra_q0381_01.htm";
            } else {
                htmltext = "sandra_q0381_03.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() != 2) {
            return null;
        }
        final int npcId = npc.getNpcId();
        final long album = st.getQuestItemsCount(COIN_ALBUM);
        final long coin = st.getQuestItemsCount(KAILS_COIN);
        final long clover = st.getQuestItemsCount(CLOVER_COIN);
        if (npcId == ANCIENT_GARGOYLE && coin == 0L) {
            if (Rnd.chance(GARGOYLE_CHANCE)) {
                st.giveItems(KAILS_COIN, 1L);
                if (album > 0L || clover > 0L) {
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == VEGUS && clover + album == 0L && st.getInt("id") != 0 && Rnd.chance(VEGUS_CHANCE)) {
            st.giveItems(CLOVER_COIN, 1L);
            if (coin > 0L) {
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
