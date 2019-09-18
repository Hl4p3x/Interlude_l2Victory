package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _008_AnAdventureBegins extends Quest {
    private final int JASMINE = 30134;
    private final int ROSELYN = 30355;
    private final int HARNE = 30144;
    private final int ROSELYNS_NOTE = 7573;
    private final int SCROLL_OF_ESCAPE_GIRAN = 7126;
    private final int MARK_OF_TRAVELER = 7570;

    public _008_AnAdventureBegins() {
        super(false);
        addStartNpc(JASMINE);
        addTalkId(JASMINE, ROSELYN, HARNE);
        addQuestItem(ROSELYNS_NOTE);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("jasmine_q0008_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("sentry_roseline_q0008_0201.htm".equalsIgnoreCase(event)) {
            st.giveItems(ROSELYNS_NOTE, 1L);
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
        } else if ("harne_q0008_0301.htm".equalsIgnoreCase(event)) {
            st.takeItems(ROSELYNS_NOTE, -1L);
            st.setCond(3);
            st.playSound("ItemSound.quest_middle");
        } else if ("jasmine_q0008_0401.htm".equalsIgnoreCase(event)) {
            st.giveItems(SCROLL_OF_ESCAPE_GIRAN, 1L);
            st.giveItems(MARK_OF_TRAVELER, 1L);
            st.setCond(0);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == JASMINE) {
            if (cond == 0 && st.getPlayer().getRace() == Race.darkelf) {
                if (st.getPlayer().getLevel() >= 3) {
                    htmltext = "jasmine_q0008_0101.htm";
                } else {
                    htmltext = "jasmine_q0008_0102.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "jasmine_q0008_0105.htm";
            } else if (cond == 3) {
                htmltext = "jasmine_q0008_0301.htm";
            }
        } else if (npcId == ROSELYN) {
            if (st.getQuestItemsCount(ROSELYNS_NOTE) == 0L) {
                htmltext = "sentry_roseline_q0008_0101.htm";
            } else {
                htmltext = "sentry_roseline_q0008_0202.htm";
            }
        } else if (npcId == HARNE) {
            if (cond == 2 && st.getQuestItemsCount(ROSELYNS_NOTE) > 0L) {
                htmltext = "harne_q0008_0201.htm";
            } else if (cond == 2 && st.getQuestItemsCount(ROSELYNS_NOTE) == 0L) {
                htmltext = "harne_q0008_0302.htm";
            } else if (cond == 3) {
                htmltext = "harne_q0008_0303.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        return null;
    }
}
