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

public class _616_MagicalPowerofFire2 extends Quest {
    private static final int KETRAS_HOLY_ALTAR = 31558;
    private static final int UDAN = 31379;
    private static final int FIRE_HEART_OF_NASTRON = 7244;
    private static final int RED_TOTEM = 7243;
    private static final int SoulOfFireNastron = 25306;
    private static final int Reward_First = 4589;
    private static final int Reward_Last = 4594;

    private NpcInstance SoulOfFireNastronSpawn;

    public _616_MagicalPowerofFire2() {
        super(true);
        SoulOfFireNastronSpawn = null;
        addStartNpc(31379);
        addTalkId(31558);
        addKillId(25306);
        addQuestItem(7244);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final NpcInstance isQuest = GameObjectsStorage.getByNpcId(25306);
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            htmltext = "shaman_udan_q0616_0104.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("616_1".equalsIgnoreCase(event)) {
            if (ServerVariables.getLong(_616_MagicalPowerofFire2.class.getSimpleName(), 0L) + 10800000L > System.currentTimeMillis()) {
                htmltext = "totem_of_ketra_q0616_0204.htm";
            } else if (st.getQuestItemsCount(7243) >= 1L && isQuest == null) {
                st.takeItems(7243, 1L);
                (SoulOfFireNastronSpawn = st.addSpawn(25306, 142528, -82528, -6496)).addListener((Listener) new DeathListener());
                st.playSound("ItemSound.quest_middle");
            } else {
                htmltext = "totem_of_ketra_q0616_0203.htm";
            }
        } else if ("616_3".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(7244) >= 1L) {
                st.takeItems(7244, -1L);
                st.addExpAndSp(10000L, 0L);
                st.giveItems(Rnd.get(_616_MagicalPowerofFire2.Reward_First, _616_MagicalPowerofFire2.Reward_Last), 5L, true);
                st.playSound("ItemSound.quest_finish");
                htmltext = "shaman_udan_q0616_0301.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "shaman_udan_q0616_0302.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final NpcInstance isQuest = GameObjectsStorage.getByNpcId(25306);
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        switch (npcId) {
            case 31379: {
                if (cond == 0) {
                    if (st.getPlayer().getLevel() < 75) {
                        htmltext = "shaman_udan_q0616_0103.htm";
                        st.exitCurrentQuest(true);
                        break;
                    }
                    if (st.getQuestItemsCount(7243) >= 1L) {
                        htmltext = "shaman_udan_q0616_0101.htm";
                        break;
                    }
                    htmltext = "shaman_udan_q0616_0102.htm";
                    st.exitCurrentQuest(true);
                    break;
                } else {
                    if (cond == 1) {
                        htmltext = "shaman_udan_q0616_0105.htm";
                        break;
                    }
                    if (cond == 2) {
                        htmltext = "shaman_udan_q0616_0202.htm";
                        break;
                    }
                    if (cond == 3 && st.getQuestItemsCount(7244) >= 1L) {
                        htmltext = "shaman_udan_q0616_0201.htm";
                        break;
                    }
                    break;
                }
            }
            case 31558: {
                if (ServerVariables.getLong(_616_MagicalPowerofFire2.class.getSimpleName(), 0L) + 10800000L > System.currentTimeMillis()) {
                    htmltext = "totem_of_ketra_q0616_0204.htm";
                    break;
                }
                if (npc.isBusy()) {
                    htmltext = "totem_of_ketra_q0616_0202.htm";
                    break;
                }
                if (cond == 1) {
                    htmltext = "totem_of_ketra_q0616_0101.htm";
                    break;
                }
                if (cond != 2) {
                    break;
                }
                if (isQuest == null) {
                    (SoulOfFireNastronSpawn = st.addSpawn(25306, 142528, -82528, -6496)).addListener((Listener) new DeathListener());
                    htmltext = "totem_of_ketra_q0616_0204.htm";
                    break;
                }
                htmltext = "<html><body>Already in spawn.</body></html>";
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(7244) == 0L) {
            st.giveItems(7244, 1L);
            st.setCond(3);
            if (SoulOfFireNastronSpawn != null) {
                SoulOfFireNastronSpawn.deleteMe();
            }
            SoulOfFireNastronSpawn = null;
        }
        return null;
    }

    private static class DeathListener implements OnDeathListener {
        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            ServerVariables.set(_616_MagicalPowerofFire2.class.getSimpleName(), String.valueOf(System.currentTimeMillis()));
        }
    }
}
