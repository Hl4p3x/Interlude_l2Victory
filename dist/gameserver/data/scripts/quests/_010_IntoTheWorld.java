package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _010_IntoTheWorld extends Quest {
    private final int VERY_EXPENSIVE_NECKLACE = 7574;
    private final int SCROLL_OF_ESCAPE_GIRAN = 7126;
    private final int MARK_OF_TRAVELER = 7570;
    private final int BALANKI = 30533;
    private final int REED = 30520;
    private final int GERALD = 30650;

    public _010_IntoTheWorld() {
        super(false);
        addStartNpc(BALANKI);
        addTalkId(BALANKI, REED, GERALD);
        addQuestItem(VERY_EXPENSIVE_NECKLACE);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("elder_balanki_q0010_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("warehouse_chief_reed_q0010_0201.htm".equalsIgnoreCase(event)) {
            st.giveItems(VERY_EXPENSIVE_NECKLACE, 1L);
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
        } else if ("gerald_priest_of_earth_q0010_0301.htm".equalsIgnoreCase(event)) {
            st.takeItems(VERY_EXPENSIVE_NECKLACE, -1L);
            st.setCond(3);
            st.playSound("ItemSound.quest_middle");
        } else if ("warehouse_chief_reed_q0010_0401.htm".equalsIgnoreCase(event)) {
            st.setCond(4);
            st.playSound("ItemSound.quest_middle");
        } else if ("elder_balanki_q0010_0501.htm".equalsIgnoreCase(event)) {
            st.giveItems(SCROLL_OF_ESCAPE_GIRAN, 1L);
            st.giveItems(MARK_OF_TRAVELER, 1L);
            st.exitCurrentQuest(false);
            st.playSound("ItemSound.quest_finish");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == BALANKI) {
            switch (cond) {
                case 0:
                    if (st.getPlayer().getRace() == Race.dwarf && st.getPlayer().getLevel() >= 3) {
                        htmltext = "elder_balanki_q0010_0101.htm";
                    } else {
                        htmltext = "elder_balanki_q0010_0102.htm";
                        st.exitCurrentQuest(true);
                    }
                    break;
                case 1:
                    htmltext = "elder_balanki_q0010_0105.htm";
                    break;
                case 4:
                    htmltext = "elder_balanki_q0010_0401.htm";
                    break;
            }
        } else if (npcId == REED) {
            switch (cond) {
                case 1:
                    htmltext = "warehouse_chief_reed_q0010_0101.htm";
                    break;
                case 2:
                    htmltext = "warehouse_chief_reed_q0010_0202.htm";
                    break;
                case 3:
                    htmltext = "warehouse_chief_reed_q0010_0301.htm";
                    break;
                case 4:
                    htmltext = "warehouse_chief_reed_q0010_0402.htm";
                    break;
            }
        } else if (npcId == GERALD) {
            if (cond == 2 && st.getQuestItemsCount(VERY_EXPENSIVE_NECKLACE) > 0L) {
                htmltext = "gerald_priest_of_earth_q0010_0201.htm";
            } else if (cond == 3) {
                htmltext = "gerald_priest_of_earth_q0010_0302.htm";
            } else {
                htmltext = "gerald_priest_of_earth_q0010_0303.htm";
            }
        }
        return htmltext;
    }
}
