package quests;

import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.SkillTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class _501_ProofOfClanAlliance extends Quest {
    private static final int SIR_KRISTOF_RODEMAI = 30756;
    private static final int STATUE_OF_OFFERING = 30757;
    private static final int WITCH_ATHREA = 30758;
    private static final int WITCH_KALIS = 30759;
    private static final int HERB_OF_HARIT = 3832;
    private static final int HERB_OF_VANOR = 3833;
    private static final int HERB_OF_OEL_MAHUM = 3834;
    private static final int BLOOD_OF_EVA = 3835;
    private static final int SYMBOL_OF_LOYALTY = 3837;
    private static final int PROOF_OF_ALLIANCE = 3874;
    private static final int VOUCHER_OF_FAITH = 3873;
    private static final int ANTIDOTE_RECIPE = 3872;
    private static final int POTION_OF_RECOVERY = 3889;
    private static final int[] CHESTS = {27173, 27174, 27175, 27176, 27177};
    private static final int[][] MOBS = {{20685, 3833}, {20644, 3832}, {20576, 3834}};
    private static final int RATE = 35;
    private static final int RETRY_PRICE = 10000;

    public _501_ProofOfClanAlliance() {
        super(0);
        addStartNpc(30756);
        addStartNpc(30757);
        addStartNpc(30758);
        addTalkId(30759);
        addQuestItem(3837);
        addQuestItem(3872);
        for (final int[] i : _501_ProofOfClanAlliance.MOBS) {
            addKillId(i[0]);
            addQuestItem(i[1]);
        }
        for (final int j : _501_ProofOfClanAlliance.CHESTS) {
            addKillId(j);
        }
    }

    

    public QuestState getLeader(final QuestState st) {
        final Clan clan = st.getPlayer().getClan();
        QuestState leader = null;
        if (clan != null && clan.getLeader() != null && clan.getLeader().getPlayer() != null) {
            leader = clan.getLeader().getPlayer().getQuestState(getName());
        }
        return leader;
    }

    public void removeQuestFromMembers(final QuestState st, final boolean leader) {
        removeQuestFromOfflineMembers(st);
        removeQuestFromOnlineMembers(st, leader);
    }

    public void removeQuestFromOfflineMembers(final QuestState st) {
        if (st.getPlayer() == null || st.getPlayer().getClan() == null) {
            st.exitCurrentQuest(true);
            return;
        }
        final int clan = st.getPlayer().getClan().getClanId();
        Connection con = null;
        PreparedStatement offline = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            offline = con.prepareStatement("DELETE FROM character_quests WHERE name = ? AND char_id IN (SELECT obj_id FROM characters WHERE clanId = ? AND online = 0)");
            offline.setString(1, getName());
            offline.setInt(2, clan);
            offline.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, offline);
        }
    }

    public void removeQuestFromOnlineMembers(final QuestState st, final boolean leader) {
        if (st.getPlayer() == null || st.getPlayer().getClan() == null) {
            st.exitCurrentQuest(true);
            return;
        }
        Player pleader = null;
        if (leader) {
            final QuestState l = getLeader(st);
            if (l != null) {
                pleader = l.getPlayer();
            }
        }
        if (pleader != null) {
            pleader.stopImmobilized();
            pleader.getEffectList().stopEffect(4082);
        }
        for (final Player pl : st.getPlayer().getClan().getOnlineMembers(st.getPlayer().getClan().getLeaderId())) {
            if (pl != null && pl.getQuestState(getName()) != null) {
                pl.getQuestState(getName()).exitCurrentQuest(true);
            }
        }
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if (st.getPlayer() == null || st.getPlayer().getClan() == null) {
            st.exitCurrentQuest(true);
            return "noquest";
        }
        final QuestState leader = getLeader(st);
        if (leader == null) {
            removeQuestFromMembers(st, true);
            return "Quest Failed";
        }
        String htmltext = event;
        if (st.getPlayer().isClanLeader()) {
            if ("30756-03.htm".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
            } else if ("30759-03.htm".equalsIgnoreCase(event)) {
                st.setCond(2);
                st.set("dead_list", " ");
            } else if ("30759-07.htm".equalsIgnoreCase(event)) {
                st.takeItems(3837, -1L);
                st.giveItems(3872, 1L);
                st.addNotifyOfDeath(st.getPlayer(), false);
                st.setCond(3);
                st.set("chest_count", "0");
                st.set("chest_game", "0");
                st.set("chest_try", "0");
                st.startQuestTimer("poison_timer", 3600000L);
                st.getPlayer().altUseSkill(SkillTable.getInstance().getInfo(4082, 1), st.getPlayer());
                st.getPlayer().startImmobilized();
                htmltext = "30759-07.htm";
            }
        }
        if ("poison_timer".equalsIgnoreCase(event)) {
            removeQuestFromMembers(st, true);
            htmltext = "30759-09.htm";
        } else if ("chest_timer".equalsIgnoreCase(event)) {
            htmltext = "";
            if (leader.getInt("chest_game") < 2) {
                stop_chest_game(st);
            }
        } else if ("30757-04.htm".equalsIgnoreCase(event)) {
            final List<String> deadlist = new ArrayList<>(Arrays.asList(leader.get("dead_list").split(" ")));
            deadlist.add(st.getPlayer().getName());
            StringBuilder deadstr = new StringBuilder();
            for (final String s : deadlist) {
                deadstr.append(s).append(" ");
            }
            leader.set("dead_list", deadstr.toString());
            st.addNotifyOfDeath(leader.getPlayer(), false);
            if (Rnd.chance(50)) {
                st.getPlayer().reduceCurrentHp(st.getPlayer().getCurrentHp() * 8.0, st.getPlayer(), null, true, true, false, false, false, false, false);
            }
            st.giveItems(3837, 1L);
            st.playSound("ItemSound.quest_accept");
        } else if ("30757-05.htm".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
        } else if ("30758-03.htm".equalsIgnoreCase(event)) {
            start_chest_game(st);
        } else if ("30758-07.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(57) < 10000L) {
                htmltext = "30758-06.htm";
            } else {
                st.takeItems(57, 10000L);
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (st.getPlayer() == null || st.getPlayer().getClan() == null) {
            st.exitCurrentQuest(true);
            return htmltext;
        }
        final QuestState leader = getLeader(st);
        if (leader == null) {
            removeQuestFromMembers(st, true);
            return "Quest Failed";
        }
        final int npcId = npc.getNpcId();
        switch (npcId) {
            case 30756:
                if (!st.getPlayer().isClanLeader()) {
                    st.exitCurrentQuest(true);
                    return "30756-10.htm";
                }
                if (st.getPlayer().getClan().getLevel() <= 2) {
                    st.exitCurrentQuest(true);
                    return "30756-08.htm";
                }
                if (st.getPlayer().getClan().getLevel() >= 4) {
                    st.exitCurrentQuest(true);
                    return "30756-09.htm";
                }
                if (st.getQuestItemsCount(3873) > 0L) {
                    st.playSound("ItemSound.quest_fanfare_2");
                    st.takeItems(3873, -1L);
                    st.giveItems(3874, 1L);
                    st.addExpAndSp(0L, 120000L);
                    htmltext = "30756-07.htm";
                    st.exitCurrentQuest(true);
                } else {
                    if (cond == 1 || cond == 2) {
                        return "30756-06.htm";
                    }
                    if (st.getQuestItemsCount(3874) == 0L) {
                        st.setCond(0);
                        return "30756-01.htm";
                    }
                    st.exitCurrentQuest(true);
                    return htmltext;
                }
                break;
            case 30759:
                if (st.getPlayer().isClanLeader()) {
                    if (cond == 1) {
                        return "30759-01.htm";
                    }
                    if (cond == 2) {
                        htmltext = "30759-05.htm";
                        if (st.getQuestItemsCount(3837) == 3L) {
                            int deads = 0;
                            try {
                                deads = st.get("dead_list").split(" ").length;
                            } finally {
                                if (deads == 3) {
                                    htmltext = "30759-06.htm";
                                }
                            }
                        }
                    } else if (cond == 3) {
                        if (st.getQuestItemsCount(3832) > 0L && st.getQuestItemsCount(3833) > 0L && st.getQuestItemsCount(3834) > 0L && st.getQuestItemsCount(3835) > 0L && st.getQuestItemsCount(3872) > 0L) {
                            st.takeItems(3872, 1L);
                            st.takeItems(3832, 1L);
                            st.takeItems(3833, 1L);
                            st.takeItems(3834, 1L);
                            st.takeItems(3835, 1L);
                            st.giveItems(3889, 1L);
                            st.giveItems(3873, 1L);
                            st.cancelQuestTimer("poison_timer");
                            removeQuestFromMembers(st, false);
                            st.getPlayer().stopImmobilized();
                            st.getPlayer().getEffectList().stopEffect(4082);
                            st.setCond(4);
                            st.playSound("ItemSound.quest_finish");
                            return "30759-08.htm";
                        }
                        if (st.getQuestItemsCount(3873) == 0L) {
                            return "30759-10.htm";
                        }
                    }
                } else if (leader.getCond() == 3) {
                    return "30759-11.htm";
                }
                break;
            case 30757: {
                if (st.getPlayer().isClanLeader()) {
                    return "30757-03.htm";
                }
                if (st.getPlayer().getLevel() <= 39) {
                    st.exitCurrentQuest(true);
                    return "30757-02.htm";
                }
                String[] dlist;
                int deads2;
                try {
                    dlist = leader.get("dead_list").split(" ");
                    deads2 = dlist.length;
                } catch (Exception e) {
                    removeQuestFromMembers(st, true);
                    return "Who are you?";
                }
                if (deads2 < 3) {
                    for (final String str : dlist) {
                        if (st.getPlayer().getName().equalsIgnoreCase(str)) {
                            return "you cannot die again!";
                        }
                    }
                    return "30757-01.htm";
                }
                break;
            }
            case 30758: {
                if (st.getPlayer().isClanLeader()) {
                    return "30757-03.htm";
                }
                String[] dlist;
                try {
                    dlist = leader.get("dead_list").split(" ");
                } catch (Exception e2) {
                    st.exitCurrentQuest(true);
                    return "Who are you?";
                }
                Boolean flag = false;
                if (dlist != null) {
                    for (final String str : dlist) {
                        if (st.getPlayer().getName().equalsIgnoreCase(str)) {
                            flag = true;
                        }
                    }
                }
                if (!flag) {
                    st.exitCurrentQuest(true);
                    return "Who are you?";
                }
                final int game_state = leader.getInt("chest_game");
                if (game_state == 0) {
                    if (leader.getInt("chest_try") == 0) {
                        return "30758-01.htm";
                    }
                    return "30758-05.htm";
                } else {
                    if (game_state == 1) {
                        return "30758-09.htm";
                    }
                    if (game_state == 2) {
                        st.playSound("ItemSound.quest_finish");
                        st.giveItems(3835, 1L);
                        st.cancelQuestTimer("chest_timer");
                        stop_chest_game(st);
                        leader.set("chest_game", "3");
                        return "30758-08.htm";
                    }
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getPlayer() == null || st.getPlayer().getClan() == null) {
            st.exitCurrentQuest(true);
            return "noquest";
        }
        final QuestState leader = getLeader(st);
        if (leader == null) {
            removeQuestFromMembers(st, true);
            return "Quest Failed";
        }
        final int npcId = npc.getNpcId();
        if (!leader.isRunningQuestTimer("poison_timer")) {
            stop_chest_game(st);
            return "Quest Failed";
        }
        for (final int[] m : _501_ProofOfClanAlliance.MOBS) {
            if (npcId == m[0] && st.getInt(String.valueOf(m[1])) == 0 && Rnd.chance(35)) {
                st.giveItems(m[1], 1L);
                leader.set(String.valueOf(m[1]), "1");
                st.playSound("ItemSound.quest_middle");
                return null;
            }
        }
        final int[] chests = _501_ProofOfClanAlliance.CHESTS;
        final int length2 = chests.length;
        int k = 0;
        while (k < length2) {
            final int i = chests[k];
            if (npcId == i) {
                if (!leader.isRunningQuestTimer("chest_timer")) {
                    stop_chest_game(st);
                    return "Time is up!";
                }
                if (Rnd.chance(25)) {
                    Functions.npcSay(npc, "###### BINGO! ######");
                    int count = leader.getInt("chest_count");
                    if (count < 4) {
                        ++count;
                        leader.set("chest_count", String.valueOf(count));
                    }
                    if (count >= 4) {
                        stop_chest_game(st);
                        leader.set("chest_game", "2");
                        leader.cancelQuestTimer("chest_timer");
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
                return null;
            } else {
                ++k;
            }
        }
        return null;
    }

    public void start_chest_game(final QuestState st) {
        if (st.getPlayer() == null || st.getPlayer().getClan() == null) {
            st.exitCurrentQuest(true);
            return;
        }
        final QuestState leader = getLeader(st);
        if (leader == null) {
            removeQuestFromMembers(st, true);
            return;
        }
        leader.set("chest_game", "1");
        leader.set("chest_count", "0");
        final int attempts = leader.getInt("chest_try");
        leader.set("chest_try", String.valueOf(attempts + 1));
        GameObjectsStorage.getNpcs(npc -> ArrayUtils.contains(CHESTS, npc.getNpcId())).forEach(NpcInstance::deleteMe);
        for (int n = 1; n <= 5; ++n) {
            for (final int i : _501_ProofOfClanAlliance.CHESTS) {
                leader.addSpawn(i, 102100, 103450, -3400, 0, 100, 60000);
            }
        }
        leader.startQuestTimer("chest_timer", 60000L);
    }

    public void stop_chest_game(final QuestState st) {
        final QuestState leader = getLeader(st);
        if (leader == null) {
            removeQuestFromMembers(st, true);
            return;
        }
        GameObjectsStorage.getNpcs(npc -> ArrayUtils.contains(CHESTS, npc.getNpcId())).forEach(NpcInstance::deleteMe);
        leader.set("chest_game", "0");
    }

    @Override
    public String onDeath(final Creature npc, final Creature pc, final QuestState st) {
        if (st.getPlayer() == null || st.getPlayer().getClan() == null) {
            st.exitCurrentQuest(true);
            return null;
        }
        final QuestState leader = getLeader(st);
        if (leader == null) {
            removeQuestFromMembers(st, true);
            return null;
        }
        if (st.getPlayer() == pc) {
            leader.cancelQuestTimer("poison_timer");
            leader.cancelQuestTimer("chest_timer");
            removeQuestFromMembers(st, true);
        }
        return null;
    }
}
