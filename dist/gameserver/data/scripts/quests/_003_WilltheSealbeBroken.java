package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _003_WilltheSealbeBroken extends Quest {
    private static final int redry = 30141;
    private static final int onyx_beast = 20031;
    private static final int tainted_zombie = 20041;
    private static final int stink_zombie = 20046;
    private static final int least_succubus = 20048;
    private static final int least_succubus_turen = 20052;
    private static final int least_succubus_tilfo = 20057;
    private static final int scrl_of_ench_am_d = 956;
    private static final int onyx_beast_eye = 1081;
    private static final int taint_stone = 1082;
    private static final int succubus_blood = 1083;

    public _003_WilltheSealbeBroken() {
        super(false);
        addStartNpc(redry);
        addKillId(onyx_beast, tainted_zombie, stink_zombie, least_succubus, least_succubus_turen, least_succubus_tilfo);
        addQuestItem(onyx_beast_eye, taint_stone, succubus_blood);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int npcId = npc.getNpcId();
        if (npcId == 30141 && "quest_accept".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.set("release_darkelf_elder1", String.valueOf(1), true);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "redry_q0003_03.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("release_darkelf_elder1");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != redry) {
                    break;
                }
                if (st.getPlayer().getLevel() < 16) {
                    htmltext = "redry_q0003_01.htm";
                    break;
                }
                if (st.getPlayer().getRace() != Race.darkelf) {
                    st.exitCurrentQuest(true);
                    htmltext = "redry_q0003_00.htm";
                    break;
                }
                htmltext = "redry_q0003_02.htm";
                break;
            }
            case 2: {
                if (npcId != redry) {
                    break;
                }
                if (GetMemoState == 1 && st.getQuestItemsCount(onyx_beast_eye) >= 1L && st.getQuestItemsCount(taint_stone) >= 1L && st.getQuestItemsCount(succubus_blood) >= 1L) {
                    st.giveItems(scrl_of_ench_am_d, 1L);
                    st.takeItems(onyx_beast_eye, -1L);
                    st.takeItems(taint_stone, -1L);
                    st.takeItems(succubus_blood, -1L);
                    st.unset("release_darkelf_elder1");
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                    htmltext = "redry_q0003_06.htm";
                    break;
                }
                htmltext = "redry_q0003_04.htm";
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("release_darkelf_elder1");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 1) {
            switch (npcId) {
                case 20031:
                    st.giveItems(onyx_beast_eye, 1L);
                    st.playSound("ItemSound.quest_middle");
                    if (st.getQuestItemsCount(taint_stone) >= 1L && st.getQuestItemsCount(succubus_blood) >= 1L) {
                        st.setCond(2);
                    }
                    break;
                case 20041:
                case 20046:
                    st.giveItems(taint_stone, 1L);
                    st.playSound("ItemSound.quest_middle");
                    if (st.getQuestItemsCount(onyx_beast_eye) >= 1L && st.getQuestItemsCount(succubus_blood) >= 1L) {
                        st.setCond(2);
                    }
                    break;
                case 20048:
                case 20052:
                case 20057:
                    st.giveItems(succubus_blood, 1L);
                    st.playSound("ItemSound.quest_middle");
                    if (st.getQuestItemsCount(onyx_beast_eye) >= 1L && st.getQuestItemsCount(taint_stone) >= 1L) {
                        st.setCond(2);
                    }
                    break;
            }
        }
        return null;
    }
}
