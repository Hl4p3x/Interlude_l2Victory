package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _631_DeliciousTopChoiceMeat extends Quest {
    public final int TUNATUN = 31537;
    public final int[] MOB_LIST;
    public final int TOP_QUALITY_MEAT = 7546;
    public final int MOLD_GLUE = 4039;
    public final int MOLD_LUBRICANT = 4040;
    public final int MOLD_HARDENER = 4041;
    public final int ENRIA = 4042;
    public final int ASOFE = 4043;
    public final int THONS = 4044;
    public final int[][] REWARDS;

    public _631_DeliciousTopChoiceMeat() {
        super(false);
        MOB_LIST = new int[]{21460, 21461, 21462, 21463, 21464, 21465, 21466, 21467, 21468, 21469, 21479, 21480, 21481, 21482, 21483, 21484, 21485, 21486, 21487, 21488, 21498, 21499, 21500, 21501, 21502, 21503, 21504, 21505, 21506, 21507};
        REWARDS = new int[][]{{1, 4039, 15}, {2, 4043, 15}, {3, 4044, 15}, {4, 4040, 10}, {5, 4042, 10}, {6, 4041, 5}};
        addStartNpc(31537);
        addTalkId(31537);
        for (final int i : MOB_LIST) {
            addKillId(i);
        }
        addQuestItem(7546);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("31537-03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("31537-05.htm".equalsIgnoreCase(event) && st.getQuestItemsCount(7546) >= 120L) {
            st.setCond(3);
        }
        for (final int[] element : REWARDS) {
            if (event.equalsIgnoreCase(String.valueOf(element[0]))) {
                if (st.getCond() == 3 && st.getQuestItemsCount(7546) >= 120L) {
                    htmltext = "31537-06.htm";
                    st.takeItems(7546, -1L);
                    st.giveItems(element[1], (long) (element[2] * (int) st.getRateQuestsReward()));
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "31537-07.htm";
                    st.setCond(1);
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond < 1) {
            if (st.getPlayer().getLevel() < 65) {
                htmltext = "31537-02.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "31537-01.htm";
            }
        } else if (cond == 1) {
            htmltext = "31537-01a.htm";
        } else if (cond == 2) {
            if (st.getQuestItemsCount(7546) < 120L) {
                htmltext = "31537-01a.htm";
                st.setCond(1);
            } else {
                htmltext = "31537-04.htm";
            }
        } else if (cond == 3) {
            htmltext = "31537-05.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (st.getCond() == 1) {
            for (final int i : MOB_LIST) {
                if (Rnd.chance(80) && npcId == i) {
                    st.rollAndGive(7546, 1, 100.0);
                    if (st.getQuestItemsCount(7546) < 120L) {
                        st.playSound("ItemSound.quest_itemget");
                    } else {
                        st.playSound("ItemSound.quest_middle");
                        st.setCond(2);
                    }
                }
            }
        }
        return null;
    }
}
