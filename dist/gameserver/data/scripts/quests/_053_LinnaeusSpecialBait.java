package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _053_LinnaeusSpecialBait extends Quest {
    int Linnaeu;
    int CrimsonDrake;
    int HeartOfCrimsonDrake;
    int FlameFishingLure;
    Integer FishSkill;

    public _053_LinnaeusSpecialBait() {
        super(false);
        Linnaeu = 31577;
        CrimsonDrake = 20670;
        HeartOfCrimsonDrake = 7624;
        FlameFishingLure = 7613;
        FishSkill = 1315;
        addStartNpc(Linnaeu);
        addTalkId(Linnaeu);
        addKillId(CrimsonDrake);
        addQuestItem(HeartOfCrimsonDrake);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("fisher_linneaus_q0053_0104.htm".equals(event)) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("fisher_linneaus_q0053_0201.htm".equals(event)) {
            if (st.getQuestItemsCount(HeartOfCrimsonDrake) < 100L) {
                htmltext = "fisher_linneaus_q0053_0202.htm";
            } else {
                st.unset("cond");
                st.takeItems(HeartOfCrimsonDrake, -1L);
                st.giveItems(FlameFishingLure, 4L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(false);
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        final int id = st.getState();
        if (npcId == Linnaeu) {
            if (id == 1) {
                if (st.getPlayer().getLevel() < 60) {
                    htmltext = "fisher_linneaus_q0053_0103.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getPlayer().getSkillLevel(FishSkill) >= 21) {
                    htmltext = "fisher_linneaus_q0053_0101.htm";
                } else {
                    htmltext = "fisher_linneaus_q0053_0102.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1 || cond == 2) {
                if (st.getQuestItemsCount(HeartOfCrimsonDrake) < 100L) {
                    htmltext = "fisher_linneaus_q0053_0106.htm";
                    st.setCond(1);
                } else {
                    htmltext = "fisher_linneaus_q0053_0105.htm";
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (npcId == CrimsonDrake && st.getCond() == 1 && st.getQuestItemsCount(HeartOfCrimsonDrake) < 100L && Rnd.chance(30)) {
            st.giveItems(HeartOfCrimsonDrake, 1L);
            if (st.getQuestItemsCount(HeartOfCrimsonDrake) == 100L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
