package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _034_InSearchOfClothes extends Quest {
    int SPINNERET;
    int SUEDE;
    int THREAD;
    int SPIDERSILK;
    int MYSTERIOUS_CLOTH;

    public _034_InSearchOfClothes() {
        super(false);
        SPINNERET = 7528;
        SUEDE = 1866;
        THREAD = 1868;
        SPIDERSILK = 1493;
        MYSTERIOUS_CLOTH = 7076;
        addStartNpc(30088);
        addTalkId(30088);
        addTalkId(30165);
        addTalkId(30294);
        addKillId(20560);
        addQuestItem(SPINNERET);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int cond = st.getCond();
        if ("30088-1.htm".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("30294-1.htm".equals(event) && cond == 1) {
            st.setCond(2);
        } else if ("30088-3.htm".equals(event) && cond == 2) {
            st.setCond(3);
        } else if ("30165-1.htm".equals(event) && cond == 3) {
            st.setCond(4);
        } else if ("30165-3.htm".equals(event) && cond == 5) {
            if (st.getQuestItemsCount(SPINNERET) == 10L) {
                st.takeItems(SPINNERET, 10L);
                st.giveItems(SPIDERSILK, 1L);
                st.setCond(6);
            } else {
                htmltext = "30165-1r.htm";
            }
        } else if ("30088-5.htm".equals(event) && cond == 6) {
            if (st.getQuestItemsCount(SUEDE) >= 3000L && st.getQuestItemsCount(THREAD) >= 5000L && st.getQuestItemsCount(SPIDERSILK) == 1L) {
                st.takeItems(SUEDE, 3000L);
                st.takeItems(THREAD, 5000L);
                st.takeItems(SPIDERSILK, 1L);
                st.giveItems(MYSTERIOUS_CLOTH, 1L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            } else {
                htmltext = "30088-havent.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        switch (npcId) {
            case 30088:
                if (cond == 0 && st.getQuestItemsCount(MYSTERIOUS_CLOTH) == 0L) {
                    if (st.getPlayer().getLevel() >= 60) {
                        final QuestState fwear = st.getPlayer().getQuestState(_037_PleaseMakeMeFormalWear.class);
                        if (fwear != null && fwear.getCond() == 6) {
                            htmltext = "30088-0.htm";
                        } else {
                            st.exitCurrentQuest(true);
                        }
                    } else {
                        htmltext = "30088-6.htm";
                    }
                } else if (cond == 1) {
                    htmltext = "30088-1r.htm";
                } else if (cond == 2) {
                    htmltext = "30088-2.htm";
                } else if (cond == 3) {
                    htmltext = "30088-3r.htm";
                } else if (cond == 6 && (st.getQuestItemsCount(SUEDE) < 3000L || st.getQuestItemsCount(THREAD) < 5000L || st.getQuestItemsCount(SPIDERSILK) < 1L)) {
                    htmltext = "30088-havent.htm";
                } else if (cond == 6) {
                    htmltext = "30088-4.htm";
                }
                break;
            case 30294:
                if (cond == 1) {
                    htmltext = "30294-0.htm";
                } else if (cond == 2) {
                    htmltext = "30294-1r.htm";
                }
                break;
            case 30165:
                if (cond == 3) {
                    htmltext = "30165-0.htm";
                } else if (cond == 4 && st.getQuestItemsCount(SPINNERET) < 10L) {
                    htmltext = "30165-1r.htm";
                } else if (cond == 5) {
                    htmltext = "30165-2.htm";
                } else if (cond == 6 && (st.getQuestItemsCount(SUEDE) < 3000L || st.getQuestItemsCount(THREAD) < 5000L || st.getQuestItemsCount(SPIDERSILK) < 1L)) {
                    htmltext = "30165-3r.htm";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(SPINNERET) < 10L) {
            st.giveItems(SPINNERET, 1L);
            if (st.getQuestItemsCount(SPINNERET) == 10L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(5);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
