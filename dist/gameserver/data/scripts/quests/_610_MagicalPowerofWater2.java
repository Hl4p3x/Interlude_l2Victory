package quests;

import ru.j2dev.commons.listener.Listener;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.manager.ServerVariables;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _610_MagicalPowerofWater2 extends Quest {
    private static final int ASEFA = 31372;
    private static final int VARKAS_HOLY_ALTAR = 31560;
    private static final int GREEN_TOTEM = 7238;
    private static final int Reward_First = 4589;
    private static final int Reward_Last = 4594;
    private static final int SoulOfWaterAshutar = 25316;
    int ICE_HEART_OF_ASHUTAR;
    private NpcInstance SoulOfWaterAshutarSpawn;

    public _610_MagicalPowerofWater2() {
        super(true);
        ICE_HEART_OF_ASHUTAR = 7239;
        SoulOfWaterAshutarSpawn = null;
        addStartNpc(31372);
        addTalkId(31560);
        addKillId(25316);
        addQuestItem(ICE_HEART_OF_ASHUTAR);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final NpcInstance isQuest = GameObjectsStorage.getByNpcId(25316);
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            htmltext = "shaman_asefa_q0610_0104.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("610_1".equalsIgnoreCase(event)) {
            if (ServerVariables.getLong(_610_MagicalPowerofWater2.class.getSimpleName(), 0L) + 10800000L > System.currentTimeMillis()) {
                htmltext = "totem_of_barka_q0610_0204.htm";
            } else if (st.getQuestItemsCount(7238) >= 1L && isQuest == null) {
                st.takeItems(7238, 1L);
                (SoulOfWaterAshutarSpawn = st.addSpawn(25316, 104825, -36926, -1136)).addListener((Listener) new DeathListener());
                st.playSound("ItemSound.quest_middle");
            } else {
                htmltext = "totem_of_barka_q0610_0203.htm";
            }
        } else if ("610_3".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(ICE_HEART_OF_ASHUTAR) >= 1L) {
                st.takeItems(ICE_HEART_OF_ASHUTAR, -1L);
                st.addExpAndSp(10000L, 0L);
                st.giveItems(Rnd.get(4589, 4594), 5L, true);
                st.playSound("ItemSound.quest_finish");
                htmltext = "shaman_asefa_q0610_0301.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "shaman_asefa_q0610_0302.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final NpcInstance isQuest = GameObjectsStorage.getByNpcId(25316);
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 31372) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 75) {
                    if (st.getQuestItemsCount(7238) >= 1L) {
                        htmltext = "shaman_asefa_q0610_0101.htm";
                    } else {
                        htmltext = "shaman_asefa_q0610_0102.htm";
                        st.exitCurrentQuest(true);
                    }
                } else {
                    htmltext = "shaman_asefa_q0610_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "shaman_asefa_q0610_0105.htm";
            } else if (cond == 2) {
                htmltext = "shaman_asefa_q0610_0202.htm";
            } else if (cond == 3 && st.getQuestItemsCount(ICE_HEART_OF_ASHUTAR) >= 1L) {
                htmltext = "shaman_asefa_q0610_0201.htm";
            }
        } else if (npcId == 31560) {
            if (!npc.isBusy()) {
                if (ServerVariables.getLong(_610_MagicalPowerofWater2.class.getSimpleName(), 0L) + 10800000L > System.currentTimeMillis()) {
                    htmltext = "totem_of_barka_q0610_0204.htm";
                } else if (cond == 1) {
                    htmltext = "totem_of_barka_q0610_0101.htm";
                } else if (cond == 2 && isQuest == null) {
                    (SoulOfWaterAshutarSpawn = st.addSpawn(25316, 104825, -36926, -1136)).addListener((Listener) new DeathListener());
                    htmltext = "totem_of_barka_q0610_0204.htm";
                }
            } else {
                htmltext = "totem_of_barka_q0610_0202.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(ICE_HEART_OF_ASHUTAR) == 0L && npc.getNpcId() == 25316) {
            st.giveItems(ICE_HEART_OF_ASHUTAR, 1L);
            st.setCond(3);
            if (SoulOfWaterAshutarSpawn != null) {
                SoulOfWaterAshutarSpawn.deleteMe();
            }
            SoulOfWaterAshutarSpawn = null;
        }
        return null;
    }

    private static class DeathListener implements OnDeathListener {
        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            ServerVariables.set(_610_MagicalPowerofWater2.class.getSimpleName(), String.valueOf(System.currentTimeMillis()));
        }
    }
}
