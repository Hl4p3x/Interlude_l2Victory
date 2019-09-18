package quests;

import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SkillList;

public class _247_PossessorOfaPreciousSoul4 extends Quest {
    private static final int CARADINE = 31740;
    private static final int LADY_OF_LAKE = 31745;
    private static final int CARADINE_LETTER_LAST = 7679;
    private static final int NOBLESS_TIARA = 7694;

    public _247_PossessorOfaPreciousSoul4() {
        super(false);
        addStartNpc(CARADINE);
        addTalkId(LADY_OF_LAKE);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int cond = st.getCond();
        if (cond == 0 && "caradine_q0247_03.htm".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if (cond == 1) {
            if ("caradine_q0247_04.htm".equals(event)) {
                return htmltext;
            }
            if ("caradine_q0247_05.htm".equals(event)) {
                st.setCond(2);
                st.takeItems(CARADINE_LETTER_LAST, 1L);
                st.getPlayer().teleToLocation(143230, 44030, -3030);
                return htmltext;
            }
        } else if (cond == 2) {
            if ("caradine_q0247_06.htm".equals(event)) {
                return htmltext;
            }
            if ("caradine_q0247_05.htm".equals(event)) {
                st.getPlayer().teleToLocation(143230, 44030, -3030);
                return htmltext;
            }
            if ("lady_of_the_lake_q0247_02.htm".equals(event)) {
                return htmltext;
            }
            if ("lady_of_the_lake_q0247_03.htm".equals(event)) {
                return htmltext;
            }
            if ("lady_of_the_lake_q0247_04.htm".equals(event)) {
                return htmltext;
            }
            if ("lady_of_the_lake_q0247_05.htm".equals(event)) {
                if (st.getPlayer().getLevel() >= 75) {
                    st.giveItems(NOBLESS_TIARA, 1L);
                    st.playSound("ItemSound.quest_finish");
                    st.unset("cond");
                    st.exitCurrentQuest(false);
                    NoblessManager.getInstance().addNoble(st.getPlayer());
                    st.getPlayer().setNoble(true);
                    st.getPlayer().updatePledgeClass();
                    st.getPlayer().updateNobleSkills();
                    st.getPlayer().sendPacket(new SkillList(st.getPlayer()));
                    st.getPlayer().broadcastUserInfo(true);
                } else {
                    htmltext = "lady_of_the_lake_q0247_06.htm";
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (!st.getPlayer().isSubClassActive()) {
            return "Subclass only!";
        }
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        final int cond = st.getCond();
        if (npcId == CARADINE) {
            final QuestState previous = st.getPlayer().getQuestState(_246_PossessorOfaPreciousSoul3.class);
            if (id == 1 && previous != null && previous.getState() == 3) {
                if (st.getPlayer().getLevel() < 75) {
                    htmltext = "caradine_q0247_02.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "caradine_q0247_01.htm";
                }
            } else if (cond == 1) {
                htmltext = "caradine_q0247_03.htm";
            } else if (cond == 2) {
                htmltext = "caradine_q0247_06.htm";
            }
        } else if (npcId == LADY_OF_LAKE && cond == 2) {
            if (st.getPlayer().getLevel() >= 75) {
                htmltext = "lady_of_the_lake_q0247_01.htm";
            } else {
                htmltext = "lady_of_the_lake_q0247_06.htm";
            }
        }
        return htmltext;
    }
}
