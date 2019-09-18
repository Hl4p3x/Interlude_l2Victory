package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _052_WilliesSpecialBait extends Quest {
    private static final int Willie = 31574;
    private static final int[] TarlkBasilisks = {20573, 20574};
    private static final int EyeOfTarlkBasilisk = 7623;
    private static final int EarthFishingLure = 7612;
    private static final Integer FishSkill = 1315;

    public _052_WilliesSpecialBait() {
        super(false);
        addStartNpc(31574);
        addKillId(_052_WilliesSpecialBait.TarlkBasilisks);
        addQuestItem(7623);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("fisher_willeri_q0052_0104.htm".equals(event)) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("fisher_willeri_q0052_0201.htm".equals(event)) {
            if (st.getQuestItemsCount(7623) < 100L) {
                htmltext = "fisher_willeri_q0052_0202.htm";
            } else {
                st.unset("cond");
                st.takeItems(7623, -1L);
                st.giveItems(7612, 4L);
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
        if (npcId == 31574) {
            if (id == 1) {
                if (st.getPlayer().getLevel() < 48) {
                    htmltext = "fisher_willeri_q0052_0103.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getPlayer().getSkillLevel(_052_WilliesSpecialBait.FishSkill) >= 16) {
                    htmltext = "fisher_willeri_q0052_0101.htm";
                } else {
                    htmltext = "fisher_willeri_q0052_0102.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1 || cond == 2) {
                if (st.getQuestItemsCount(7623) < 100L) {
                    htmltext = "fisher_willeri_q0052_0106.htm";
                    st.setCond(1);
                } else {
                    htmltext = "fisher_willeri_q0052_0105.htm";
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if ((npcId == _052_WilliesSpecialBait.TarlkBasilisks[0] || (npcId == _052_WilliesSpecialBait.TarlkBasilisks[1] && st.getCond() == 1)) && st.getQuestItemsCount(7623) < 100L && Rnd.chance(30)) {
            st.giveItems(7623, 1L);
            if (st.getQuestItemsCount(7623) == 100L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
