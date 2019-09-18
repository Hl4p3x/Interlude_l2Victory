package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _328_SenseForBusiness extends Quest {
    private final int SARIEN = 30436;
    private final int MONSTER_EYE_CARCASS = 1347;
    private final int MONSTER_EYE_LENS = 1366;
    private final int BASILISK_GIZZARD = 1348;

    public _328_SenseForBusiness() {
        super(false);
        addStartNpc(SARIEN);
        addKillId(20055);
        addKillId(20059);
        addKillId(20067);
        addKillId(20068);
        addKillId(20070);
        addKillId(20072);
        addQuestItem(MONSTER_EYE_CARCASS);
        addQuestItem(MONSTER_EYE_LENS);
        addQuestItem(BASILISK_GIZZARD);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("trader_salient_q0328_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("trader_salient_q0328_06.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int id = st.getState();
        if (id == 1) {
            st.setCond(0);
        }
        String htmltext;
        if (st.getCond() == 0) {
            if (st.getPlayer().getLevel() >= 21) {
                htmltext = "trader_salient_q0328_02.htm";
                return htmltext;
            }
            htmltext = "trader_salient_q0328_01.htm";
            st.exitCurrentQuest(true);
        } else {
            final long carcass = st.getQuestItemsCount(MONSTER_EYE_CARCASS);
            final long lenses = st.getQuestItemsCount(MONSTER_EYE_LENS);
            final long gizzard = st.getQuestItemsCount(BASILISK_GIZZARD);
            if (carcass + lenses + gizzard > 0L) {
                st.giveItems(57, 30L * carcass + 2000L * lenses + 75L * gizzard);
                st.takeItems(MONSTER_EYE_CARCASS, -1L);
                st.takeItems(MONSTER_EYE_LENS, -1L);
                st.takeItems(BASILISK_GIZZARD, -1L);
                htmltext = "trader_salient_q0328_05.htm";
            } else {
                htmltext = "trader_salient_q0328_04.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int n = Rnd.get(1, 100);
        if (npcId == 20055) {
            if (n < 47) {
                st.giveItems(MONSTER_EYE_CARCASS, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (n < 49) {
                st.giveItems(MONSTER_EYE_LENS, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (npcId == 20059) {
            if (n < 51) {
                st.giveItems(MONSTER_EYE_CARCASS, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (n < 53) {
                st.giveItems(MONSTER_EYE_LENS, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (npcId == 20067) {
            if (n < 67) {
                st.giveItems(MONSTER_EYE_CARCASS, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (n < 69) {
                st.giveItems(MONSTER_EYE_LENS, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (npcId == 20068) {
            if (n < 75) {
                st.giveItems(MONSTER_EYE_CARCASS, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (n < 77) {
                st.giveItems(MONSTER_EYE_LENS, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (npcId == 20070) {
            if (n < 50) {
                st.giveItems(BASILISK_GIZZARD, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (npcId == 20072 && n < 51) {
            st.giveItems(BASILISK_GIZZARD, 1L);
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
