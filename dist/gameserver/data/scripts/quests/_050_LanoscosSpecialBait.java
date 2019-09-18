package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _050_LanoscosSpecialBait extends Quest {
    int Lanosco;
    int SingingWind;
    int EssenceofWind;
    int WindFishingLure;
    Integer FishSkill;

    public _050_LanoscosSpecialBait() {
        super(false);
        Lanosco = 31570;
        SingingWind = 21026;
        EssenceofWind = 7621;
        WindFishingLure = 7610;
        FishSkill = 1315;
        addStartNpc(Lanosco);
        addTalkId(Lanosco);
        addKillId(SingingWind);
        addQuestItem(EssenceofWind);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("fisher_lanosco_q0050_0104.htm".equals(event)) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("fisher_lanosco_q0050_0201.htm".equals(event)) {
            if (st.getQuestItemsCount(EssenceofWind) < 100L) {
                htmltext = "fisher_lanosco_q0050_0202.htm";
            } else {
                st.unset("cond");
                st.takeItems(EssenceofWind, -1L);
                st.giveItems(WindFishingLure, 4L);
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
        if (npcId == Lanosco) {
            if (id == 1) {
                if (st.getPlayer().getLevel() < 27) {
                    htmltext = "fisher_lanosco_q0050_0103.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getPlayer().getSkillLevel(FishSkill) >= 8) {
                    htmltext = "fisher_lanosco_q0050_0101.htm";
                } else {
                    htmltext = "fisher_lanosco_q0050_0102.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1 || cond == 2) {
                if (st.getQuestItemsCount(EssenceofWind) < 100L) {
                    htmltext = "fisher_lanosco_q0050_0106.htm";
                    st.setCond(1);
                } else {
                    htmltext = "fisher_lanosco_q0050_0105.htm";
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (npcId == SingingWind && st.getCond() == 1 && st.getQuestItemsCount(EssenceofWind) < 100L && Rnd.chance(30)) {
            st.giveItems(EssenceofWind, 1L);
            if (st.getQuestItemsCount(EssenceofWind) == 100L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
