package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.HashMap;
import java.util.Map;

public class _633_InTheForgottenVillage extends Quest {
    private static final int MINA = 31388;
    private static final int RIB_BONE = 7544;
    private static final int Z_LIVER = 7545;
    private static final Map<Integer, Double> DAMOBS = new HashMap<>();
    private static final Map<Integer, Double> UNDEADS = new HashMap<>();

    public _633_InTheForgottenVillage() {
        super(true);
        DAMOBS.put(21557, 32.8);
        DAMOBS.put(21558, 32.8);
        DAMOBS.put(21559, 33.7);
        DAMOBS.put(21560, 33.7);
        DAMOBS.put(21563, 34.2);
        DAMOBS.put(21564, 34.8);
        DAMOBS.put(21565, 35.1);
        DAMOBS.put(21566, 35.9);
        DAMOBS.put(21567, 35.9);
        DAMOBS.put(21572, 36.5);
        DAMOBS.put(21574, 38.3);
        DAMOBS.put(21575, 38.3);
        DAMOBS.put(21580, 38.5);
        DAMOBS.put(21581, 39.5);
        DAMOBS.put(21583, 39.7);
        DAMOBS.put(21584, 40.1);
        UNDEADS.put(21553, 34.7);
        UNDEADS.put(21554, 34.7);
        UNDEADS.put(21561, 45.0);
        UNDEADS.put(21578, 50.1);
        UNDEADS.put(21596, 35.9);
        UNDEADS.put(21597, 37.0);
        UNDEADS.put(21598, 44.1);
        UNDEADS.put(21599, 39.5);
        UNDEADS.put(21600, 40.8);
        UNDEADS.put(21601, 41.1);
        addStartNpc(MINA);
        addQuestItem(RIB_BONE);
        UNDEADS.keySet().forEach(this::addKillId);
        DAMOBS.keySet().forEach(this::addKillId);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "day_mina_q0633_0104.htm";
        }
        if ("633_4".equalsIgnoreCase(event)) {
            st.takeItems(RIB_BONE, -1L);
            st.playSound("ItemSound.quest_finish");
            htmltext = "day_mina_q0633_0204.htm";
            st.exitCurrentQuest(true);
        } else if ("633_1".equalsIgnoreCase(event)) {
            htmltext = "day_mina_q0633_0201.htm";
        } else if ("633_3".equalsIgnoreCase(event) && st.getCond() == 2) {
            if (st.getQuestItemsCount(RIB_BONE) >= 200L) {
                st.takeItems(RIB_BONE, -1L);
                st.giveItems(57, 25000L);
                st.addExpAndSp(305235L, 0L);
                st.playSound("ItemSound.quest_finish");
                st.setCond(1);
                htmltext = "day_mina_q0633_0202.htm";
            } else {
                htmltext = "day_mina_q0633_0203.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        final int id = st.getState();
        if (npcId == MINA) {
            if (id == 1) {
                if (st.getPlayer().getLevel() >= 65) {
                    htmltext = "day_mina_q0633_0101.htm";
                } else {
                    htmltext = "day_mina_q0633_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "day_mina_q0633_0106.htm";
            } else if (cond == 2) {
                htmltext = "day_mina_q0633_0105.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (UNDEADS.containsKey(npcId)) {
            st.rollAndGive(Z_LIVER, 1, UNDEADS.get(npcId));
        } else if (DAMOBS.containsKey(npcId)) {
            final long count = st.getQuestItemsCount(RIB_BONE);
            if (count < 200L && Rnd.chance(DAMOBS.get(npcId))) {
                st.giveItems(RIB_BONE, 1L);
                if (count >= 199L) {
                    st.setCond(2);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
