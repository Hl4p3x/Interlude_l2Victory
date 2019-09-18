package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _047_IntoTheDarkForest extends Quest {
    private static final int GALLADUCCIS_ORDER_DOCUMENT_ID_1 = 7563;
    private static final int GALLADUCCIS_ORDER_DOCUMENT_ID_2 = 7564;
    private static final int GALLADUCCIS_ORDER_DOCUMENT_ID_3 = 7565;
    private static final int MAGIC_SWORD_HILT_ID = 7568;
    private static final int GEMSTONE_POWDER_ID = 7567;
    private static final int PURIFIED_MAGIC_NECKLACE_ID = 7566;
    private static final int MARK_OF_TRAVELER_ID = 7570;
    private static final int SCROLL_OF_ESCAPE_DARK_ELF_VILLAGE = 7119;

    public _047_IntoTheDarkForest() {
        super(false);
        addStartNpc(30097);
        addTalkId(30097);
        addTalkId(30097);
        addTalkId(30097);
        addTalkId(30094);
        addTalkId(30090);
        addTalkId(30116);
        addQuestItem(7563, 7564, 7565, 7568, 7567, 7566);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "1":
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                st.giveItems(7563, 1L);
                htmltext = "galladuchi_q0047_0104.htm";
                break;
            case "2":
                st.setCond(2);
                st.takeItems(7563, 1L);
                st.giveItems(7568, 1L);
                htmltext = "gentler_q0047_0201.htm";
                break;
            case "3":
                st.setCond(3);
                st.takeItems(7568, 1L);
                st.giveItems(7564, 1L);
                htmltext = "galladuchi_q0047_0301.htm";
                break;
            case "4":
                st.setCond(4);
                st.takeItems(7564, 1L);
                st.giveItems(7567, 1L);
                htmltext = "sandra_q0047_0401.htm";
                break;
            case "5":
                st.setCond(5);
                st.takeItems(7567, 1L);
                st.giveItems(7565, 1L);
                htmltext = "galladuchi_q0047_0501.htm";
                break;
            case "6":
                st.setCond(6);
                st.takeItems(7565, 1L);
                st.giveItems(7566, 1L);
                htmltext = "dustin_q0047_0601.htm";
                break;
            case "7":
                st.giveItems(7119, 1L);
                st.takeItems(7566, 1L);
                htmltext = "galladuchi_q0047_0701.htm";
                st.setCond(0);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(false);
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        if (id == 1) {
            if (st.getPlayer().getRace() != Race.darkelf || st.getQuestItemsCount(7570) == 0L) {
                htmltext = "galladuchi_q0047_0102.htm";
                st.exitCurrentQuest(true);
            } else if (st.getPlayer().getLevel() < 3) {
                htmltext = "galladuchi_q0047_0103.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "galladuchi_q0047_0101.htm";
            }
        } else if (npcId == 30097 && st.getCond() == 1) {
            htmltext = "galladuchi_q0047_0105.htm";
        } else if (npcId == 30097 && st.getCond() == 2) {
            htmltext = "galladuchi_q0047_0201.htm";
        } else if (npcId == 30097 && st.getCond() == 3) {
            htmltext = "galladuchi_q0047_0303.htm";
        } else if (npcId == 30097 && st.getCond() == 4) {
            htmltext = "galladuchi_q0047_0401.htm";
        } else if (npcId == 30097 && st.getCond() == 5) {
            htmltext = "galladuchi_q0047_0503.htm";
        } else if (npcId == 30097 && st.getCond() == 6) {
            htmltext = "galladuchi_q0047_0601.htm";
        } else if (npcId == 30094 && st.getCond() == 1) {
            htmltext = "gentler_q0047_0101.htm";
        } else if (npcId == 30094 && st.getCond() == 2) {
            htmltext = "gentler_q0047_0203.htm";
        } else if (npcId == 30090 && st.getCond() == 3) {
            htmltext = "sandra_q0047_0301.htm";
        } else if (npcId == 30090 && st.getCond() == 4) {
            htmltext = "sandra_q0047_0403.htm";
        } else if (npcId == 30116 && st.getCond() == 5) {
            htmltext = "dustin_q0047_0501.htm";
        } else if (npcId == 30116 && st.getCond() == 6) {
            htmltext = "dustin_q0047_0603.htm";
        }
        return htmltext;
    }
}
