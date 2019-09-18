package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _660_AidingtheFloranVillage extends Quest {
    public final int MARIA = 30608;
    public final int ALEX = 30291;
    public final int CARSED_SEER = 21106;
    public final int PLAIN_WATCMAN = 21102;
    public final int ROUGH_HEWN_ROCK_GOLEM = 21103;
    public final int DELU_LIZARDMAN_SHAMAN = 20781;
    public final int DELU_LIZARDMAN_SAPPLIER = 21104;
    public final int DELU_LIZARDMAN_COMMANDER = 21107;
    public final int DELU_LIZARDMAN_SPESIAL_AGENT = 21105;
    public final int WATCHING_EYES = 8074;
    public final int ROUGHLY_HEWN_ROCK_GOLEM_SHARD = 8075;
    public final int DELU_LIZARDMAN_SCALE = 8076;
    public final int SCROLL_ENCANT_ARMOR = 956;
    public final int SCROLL_ENCHANT_WEAPON = 955;

    public _660_AidingtheFloranVillage() {
        super(false);
        addStartNpc(30608);
        addTalkId(30608);
        addTalkId(30291);
        addKillId(21106);
        addKillId(21102);
        addKillId(21103);
        addKillId(20781);
        addKillId(21104);
        addKillId(21107);
        addKillId(21105);
        addQuestItem(8074);
        addQuestItem(8076);
        addQuestItem(8075);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final long EYES = st.getQuestItemsCount(8074);
        final long SCALE = st.getQuestItemsCount(8076);
        final long SHARD = st.getQuestItemsCount(8075);
        if ("30608-04.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("30291-05.htm".equalsIgnoreCase(event)) {
            if (EYES + SCALE + SHARD >= 45L) {
                st.giveItems(57, EYES * 100L + SCALE * 100L + SHARD * 100L + 9000L);
                st.takeItems(8074, -1L);
                st.takeItems(8076, -1L);
                st.takeItems(8075, -1L);
            } else {
                st.giveItems(57, EYES * 100L + SCALE * 100L + SHARD * 100L);
                st.takeItems(8074, -1L);
                st.takeItems(8076, -1L);
                st.takeItems(8075, -1L);
            }
            st.playSound("ItemSound.quest_itemget");
        } else if ("30291-11.htm".equalsIgnoreCase(event)) {
            if (EYES + SCALE + SHARD >= 99L) {
                final long n = 100L - EYES;
                final long t = 100L - SCALE - EYES;
                if (EYES >= 100L) {
                    st.takeItems(8074, 100L);
                } else {
                    st.takeItems(8074, -1L);
                    if (SCALE >= n) {
                        st.takeItems(8076, n);
                    } else {
                        st.takeItems(8076, -1L);
                        st.takeItems(8075, t);
                    }
                }
                if (Rnd.chance(80)) {
                    st.giveItems(57, 13000L);
                    st.giveItems(956, 1L);
                } else {
                    st.giveItems(57, 1000L);
                }
                st.playSound("ItemSound.quest_itemget");
            } else {
                htmltext = "30291-14.htm";
            }
        } else if ("30291-12.htm".equalsIgnoreCase(event)) {
            if (EYES + SCALE + SHARD >= 199L) {
                final long n = 200L - EYES;
                final long t = 200L - SCALE - EYES;
                final int luck = Rnd.get(15);
                if (EYES >= 200L) {
                    st.takeItems(8074, 200L);
                } else {
                    st.takeItems(8074, -1L);
                }
                if (SCALE >= n) {
                    st.takeItems(8076, n);
                } else {
                    st.takeItems(8076, -1L);
                }
                st.takeItems(8075, t);
                if (luck < 9) {
                    st.giveItems(57, 20000L);
                    st.giveItems(956, 1L);
                } else if (luck > 8 && luck < 12) {
                    st.giveItems(955, 1L);
                } else {
                    st.giveItems(57, 2000L);
                }
                st.playSound("ItemSound.quest_itemget");
            } else {
                htmltext = "30291-14.htm";
            }
        } else if ("30291-13.htm".equalsIgnoreCase(event)) {
            if (EYES + SCALE + SHARD >= 499L) {
                final long n = 500L - EYES;
                final long t = 500L - SCALE - EYES;
                if (EYES >= 500L) {
                    st.takeItems(8074, 500L);
                } else {
                    st.takeItems(8074, -1L);
                }
                if (SCALE >= n) {
                    st.takeItems(8076, n);
                } else {
                    st.takeItems(8076, -1L);
                    st.takeItems(8075, t);
                }
                if (Rnd.chance(80)) {
                    st.giveItems(57, 45000L);
                    st.giveItems(955, 1L);
                } else {
                    st.giveItems(57, 5000L);
                }
                st.playSound("ItemSound.quest_itemget");
            } else {
                htmltext = "30291-14.htm";
            }
        } else if ("30291-06.htm".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
            st.playSound("ItemSound.quest_finish");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 30608 && cond < 1) {
            if (st.getPlayer().getLevel() < 30) {
                htmltext = "30608-01.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "30608-02.htm";
            }
        } else if (npcId == 30608 && cond == 1) {
            htmltext = "30608-06.htm";
        } else if (npcId == 30291 && cond == 1) {
            htmltext = "30291-01.htm";
            st.playSound("ItemSound.quest_middle");
            st.setCond(2);
        } else if (npcId == 30291 && cond == 2) {
            if (st.getQuestItemsCount(8074) + st.getQuestItemsCount(8076) + st.getQuestItemsCount(8075) == 0L) {
                htmltext = "30291-02.htm";
            } else {
                htmltext = "30291-03.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int chance = Rnd.get(100) + 1;
        if (st.getCond() == 2) {
            if ((npcId == 21106 | npcId == 21102) && chance < 79) {
                st.giveItems(8074, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (npcId == 21103 && chance < 75) {
                st.giveItems(8075, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if ((npcId == 20781 | npcId == 21104 | npcId == 21107 | npcId == 21105) && chance < 67) {
                st.giveItems(8076, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
