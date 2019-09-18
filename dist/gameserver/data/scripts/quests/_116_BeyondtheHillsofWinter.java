package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _116_BeyondtheHillsofWinter extends Quest {
    public final int FILAUR = 30535;
    public final int OBI = 32052;
    public final int Supplying_Goods_for_Railroad_Worker = 8098;
    public final int Bandage = 1833;
    public final int Energy_Stone = 5589;
    public final int Thief_Key = 1661;
    public final int SSD = 1463;

    public _116_BeyondtheHillsofWinter() {
        super(false);
        addStartNpc(30535);
        addTalkId(32052);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("elder_filaur_q0116_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("elder_filaur_q0116_0201.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(1833) >= 20L && st.getQuestItemsCount(5589) >= 5L && st.getQuestItemsCount(1661) >= 10L) {
                st.takeItems(1833, 20L);
                st.takeItems(5589, 5L);
                st.takeItems(1661, 10L);
                st.giveItems(8098, 1L);
                st.setCond(2);
                st.setState(2);
            } else {
                htmltext = "elder_filaur_q0116_0104.htm";
            }
        } else if ("materials".equalsIgnoreCase(event)) {
            htmltext = "railman_obi_q0116_0302.htm";
            st.takeItems(8098, 1L);
            st.giveItems(1463, 1650L);
            st.exitCurrentQuest(false);
        } else if ("adena".equalsIgnoreCase(event)) {
            htmltext = "railman_obi_q0116_0302.htm";
            st.takeItems(8098, 1L);
            st.giveItems(57, 16500L);
            st.exitCurrentQuest(false);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        int cond = 0;
        if (id != 1) {
            cond = st.getCond();
        }
        if (npcId == 30535) {
            switch (cond) {
                case 0:
                    if (st.getPlayer().getLevel() < 30) {
                        htmltext = "elder_filaur_q0116_0103.htm";
                        st.exitCurrentQuest(true);
                    } else {
                        htmltext = "elder_filaur_q0116_0101.htm";
                    }
                    break;
                case 1:
                    htmltext = "elder_filaur_q0116_0105.htm";
                    break;
                case 2:
                    htmltext = "elder_filaur_q0116_0201.htm";
                    break;
            }
        } else if (npcId == 32052 && cond == 2 && st.getQuestItemsCount(8098) > 0L) {
            htmltext = "railman_obi_q0116_0201.htm";
        }
        return htmltext;
    }
}
