package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _704_Missqueen extends Quest {
    public final int m_q = 31760;
    public final int item_1 = 7832;
    public final int item_2 = 7833;

    public _704_Missqueen() {
        super(false);
        addStartNpc(31760);
        addTalkId(31760);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = "noquest";
        if ("31760-02.htm".equals(event)) {
            if (st.getCond() == 0 && st.getPlayer().getLevel() <= 20 && st.getPlayer().getLevel() >= 6 && st.getPlayer().getPkKills() == 0) {
                st.giveItems(7832, 1L);
                st.setCond(1);
                htmltext = "c_1.htm";
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "fail-01.htm";
            }
        } else if ("31760-03.htm".equals(event)) {
            if (st.getInt("m_scond") == 0 && st.getPlayer().getLevel() <= 25 && st.getPlayer().getLevel() >= 20 && st.getPlayer().getPkKills() == 0) {
                st.giveItems(7833, 1L);
                st.set("m_scond", "1");
                htmltext = "c_2.htm";
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "fail-02.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        if (npcId == 31760) {
            htmltext = "31760-01.htm";
        }
        return htmltext;
    }
}
