package quests;

import bosses.FourSepulchersManager;
import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.stream.IntStream;

public class _620_FourGoblets extends Quest {
    public static final int Sealed_Box = 7255;
    private static final int NAMELESS_SPIRIT = 31453;
    private static final int GHOST_OF_WIGOTH_1 = 31452;
    private static final int GHOST_OF_WIGOTH_2 = 31454;
    private static final int CONQ_SM = 31921;
    private static final int EMPER_SM = 31922;
    private static final int SAGES_SM = 31923;
    private static final int JUDGE_SM = 31924;
    private static final int GHOST_CHAMBERLAIN_1 = 31919;
    private static final int GHOST_CHAMBERLAIN_2 = 31920;
    private static final int GRAVE_PASS = 7261;
    private static final int[] GOBLETS = {7256, 7257, 7258, 7259};
    private static final int RELIC = 7254;
    private static final int ANTIQUE_BROOCH = 7262;
    private static final int[] RCP_REWARDS = {6881, 6883, 6885, 6887, 6891, 6893, 6895, 6897, 6899, 7580};

    public _620_FourGoblets() {
        super(false);
        addStartNpc(NAMELESS_SPIRIT, CONQ_SM, EMPER_SM, SAGES_SM, JUDGE_SM, GHOST_CHAMBERLAIN_1, GHOST_CHAMBERLAIN_2);
        addTalkId(GHOST_OF_WIGOTH_1, GHOST_OF_WIGOTH_2);
        addQuestItem(7255, GRAVE_PASS);
        addQuestItem(GOBLETS);
        IntStream.rangeClosed(18120, 18256).forEach(this::addKillId);
    }

    private static String onOpenBoxes(final QuestState st, final String count) {
        try {
            return new OpenSealedBox(st, Integer.parseInt(count)).apply();
        } catch (Exception e) {
            e.printStackTrace();
            return "Dont try to cheat with me!";
        }
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final Player player = st.getPlayer();
        final int cond = st.getCond();
        if ("Enter".equalsIgnoreCase(event)) {
            FourSepulchersManager.tryEntry(npc, player);
            return null;
        }
        if ("accept".equalsIgnoreCase(event)) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 74) {
                    st.setState(2);
                    st.playSound("ItemSound.quest_accept");
                    st.setCond(1);
                    return "31453-13.htm";
                }
                st.exitCurrentQuest(true);
                return "31453-12.htm";
            }
        } else {
            if (event.startsWith("openBoxes ")) {
                return onOpenBoxes(st, event.replace("openBoxes ", "").trim());
            }
            if ("12".equalsIgnoreCase(event)) {
                if (!st.checkQuestItemsCount(GOBLETS)) {
                    return "31453-14.htm";
                }
                st.takeAllItems(GOBLETS);
                st.giveItems(ANTIQUE_BROOCH, 1L);
                st.setCond(2);
                st.playSound("ItemSound.quest_finish");
                return "31453-16.htm";
            } else {
                if ("13".equalsIgnoreCase(event)) {
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                    return "31453-18.htm";
                }
                if ("14".equalsIgnoreCase(event)) {
                    if (cond == 2) {
                        return "31453-19.htm";
                    }
                    return "31453-13.htm";
                } else if ("15".equalsIgnoreCase(event)) {
                    if (st.getQuestItemsCount(ANTIQUE_BROOCH) >= 1L) {
                        st.getPlayer().teleToLocation(169608, -91256, -2912);
                        return null;
                    }
                    if (st.getQuestItemsCount(GRAVE_PASS) >= 1L) {
                        st.takeItems(GRAVE_PASS, 1L);
                        st.getPlayer().teleToLocation(169608, -91256, -2912);
                        return null;
                    }
                    return "" + str((long) npc.getNpcId()) + "-0.htm";
                } else if ("16".equalsIgnoreCase(event)) {
                    if (st.getQuestItemsCount(ANTIQUE_BROOCH) >= 1L) {
                        st.getPlayer().teleToLocation(186942, -75602, -2834);
                        return null;
                    }
                    if (st.getQuestItemsCount(GRAVE_PASS) >= 1L) {
                        st.takeItems(GRAVE_PASS, 1L);
                        st.getPlayer().teleToLocation(186942, -75602, -2834);
                        return null;
                    }
                    return "" + str((long) npc.getNpcId()) + "-0.htm";
                } else {
                    if ("17".equalsIgnoreCase(event)) {
                        if (st.getQuestItemsCount(ANTIQUE_BROOCH) >= 1L) {
                            st.getPlayer().teleToLocation(169590, -90218, -2914);
                        } else {
                            st.takeItems(GRAVE_PASS, 1L);
                            st.getPlayer().teleToLocation(169590, -90218, -2914);
                        }
                        return "31452-6.htm";
                    }
                    if ("18".equalsIgnoreCase(event)) {
                        if (st.getSumQuestItemsCount(GOBLETS) < 3L) {
                            return "31452-3.htm";
                        }
                        if (st.getSumQuestItemsCount(GOBLETS) == 3L) {
                            return "31452-4.htm";
                        }
                        if (st.getSumQuestItemsCount(GOBLETS) >= 4L) {
                            return "31452-5.htm";
                        }
                    } else {
                        if ("19".equalsIgnoreCase(event)) {
                            return new OpenSealedBox(st, 1).apply();
                        }
                        if (event.startsWith("19 ")) {
                            return onOpenBoxes(st, event.replaceFirst("19 ", ""));
                        }
                        if ("11".equalsIgnoreCase(event)) {
                            return "<html><body><a action=\"bypass -h Quest _620_FourGoblets 19\">\"Please open a box.\"</a><br><a action=\"bypass -h Quest _620_FourGoblets 19 5\">\"Please open 5 boxes.\"</a><br><a action=\"bypass -h Quest _620_FourGoblets 19 10\">\"Please open 10 boxes.\"</a><br><a action=\"bypass -h Quest _620_FourGoblets 19 50\">\"Please open 50 boxes.\"</a><br></body></html>";
                        }
                        int id = 0;
                        try {
                            id = Integer.parseInt(event);
                        } catch (Exception ignored) {
                        }
                        if (ArrayUtils.contains(RCP_REWARDS, id)) {
                            st.takeItems(RELIC, 1000L);
                            st.giveItems(id, 1L);
                            return "31454-17.htm";
                        }
                    }
                }
            }
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        final int cond = st.getCond();
        if (id == 1) {
            st.setCond(0);
        }
        switch (npcId) {
            case NAMELESS_SPIRIT:
                switch (cond) {
                    case 0:
                        if (st.getPlayer().getLevel() >= 74) {
                            htmltext = "31453-1.htm";
                        } else {
                            htmltext = "31453-12.htm";
                            st.exitCurrentQuest(true);
                        }
                        break;
                    case 1:
                        if (st.checkQuestItemsCount(GOBLETS)) {
                            htmltext = "31453-15.htm";
                        } else {
                            htmltext = "31453-14.htm";
                        }
                        break;
                    case 2:
                        htmltext = "31453-17.htm";
                        break;
                }
                break;
            case GHOST_OF_WIGOTH_1:
                if (cond == 2) {
                    htmltext = "31452-2.htm";
                } else if (cond == 1) {
                    if (st.getSumQuestItemsCount(GOBLETS) == 1L) {
                        htmltext = "31452-1.htm";
                    } else if (st.getSumQuestItemsCount(GOBLETS) > 1L) {
                        htmltext = "31452-2.htm";
                    }
                }
                break;
            case GHOST_OF_WIGOTH_2:
                if (st.getQuestItemsCount(RELIC) >= 1000L) {
                    if (st.getQuestItemsCount(7255) >= 1L) {
                        if (st.checkQuestItemsCount(GOBLETS)) {
                            htmltext = "31454-4.htm";
                        } else if (st.checkQuestItemsCount(GOBLETS)) {
                            htmltext = "31454-8.htm";
                        } else {
                            htmltext = "31454-12.htm";
                        }
                    } else if (st.checkQuestItemsCount(GOBLETS)) {
                        htmltext = "31454-3.htm";
                    } else if (st.getSumQuestItemsCount(GOBLETS) > 1L) {
                        htmltext = "31454-7.htm";
                    } else {
                        htmltext = "31454-11.htm";
                    }
                } else if (st.getQuestItemsCount(7255) >= 1L) {
                    if (st.checkQuestItemsCount(GOBLETS)) {
                        htmltext = "31454-2.htm";
                    } else if (st.getSumQuestItemsCount(GOBLETS) > 1L) {
                        htmltext = "31454-6.htm";
                    } else {
                        htmltext = "31454-10.htm";
                    }
                } else if (st.checkQuestItemsCount(GOBLETS)) {
                    htmltext = "31454-1.htm";
                } else if (st.getSumQuestItemsCount(GOBLETS) > 1L) {
                    htmltext = "31454-5.htm";
                } else {
                    htmltext = "31454-9.htm";
                }
                break;
            case CONQ_SM:
                htmltext = "31921-E.htm";
                break;
            case EMPER_SM:
                htmltext = "31922-E.htm";
                break;
            case SAGES_SM:
                htmltext = "31923-E.htm";
                break;
            case JUDGE_SM:
                htmltext = "31924-E.htm";
                break;
            case GHOST_CHAMBERLAIN_1:
                htmltext = "31919-1.htm";
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if ((cond == 1 || cond == 2) && npcId >= 18120 && npcId <= 18256 && Rnd.chance(30)) {
            st.giveItems(7255, 1L);
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
