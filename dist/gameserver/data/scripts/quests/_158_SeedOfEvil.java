package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _158_SeedOfEvil extends Quest {
    int CLAY_TABLET_ID;
    int ENCHANT_ARMOR_D;

    public _158_SeedOfEvil() {
        super(false);
        CLAY_TABLET_ID = 1025;
        ENCHANT_ARMOR_D = 956;
        addStartNpc(30031);
        addKillId(27016);
        addQuestItem(CLAY_TABLET_ID);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            st.set("id", "0");
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "30031-04.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        if (id == 1) {
            st.setState(2);
            st.set("id", "0");
        }
        if (npcId == 30031 && st.getCond() == 0) {
            if (st.getCond() < 15) {
                if (st.getPlayer().getLevel() >= 21) {
                    htmltext = "30031-03.htm";
                    return htmltext;
                }
                htmltext = "30031-02.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "30031-02.htm";
                st.exitCurrentQuest(true);
            }
        } else if (npcId == 30031 && st.getCond() == 0) {
            htmltext = "completed";
        } else if (npcId == 30031 && st.getCond() != 0 && st.getQuestItemsCount(CLAY_TABLET_ID) == 0L) {
            htmltext = "30031-05.htm";
        } else if (npcId == 30031 && st.getCond() != 0 && st.getQuestItemsCount(CLAY_TABLET_ID) != 0L) {
            st.takeItems(CLAY_TABLET_ID, st.getQuestItemsCount(CLAY_TABLET_ID));
            st.playSound("ItemSound.quest_finish");
            st.giveItems(ENCHANT_ARMOR_D, 1L);
            htmltext = "30031-06.htm";
            st.exitCurrentQuest(false);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(CLAY_TABLET_ID) == 0L) {
            st.giveItems(CLAY_TABLET_ID, 1L);
            st.playSound("ItemSound.quest_middle");
            st.setCond(2);
        }
        return null;
    }
}
