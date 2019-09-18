package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _051_OFullesSpecialBait extends Quest {
    int OFulle;
    int FetteredSoul;
    int LostBaitIngredient;
    int IcyAirFishingLure;
    Integer FishSkill;

    public _051_OFullesSpecialBait() {
        super(false);
        OFulle = 31572;
        FetteredSoul = 20552;
        LostBaitIngredient = 7622;
        IcyAirFishingLure = 7611;
        FishSkill = 1315;
        addStartNpc(OFulle);
        addTalkId(OFulle);
        addKillId(FetteredSoul);
        addQuestItem(LostBaitIngredient);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("fisher_ofulle_q0051_0104.htm".equals(event)) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("fisher_ofulle_q0051_0201.htm".equals(event)) {
            if (st.getQuestItemsCount(LostBaitIngredient) < 100L) {
                htmltext = "fisher_ofulle_q0051_0202.htm";
            } else {
                st.unset("cond");
                st.takeItems(LostBaitIngredient, -1L);
                st.giveItems(IcyAirFishingLure, 4L);
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
        if (npcId == OFulle) {
            if (id == 1) {
                if (st.getPlayer().getLevel() < 36) {
                    htmltext = "fisher_ofulle_q0051_0103.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getPlayer().getSkillLevel(FishSkill) >= 11) {
                    htmltext = "fisher_ofulle_q0051_0101.htm";
                } else {
                    htmltext = "fisher_ofulle_q0051_0102.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1 || cond == 2) {
                if (st.getQuestItemsCount(LostBaitIngredient) < 100L) {
                    htmltext = "fisher_ofulle_q0051_0106.htm";
                    st.setCond(1);
                } else {
                    htmltext = "fisher_ofulle_q0051_0105.htm";
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (npcId == FetteredSoul && st.getCond() == 1 && st.getQuestItemsCount(LostBaitIngredient) < 100L && Rnd.chance(30)) {
            st.giveItems(LostBaitIngredient, 1L);
            if (st.getQuestItemsCount(LostBaitIngredient) == 100L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
