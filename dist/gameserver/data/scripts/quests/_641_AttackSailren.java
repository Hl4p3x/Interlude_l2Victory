package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _641_AttackSailren extends Quest {
    private static final int STATUE = 32109;
    private static final int VEL1 = 22196;
    private static final int VEL2 = 22197;
    private static final int VEL3 = 22198;
    private static final int VEL4 = 22218;
    private static final int VEL5 = 22223;
    private static final int PTE = 22199;
    private static final int FRAGMENTS = 8782;
    private static final int GAZKH = 8784;

    public _641_AttackSailren() {
        super(true);
        addStartNpc(STATUE);
        addKillId(VEL1);
        addKillId(VEL2);
        addKillId(VEL3);
        addKillId(VEL4);
        addKillId(VEL5);
        addKillId(PTE);
        addQuestItem(FRAGMENTS);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("statue_of_shilen_q0641_05.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("statue_of_shilen_q0641_08.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.takeItems(FRAGMENTS, -1L);
            st.giveItems(GAZKH, 1L);
            st.exitCurrentQuest(true);
            st.unset("cond");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (cond) {
            case 0:
                final QuestState qs = st.getPlayer().getQuestState(_126_IntheNameofEvilPart2.class);
                if (qs == null || !qs.isCompleted()) {
                    htmltext = "statue_of_shilen_q0641_02.htm";
                } else if (st.getPlayer().getLevel() >= 77) {
                    htmltext = "statue_of_shilen_q0641_01.htm";
                } else {
                    st.exitCurrentQuest(true);
                }
                break;
            case 1:
                htmltext = "statue_of_shilen_q0641_05.htm";
                break;
            case 2:
                htmltext = "statue_of_shilen_q0641_07.htm";
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(FRAGMENTS) < 30L) {
            st.giveItems(FRAGMENTS, 1L);
            if (st.getQuestItemsCount(FRAGMENTS) == 30L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
                st.setState(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
