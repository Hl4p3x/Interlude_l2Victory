package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _036_MakeASewingKit extends Quest {
    int REINFORCED_STEEL;
    int ARTISANS_FRAME;
    int ORIHARUKON;
    int SEWING_KIT;

    public _036_MakeASewingKit() {
        super(false);
        REINFORCED_STEEL = 7163;
        ARTISANS_FRAME = 1891;
        ORIHARUKON = 1893;
        SEWING_KIT = 7078;
        addStartNpc(30847);
        addTalkId(30847);
        addTalkId(30847);
        addKillId(20566);
        addQuestItem(REINFORCED_STEEL);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int cond = st.getCond();
        if ("head_blacksmith_ferris_q0036_0104.htm".equals(event) && cond == 0) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("head_blacksmith_ferris_q0036_0201.htm".equals(event) && cond == 2) {
            st.takeItems(REINFORCED_STEEL, 5L);
            st.setCond(3);
        } else if ("head_blacksmith_ferris_q0036_0301.htm".equals(event)) {
            if (st.getQuestItemsCount(ORIHARUKON) >= 10L && st.getQuestItemsCount(ARTISANS_FRAME) >= 10L) {
                st.takeItems(ORIHARUKON, 10L);
                st.takeItems(ARTISANS_FRAME, 10L);
                st.giveItems(SEWING_KIT, 1L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            } else {
                htmltext = "head_blacksmith_ferris_q0036_0203.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0 && st.getQuestItemsCount(SEWING_KIT) == 0L) {
            if (st.getPlayer().getLevel() >= 60) {
                final QuestState fwear = st.getPlayer().getQuestState(_037_PleaseMakeMeFormalWear.class);
                if (fwear != null && fwear.getState() == 2) {
                    if (fwear.getCond() == 6) {
                        htmltext = "head_blacksmith_ferris_q0036_0101.htm";
                    } else {
                        st.exitCurrentQuest(true);
                    }
                } else {
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "head_blacksmith_ferris_q0036_0103.htm";
            }
        } else if (cond == 1 && st.getQuestItemsCount(REINFORCED_STEEL) < 5L) {
            htmltext = "head_blacksmith_ferris_q0036_0106.htm";
        } else if (cond == 2 && st.getQuestItemsCount(REINFORCED_STEEL) == 5L) {
            htmltext = "head_blacksmith_ferris_q0036_0105.htm";
        } else if (cond == 3 && (st.getQuestItemsCount(ORIHARUKON) < 10L || st.getQuestItemsCount(ARTISANS_FRAME) < 10L)) {
            htmltext = "head_blacksmith_ferris_q0036_0204.htm";
        } else if (cond == 3 && st.getQuestItemsCount(ORIHARUKON) >= 10L && st.getQuestItemsCount(ARTISANS_FRAME) >= 10L) {
            htmltext = "head_blacksmith_ferris_q0036_0203.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(REINFORCED_STEEL) < 5L) {
            st.giveItems(REINFORCED_STEEL, 1L);
            if (st.getQuestItemsCount(REINFORCED_STEEL) == 5L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
