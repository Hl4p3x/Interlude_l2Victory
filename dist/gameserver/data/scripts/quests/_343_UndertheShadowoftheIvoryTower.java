package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _343_UndertheShadowoftheIvoryTower extends Quest {
    public final int CEMA = 30834;
    public final int ICARUS = 30835;
    public final int MARSHA = 30934;
    public final int TRUMPIN = 30935;
    public final int[] MOBS;
    public final int ORB = 4364;
    public final int ECTOPLASM = 4365;
    public final int[] AllowClass;
    public final int CHANCE = 50;

    public _343_UndertheShadowoftheIvoryTower() {
        super(false);
        MOBS = new int[]{20563, 20564, 20565, 20566};
        AllowClass = new int[]{11, 12, 13, 14, 26, 27, 28, 39, 40, 41};
        addStartNpc(30834);
        addTalkId(30834);
        addTalkId(30835);
        addTalkId(30934);
        addTalkId(30935);
        for (final int i : MOBS) {
            addKillId(i);
        }
        addQuestItem(4364);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int random1 = Rnd.get(3);
        final int random2 = Rnd.get(2);
        final long orbs = st.getQuestItemsCount(4364);
        if ("30834-03.htm".equalsIgnoreCase(event)) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("30834-08.htm".equalsIgnoreCase(event)) {
            if (orbs > 0L) {
                st.giveItems(57, orbs * 120L);
                st.takeItems(4364, -1L);
            } else {
                htmltext = "30834-08.htm";
            }
        } else if ("30834-09.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("30934-02.htm".equalsIgnoreCase(event) || "30934-03.htm".equalsIgnoreCase(event)) {
            if (orbs < 10L) {
                htmltext = "noorbs.htm";
            } else if ("30934-03.htm".equalsIgnoreCase(event)) {
                if (orbs >= 10L) {
                    st.takeItems(4364, 10L);
                    st.set("playing", "1");
                } else {
                    htmltext = "noorbs.htm";
                }
            }
        } else if ("30934-04.htm".equalsIgnoreCase(event)) {
            if (st.getInt("playing") > 0) {
                switch (random1) {
                    case 0:
                        htmltext = "30934-05.htm";
                        st.giveItems(4364, 10L);
                        break;
                    case 1:
                        htmltext = "30934-06.htm";
                        break;
                    default:
                        htmltext = "30934-04.htm";
                        st.giveItems(4364, 20L);
                        break;
                }
                st.unset("playing");
            } else {
                htmltext = "Player is cheating";
                st.takeItems(4364, -1L);
                st.exitCurrentQuest(true);
            }
        } else if ("30934-05.htm".equalsIgnoreCase(event)) {
            if (st.getInt("playing") > 0) {
                switch (random1) {
                    case 0:
                        htmltext = "30934-04.htm";
                        st.giveItems(4364, 20L);
                        break;
                    case 1:
                        htmltext = "30934-05.htm";
                        st.giveItems(4364, 10L);
                        break;
                    default:
                        htmltext = "30934-06.htm";
                        break;
                }
                st.unset("playing");
            } else {
                htmltext = "Player is cheating";
                st.takeItems(4364, -1L);
                st.exitCurrentQuest(true);
            }
        } else if ("30934-06.htm".equalsIgnoreCase(event)) {
            if (st.getInt("playing") > 0) {
                switch (random1) {
                    case 0:
                        htmltext = "30934-04.htm";
                        st.giveItems(4364, 20L);
                        break;
                    case 1:
                        htmltext = "30934-06.htm";
                        break;
                    default:
                        htmltext = "30934-05.htm";
                        st.giveItems(4364, 10L);
                        break;
                }
                st.unset("playing");
            } else {
                htmltext = "Player is cheating";
                st.takeItems(4364, -1L);
                st.exitCurrentQuest(true);
            }
        } else if ("30935-02.htm".equalsIgnoreCase(event) || "30935-03.htm".equalsIgnoreCase(event)) {
            st.unset("toss");
            if (orbs < 10L) {
                htmltext = "noorbs.htm";
            }
        } else if ("30935-05.htm".equalsIgnoreCase(event)) {
            if (orbs >= 10L) {
                if (random2 == 0) {
                    final int toss = st.getInt("toss");
                    if (toss == 4) {
                        st.unset("toss");
                        st.giveItems(4364, 150L);
                        htmltext = "30935-07.htm";
                    } else {
                        st.set("toss", String.valueOf(toss + 1));
                        htmltext = "30935-04.htm";
                    }
                } else {
                    st.unset("toss");
                    st.takeItems(4364, 10L);
                }
            } else {
                htmltext = "noorbs.htm";
            }
        } else if ("30935-06.htm".equalsIgnoreCase(event)) {
            if (orbs >= 10L) {
                final int toss = st.getInt("toss");
                st.unset("toss");
                switch (toss) {
                    case 1:
                        st.giveItems(4364, 10L);
                        break;
                    case 2:
                        st.giveItems(4364, 30L);
                        break;
                    case 3:
                        st.giveItems(4364, 70L);
                        break;
                    case 4:
                        st.giveItems(4364, 150L);
                        break;
                }
            } else {
                htmltext = "noorbs.htm";
            }
        } else if ("30835-02.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(4365) > 0L) {
                st.takeItems(4365, 1L);
                final int random3 = Rnd.get(1000);
                if (random3 <= 119) {
                    st.giveItems(955, 1L);
                } else if (random3 <= 169) {
                    st.giveItems(951, 1L);
                } else if (random3 <= 329) {
                    st.giveItems(2511, (long) (Rnd.get(200) + 401));
                } else if (random3 <= 559) {
                    st.giveItems(2510, (long) (Rnd.get(200) + 401));
                } else if (random3 <= 561) {
                    st.giveItems(316, 1L);
                } else if (random3 <= 578) {
                    st.giveItems(630, 1L);
                } else if (random3 <= 579) {
                    st.giveItems(188, 1L);
                } else if (random3 <= 581) {
                    st.giveItems(885, 1L);
                } else if (random3 <= 582) {
                    st.giveItems(103, 1L);
                } else if (random3 <= 584) {
                    st.giveItems(917, 1L);
                } else {
                    st.giveItems(736, 1L);
                }
            } else {
                htmltext = "30835-03.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        switch (npcId) {
            case 30834:
                if (id != 2) {
                    for (final int i : AllowClass) {
                        if (st.getPlayer().getClassId().getId() == i && st.getPlayer().getLevel() >= 40) {
                            htmltext = "30834-01.htm";
                        }
                    }
                    if (!"30834-01.htm".equals(htmltext)) {
                        htmltext = "30834-07.htm";
                        st.exitCurrentQuest(true);
                    }
                } else if (st.getQuestItemsCount(4364) > 0L) {
                    htmltext = "30834-06.htm";
                } else {
                    htmltext = "30834-05.htm";
                }
                break;
            case 30835:
                htmltext = "30835-01.htm";
                break;
            case 30934:
                htmltext = "30934-01.htm";
                break;
            case 30935:
                htmltext = "30935-01.htm";
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (Rnd.chance(50)) {
            st.giveItems(4364, 1L);
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
