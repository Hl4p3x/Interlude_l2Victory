package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;

public class _021_HiddenTruth extends Quest {
    public static final int DARIN = 30048;
    public static final int ROXXY = 30006;
    public static final int BAULRO = 30033;
    public static final int MysteriousWizard = 31522;
    public static final int Tombstone = 31523;
    public static final int GhostofvonHellmannId = 31524;
    public static final int GhostofvonHellmannsPageId = 31525;
    public static final int BrokenBookshelf = 31526;
    public static final int Agripel = 31348;
    public static final int Dominic = 31350;
    public static final int Benedict = 31349;
    public static final int Innocentin = 31328;
    public static final int CrossofEinhasad = 7140;
    public static final int CrossofEinhasadNextQuest = 7141;
    public NpcInstance GhostofvonHellmannsPage;
    public NpcInstance GhostofvonHellmann;

    public _021_HiddenTruth() {
        super(false);
        addStartNpc(31522);
        addTalkId(31523);
        addTalkId(31524);
        addTalkId(31525);
        addTalkId(31526);
        addTalkId(31348);
        addTalkId(31350);
        addTalkId(31349);
        addTalkId(31328);
    }

    private void spawnGhostofvonHellmannsPage() {
        GhostofvonHellmannsPage = Functions.spawn(new Location(51462, -54539, -3176), 31525);
    }

    private void despawnGhostofvonHellmannsPage() {
        if (GhostofvonHellmannsPage != null) {
            GhostofvonHellmannsPage.deleteMe();
        }
        GhostofvonHellmannsPage = null;
    }

    private void spawnGhostofvonHellmann() {
        GhostofvonHellmann = Functions.spawn(Location.findPointToStay(new Location(51432, -54570, -3136), 50, ReflectionManager.DEFAULT.getGeoIndex()), 31524);
    }

    private void despawnGhostofvonHellmann() {
        if (GhostofvonHellmann != null) {
            GhostofvonHellmann.deleteMe();
        }
        GhostofvonHellmann = null;
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("31522-02.htm".equalsIgnoreCase(event)) {
            st.setState(2);
            st.setCond(1);
        } else if ("html".equalsIgnoreCase(event)) {
            htmltext = "31328-05.htm";
        } else if ("31328-05.htm".equalsIgnoreCase(event)) {
            st.unset("cond");
            st.takeItems(7140, -1L);
            if (st.getQuestItemsCount(7141) == 0L) {
                st.giveItems(7141, 1L);
            }
            st.playSound("ItemSound.quest_finish");
            st.startQuestTimer("html", 1L);
            htmltext = "Congratulations! You are completed this quest!<br>The Quest \"Tragedy In Von Hellmann Forest\" become available.<br>Show Cross of Einhasad to High Priest Tifaren.";
            st.exitCurrentQuest(false);
        } else if ("31523-03.htm".equalsIgnoreCase(event)) {
            st.playSound("SkillSound5.horror_02");
            st.setCond(2);
            despawnGhostofvonHellmann();
            spawnGhostofvonHellmann();
        } else if ("31524-06.htm".equalsIgnoreCase(event)) {
            st.setCond(3);
            despawnGhostofvonHellmannsPage();
            spawnGhostofvonHellmannsPage();
        } else if ("31526-03.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.item_drop_equip_armor_cloth");
        } else if ("31526-08.htm".equalsIgnoreCase(event)) {
            st.playSound("AmdSound.ed_chimes_05");
            st.setCond(5);
        } else if ("31526-14.htm".equalsIgnoreCase(event)) {
            st.giveItems(7140, 1L);
            st.setCond(6);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 31522) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() > 54) {
                    htmltext = "31522-01.htm";
                } else {
                    htmltext = "31522-03.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "31522-05.htm";
            }
        } else if (npcId == 31523) {
            if (cond == 1) {
                htmltext = "31523-01.htm";
            } else if (cond == 2 || cond == 3) {
                htmltext = "31523-04.htm";
                st.playSound("SkillSound5.horror_02");
                despawnGhostofvonHellmann();
                spawnGhostofvonHellmann();
            }
        } else if (npcId == 31524) {
            switch (cond) {
                case 2:
                    htmltext = "31524-01.htm";
                    break;
                case 3:
                    htmltext = "31524-07b.htm";
                    break;
                case 4:
                    htmltext = "31524-07c.htm";
                    break;
            }
        } else if (npcId == 31525) {
            if (cond == 3 || cond == 4) {
                htmltext = "31525-01.htm";
                if (GhostofvonHellmannsPage == null || !GhostofvonHellmannsPage.isMoving()) {
                    htmltext = "31525-02.htm";
                    if (cond == 3) {
                        st.setCond(4);
                    }
                    despawnGhostofvonHellmannsPage();
                }
            }
        } else if (npcId == 31526) {
            switch (cond) {
                case 4:
                case 3:
                    despawnGhostofvonHellmannsPage();
                    despawnGhostofvonHellmann();
                    st.setCond(5);
                    htmltext = "31526-01.htm";
                    break;
                case 5:
                    htmltext = "31526-10.htm";
                    st.playSound("AmdSound.ed_chimes_05");
                    break;
                case 6:
                    htmltext = "31526-15.htm";
                    break;
            }
        } else if (npcId == 31348 && st.getQuestItemsCount(7140) >= 1L) {
            if (cond == 6) {
                if (st.getInt("DOMINIC") == 1 && st.getInt("BENEDICT") == 1) {
                    htmltext = "31348-02.htm";
                    st.setCond(7);
                } else {
                    st.set("AGRIPEL", "1");
                    htmltext = "31348-0" + Rnd.get(3) + ".htm";
                }
            } else if (cond == 7) {
                htmltext = "31348-03.htm";
            }
        } else if (npcId == 31350 && st.getQuestItemsCount(7140) >= 1L) {
            if (cond == 6) {
                if (st.getInt("AGRIPEL") == 1 && st.getInt("BENEDICT") == 1) {
                    htmltext = "31350-02.htm";
                    st.setCond(7);
                } else {
                    st.set("DOMINIC", "1");
                    htmltext = "31350-0" + Rnd.get(3) + ".htm";
                }
            } else if (cond == 7) {
                htmltext = "31350-03.htm";
            }
        } else if (npcId == 31349 && st.getQuestItemsCount(7140) >= 1L) {
            if (cond == 6) {
                if (st.getInt("AGRIPEL") == 1 && st.getInt("DOMINIC") == 1) {
                    htmltext = "31349-02.htm";
                    st.setCond(7);
                } else {
                    st.set("BENEDICT", "1");
                    htmltext = "31349-0" + Rnd.get(3) + ".htm";
                }
            } else if (cond == 7) {
                htmltext = "31349-03.htm";
            }
        } else if (npcId == 31328) {
            if (cond == 7) {
                if (st.getQuestItemsCount(7140) != 0L) {
                    htmltext = "31328-01.htm";
                }
            } else if (cond == 0) {
                htmltext = "31328-06.htm";
            }
        }
        return htmltext;
    }
}
