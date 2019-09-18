package quests;

import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class _503_PursuitClanAmbition extends Quest {
    private final int G_Let_Martien = 3866;
    private final int Th_Wyrm_Eggs = 3842;
    private final int Drake_Eggs = 3841;
    private final int Bl_Wyrm_Eggs = 3840;
    private final int Mi_Drake_Eggs = 3839;
    private final int Brooch = 3843;
    private final int Bl_Anvil_Coin = 3871;
    private final short G_Let_Balthazar = 3867;
    private final short Recipe_Power_Stone = 3838;
    private final short Power_Stones = 3846;
    private final short Nebulite_Crystals = 3844;
    private final short Broke_Power_Stone = 3845;
    private final int G_Let_Rodemai = 3868;
    private final int Imp_Keys = 3847;
    private final int Scepter_Judgement = 3869;
    private final int Proof_Aspiration = 3870;
    private final int[] EggList;
    private final int Gustaf = 30760;
    private final int Martien = 30645;
    private final int Athrea = 30758;
    private final int Kalis = 30759;
    private final int Fritz = 30761;
    private final int Lutz = 30762;
    private final int Kurtz = 30763;
    private final int Kusto = 30512;
    private final int Balthazar = 30764;
    private final int Rodemai = 30868;
    private final int Coffer = 30765;
    private final int Cleo = 30766;
    private final int ThunderWyrm1 = 20282;
    private final int ThunderWyrm2 = 20243;
    private final int Drake1 = 20137;
    private final int Drake2 = 20285;
    private final int BlitzWyrm = 27178;
    private final int GraveGuard = 20668;
    private final int GraveKeymaster = 27179;
    private final int ImperialGravekeeper = 27181;
    private final int GiantSoldier = 20654;
    private final int GiantScout = 20656;

    public _503_PursuitClanAmbition() {
        super(2);
        EggList = new int[]{3839, 3840, 3841, 3842};
        addStartNpc(30760);
        addTalkId(30645);
        addTalkId(30758);
        addTalkId(30759);
        addTalkId(30761);
        addTalkId(30762);
        addTalkId(30763);
        addTalkId(30512);
        addTalkId(30764);
        addTalkId(30868);
        addTalkId(30765);
        addTalkId(30766);
        addKillId(20282, 20243, 20137, 20285, 27178, 20654, 20656, 20668, 27179, 27181);
        addAttackId(27181);
        for (int i = 3839; i <= 3848; ++i) {
            addQuestItem(i);
        }
        for (int i = 3866; i <= 3869; ++i) {
            addQuestItem(i);
        }
    }

    

    public void suscribe_members(final QuestState st) {
        final int clan = st.getPlayer().getClan().getClanId();
        Connection con = null;
        PreparedStatement offline = null;
        PreparedStatement insertion = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            offline = con.prepareStatement("SELECT obj_Id FROM characters WHERE clanid=? AND online=0");
            insertion = con.prepareStatement("REPLACE INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)");
            offline.setInt(1, clan);
            rs = offline.executeQuery();
            while (rs.next()) {
                final int char_id = rs.getInt("obj_Id");
                try {
                    insertion.setInt(1, char_id);
                    insertion.setString(2, getName());
                    insertion.setString(3, "<state>");
                    insertion.setString(4, "Started");
                    insertion.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        } finally {
            DbUtils.closeQuietly(insertion);
            DbUtils.closeQuietly(con, offline, rs);
        }
    }

    public void offlineMemberExit(final QuestState st) {
        final int clan = st.getPlayer().getClan().getClanId();
        Connection con = null;
        PreparedStatement offline = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            offline = con.prepareStatement("DELETE FROM character_quests WHERE name=? AND char_id IN (SELECT obj_id FROM characters WHERE clanId=? AND online=0)");
            offline.setString(1, getName());
            offline.setInt(2, clan);
            offline.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, offline);
        }
    }

    public Player getLeader(final QuestState st) {
        final Player player = st.getPlayer();
        if (player == null) {
            return null;
        }
        final Clan clan = player.getClan();
        if (clan == null) {
            return null;
        }
        return clan.getLeader().getPlayer();
    }

    public int getLeaderVar(final QuestState st, final String var) {
        final boolean cond = "cond".equalsIgnoreCase(var);
        try {
            final Player leader = getLeader(st);
            if (leader != null) {
                if (cond) {
                    return leader.getQuestState(getName()).getCond();
                }
                return leader.getQuestState(getName()).getInt(var);
            }
        } catch (Exception e2) {
            return -1;
        }
        final Clan clan = st.getPlayer().getClan();
        if (clan == null) {
            return -1;
        }
        final int leaderId = clan.getLeaderId();
        Connection con = null;
        PreparedStatement offline = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            offline = con.prepareStatement("SELECT value FROM character_quests WHERE char_id=? AND var=? AND name=?");
            offline.setInt(1, leaderId);
            offline.setString(2, var);
            offline.setString(3, getName());
            int val = -1;
            rs = offline.executeQuery();
            if (rs.next()) {
                val = rs.getInt("value");
                if (cond && (val & Integer.MIN_VALUE) != 0x0) {
                    val &= Integer.MAX_VALUE;
                    for (int i = 1; i < 32; ++i) {
                        val >>= 1;
                        if (val == 0) {
                            return i;
                        }
                    }
                }
            }
            return val;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            DbUtils.closeQuietly(con, offline, rs);
        }
    }

    public void setLeaderVar(final QuestState st, final String var, final String value) {
        final Clan clan = st.getPlayer().getClan();
        if (clan == null) {
            return;
        }
        final Player leader = clan.getLeader().getPlayer();
        if (leader != null) {
            if ("cond".equalsIgnoreCase(var)) {
                leader.getQuestState(getName()).setCond(Integer.parseInt(value));
            } else {
                leader.getQuestState(getName()).set(var, value);
            }
        } else {
            final int leaderId = st.getPlayer().getClan().getLeaderId();
            Connection con = null;
            PreparedStatement offline = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                offline = con.prepareStatement("UPDATE character_quests SET value=? WHERE char_id=? AND var=? AND name=?");
                offline.setString(1, value);
                offline.setInt(2, leaderId);
                offline.setString(3, var);
                offline.setString(4, getName());
                offline.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DbUtils.closeQuietly(con, offline);
            }
        }
    }

    public boolean checkEggs(final QuestState st) {
        int count = (int) Arrays.stream(EggList).filter(item -> st.getQuestItemsCount(item) > 9L).count();
        return count > 3;
    }

    public void giveItem(final int item, final long maxcount, final QuestState st) {
        final Player player = st.getPlayer();
        if (player == null) {
            return;
        }
        final Player leader = getLeader(st);
        if (leader == null) {
            return;
        }
        if (player.getDistance(leader) > Config.ALT_PARTY_DISTRIBUTION_RANGE) {
            return;
        }
        final QuestState qs = leader.getQuestState(getClass());
        if (qs == null) {
            return;
        }
        final long count = qs.getQuestItemsCount(item);
        if (count < maxcount) {
            qs.giveItems(item, 1L);
            if (count == maxcount - 1L) {
                qs.playSound("ItemSound.quest_middle");
            } else {
                qs.playSound("ItemSound.quest_itemget");
            }
        }
    }

    public String exit503(final boolean completed, final QuestState st) {
        if (completed) {
            st.giveItems(3870, 1L);
            st.addExpAndSp(0L, 250000L);
            st.unset("cond");
            st.unset("Fritz");
            st.unset("Lutz");
            st.unset("Kurtz");
            st.unset("ImpGraveKeeper");
            st.exitCurrentQuest(false);
        } else {
            st.exitCurrentQuest(true);
        }
        st.takeItems(3869, -1L);
        try {
            final List<Player> members = st.getPlayer().getClan().getOnlineMembers(0);
            members.stream().filter(Objects::nonNull).map(player -> player.getQuestState(getName())).filter(Objects::nonNull).forEach(qs -> qs.exitCurrentQuest(true));
            offlineMemberExit(st);
        } catch (Exception e) {
            return "You dont have any members in your Clan, so you can't finish the Pursuit of Aspiration";
        }
        return "Congratulations, you have finished the Pursuit of Clan Ambition";
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30760-08.htm".equalsIgnoreCase(event)) {
            st.giveItems(3866, 1L);
            st.setCond(1);
            st.set("Fritz", "1");
            st.set("Lutz", "1");
            st.set("Kurtz", "1");
            st.set("ImpGraveKeeper", "1");
            st.setState(2);
        } else if ("30760-12.htm".equalsIgnoreCase(event)) {
            st.giveItems(3867, 1L);
            st.setCond(4);
        } else if ("30760-16.htm".equalsIgnoreCase(event)) {
            st.giveItems(3868, 1L);
            st.setCond(7);
        } else if ("30760-20.htm".equalsIgnoreCase(event)) {
            exit503(true, st);
        } else if ("30760-22.htm".equalsIgnoreCase(event)) {
            st.setCond(13);
        } else if ("30760-23.htm".equalsIgnoreCase(event)) {
            exit503(true, st);
        } else if ("30645-03.htm".equalsIgnoreCase(event)) {
            st.takeItems(3866, -1L);
            st.setCond(2);
            suscribe_members(st);
            final List<Player> members = st.getPlayer().getClan().getOnlineMembers(st.getPlayer().getObjectId());
            for (final Player player : members) {
                newQuestState(player, 2);
            }
        } else if ("30763-03.htm".equalsIgnoreCase(event)) {
            if (st.getInt("Kurtz") == 1) {
                htmltext = "30763-02.htm";
                st.giveItems(3839, 6L);
                st.giveItems(3843, 1L);
                st.set("Kurtz", "2");
            }
        } else if ("30762-03.htm".equalsIgnoreCase(event)) {
            final int lutz = st.getInt("Lutz");
            if (lutz == 1) {
                htmltext = "30762-02.htm";
                st.giveItems(3839, 4L);
                st.giveItems(3840, 3L);
                st.set("Lutz", "2");
            }
            st.addSpawn(27178, npc.getLoc().x, npc.getLoc().y, npc.getLoc().z, Location.getRandomHeading(), 300, 120000);
            st.addSpawn(27178, npc.getLoc().x, npc.getLoc().y, npc.getLoc().z, Location.getRandomHeading(), 300, 120000);
        } else if ("30761-03.htm".equalsIgnoreCase(event)) {
            final int fritz = st.getInt("Fritz");
            if (fritz == 1) {
                htmltext = "30761-02.htm";
                st.giveItems(3840, 3L);
                st.set("Fritz", "2");
            }
            st.addSpawn(27178, npc.getLoc().x, npc.getLoc().y, npc.getLoc().z, Location.getRandomHeading(), 300, 120000);
            st.addSpawn(27178, npc.getLoc().x, npc.getLoc().y, npc.getLoc().z, Location.getRandomHeading(), 300, 120000);
        } else if ("30512-03.htm".equalsIgnoreCase(event)) {
            st.takeItems(3843, -1L);
            st.giveItems(3871, 1L);
            st.set("Kurtz", "3");
        } else if ("30764-03.htm".equalsIgnoreCase(event)) {
            st.takeItems(3867, -1L);
            st.setCond(5);
            st.set("Kurtz", "3");
        } else if ("30764-05.htm".equalsIgnoreCase(event)) {
            st.takeItems(3867, -1L);
            st.setCond(5);
        } else if ("30764-06.htm".equalsIgnoreCase(event)) {
            st.takeItems(3871, -1L);
            st.set("Kurtz", "4");
            st.giveItems(3838, 1L);
        } else if ("30868-04.htm".equalsIgnoreCase(event)) {
            st.takeItems(3868, -1L);
            st.setCond(8);
        } else if ("30868-06a.htm".equalsIgnoreCase(event)) {
            st.setCond(10);
        } else if ("30868-10.htm".equalsIgnoreCase(event)) {
            st.setCond(12);
        } else if ("30766-04.htm".equalsIgnoreCase(event)) {
            st.setCond(9);
            NpcInstance n = st.findTemplate(30766);
            if (n != null) {
                Functions.npcSay(n, "Blood and Honour");
            }
            n = st.findTemplate(30759);
            if (n != null) {
                Functions.npcSay(n, "Ambition and Power");
            }
            n = st.findTemplate(30758);
            if (n != null) {
                Functions.npcSay(n, "War and Death");
            }
        } else if ("30766-08.htm".equalsIgnoreCase(event)) {
            st.takeItems(3869, -1L);
            exit503(false, st);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        String htmltext = "noquest";
        final boolean isLeader = st.getPlayer().isClanLeader();
        if (id == 1 && npcId == 30760) {
            if (st.getPlayer().getClan() != null) {
                if (isLeader) {
                    final int clanLevel = st.getPlayer().getClan().getLevel();
                    if (st.getQuestItemsCount(3870) > 0L) {
                        htmltext = "30760-03.htm";
                        st.exitCurrentQuest(true);
                    } else if (clanLevel > 3) {
                        htmltext = "30760-04.htm";
                    } else {
                        htmltext = "30760-02.htm";
                        st.exitCurrentQuest(true);
                    }
                } else {
                    htmltext = "30760-04t.htm";
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "30760-01.htm";
                st.exitCurrentQuest(true);
            }
            return htmltext;
        }
        if (st.getPlayer().getClan() != null && st.getPlayer().getClan().getLevel() == 5) {
            return "completed";
        }
        if (isLeader) {
            if (st.getCond() == 0) {
                st.setCond(1);
            }
            if (st.get("Kurtz") == null) {
                st.set("Kurtz", "1");
            }
            if (st.get("Lutz") == null) {
                st.set("Lutz", "1");
            }
            if (st.get("Fritz") == null) {
                st.set("Fritz", "1");
            }
            final int cond = st.getCond();
            final int kurtz = st.getInt("Kurtz");
            final int lutz = st.getInt("Lutz");
            final int fritz = st.getInt("Fritz");
            if (npcId == 30760) {
                switch (cond) {
                    case 1:
                        htmltext = "30760-09.htm";
                        break;
                    case 2:
                        htmltext = "30760-10.htm";
                        break;
                    case 3:
                        htmltext = "30760-11.htm";
                        break;
                    case 4:
                        htmltext = "30760-13.htm";
                        break;
                    case 5:
                        htmltext = "30760-14.htm";
                        break;
                    case 6:
                        htmltext = "30760-15.htm";
                        break;
                    case 7:
                        htmltext = "30760-17.htm";
                        break;
                    case 12:
                        htmltext = "30760-19.htm";
                        break;
                    case 13:
                        htmltext = "30760-24.htm";
                        break;
                    default:
                        htmltext = "30760-18.htm";
                        break;
                }
            } else if (npcId == 30645) {
                switch (cond) {
                    case 1:
                        htmltext = "30645-02.htm";
                        break;
                    case 2:
                        if (checkEggs(st) && kurtz > 1 && lutz > 1 && fritz > 1) {
                            htmltext = "30645-05.htm";
                            st.setCond(3);
                            for (final int item : EggList) {
                                st.takeItems(item, -1L);
                            }
                        } else {
                            htmltext = "30645-04.htm";
                        }
                        break;
                    case 3:
                        htmltext = "30645-07.htm";
                        break;
                    default:
                        htmltext = "30645-08.htm";
                        break;
                }
            } else if (npcId == 30762 && cond == 2) {
                htmltext = "30762-01.htm";
            } else if (npcId == 30763 && cond == 2) {
                htmltext = "30763-01.htm";
            } else if (npcId == 30761 && cond == 2) {
                htmltext = "30761-01.htm";
            } else if (npcId == 30512) {
                switch (kurtz) {
                    case 1:
                        htmltext = "30512-01.htm";
                        break;
                    case 2:
                        htmltext = "30512-02.htm";
                        break;
                    default:
                        htmltext = "30512-04.htm";
                        break;
                }
            } else if (npcId == 30764) {
                switch (cond) {
                    case 4:
                        if (kurtz > 2) {
                            htmltext = "30764-04.htm";
                        } else {
                            htmltext = "30764-02.htm";
                        }
                        break;
                    case 5:
                        if (st.getQuestItemsCount(3846) > 9L && st.getQuestItemsCount(3844) > 9L) {
                            htmltext = "30764-08.htm";
                            st.takeItems(3846, -1L);
                            st.takeItems(3844, -1L);
                            st.takeItems(3843, -1L);
                            st.setCond(6);
                        } else {
                            htmltext = "30764-07.htm";
                        }
                        break;
                    case 6:
                        htmltext = "30764-09.htm";
                        break;
                }
            } else if (npcId == 30868) {
                switch (cond) {
                    case 7:
                        htmltext = "30868-02.htm";
                        break;
                    case 8:
                        htmltext = "30868-05.htm";
                        break;
                    case 9:
                        htmltext = "30868-06.htm";
                        break;
                    case 10:
                        htmltext = "30868-08.htm";
                        break;
                    case 11:
                        htmltext = "30868-09.htm";
                        break;
                    case 12:
                        htmltext = "30868-11.htm";
                        break;
                }
            } else if (npcId == 30766) {
                switch (cond) {
                    case 8:
                        htmltext = "30766-02.htm";
                        break;
                    case 9:
                        htmltext = "30766-05.htm";
                        break;
                    case 10:
                        htmltext = "30766-06.htm";
                        break;
                    case 11:
                    case 12:
                    case 13:
                        htmltext = "30766-07.htm";
                        break;
                }
            } else if (npcId == 30765) {
                if (st.getCond() == 10) {
                    if (st.getQuestItemsCount(3847) < 6L) {
                        htmltext = "30765-03a.htm";
                    } else if (st.getInt("ImpGraveKeeper") == 3) {
                        htmltext = "30765-02.htm";
                        st.setCond(11);
                        st.takeItems(3847, 6L);
                        st.giveItems(3869, 1L);
                    } else {
                        htmltext = "<html><head><body>(You and your Clan didn't kill the Imperial Gravekeeper by your own, do it try again.)</body></html>";
                    }
                } else {
                    htmltext = "<html><head><body>(You already have the Scepter of Judgement.)</body></html>";
                }
            } else if (npcId == 30759) {
                htmltext = "30759-01.htm";
            } else if (npcId == 30758) {
                htmltext = "30758-01.htm";
            }
            return htmltext;
        }
        final int cond = getLeaderVar(st, "cond");
        if (npcId == 30645 && (cond == 1 || cond == 2 || cond == 3)) {
            htmltext = "30645-01.htm";
        } else if (npcId == 30868) {
            if (cond == 9 || cond == 10) {
                htmltext = "30868-07.htm";
            } else if (cond == 7) {
                htmltext = "30868-01.htm";
            }
        } else if (npcId == 30764 && cond == 4) {
            htmltext = "30764-01.htm";
        } else if (npcId == 30766 && cond == 8) {
            htmltext = "30766-01.htm";
        } else if (npcId == 30512 && cond > 2 && cond < 6) {
            htmltext = "30512-01a.htm";
        } else if (npcId == 30765 && cond == 10) {
            htmltext = "30765-01.htm";
        } else if (npcId == 30760) {
            switch (cond) {
                case 3:
                    htmltext = "30760-11t.htm";
                    break;
                case 4:
                    htmltext = "30760-15t.htm";
                    break;
                case 12:
                    htmltext = "30760-19t.htm";
                    break;
                case 13:
                    htmltext = "30766-24t.htm";
                    break;
            }
        }
        return htmltext;
    }

    @Override
    public String onAttack(final NpcInstance npc, final QuestState st) {
        if (npc.getMaxHp() / 2 > npc.getCurrentHp() && Rnd.chance(4)) {
            final int ImpGraveKepperStat = getLeaderVar(st, "ImpGraveKeeper");
            if (ImpGraveKepperStat == 1) {
                for (int i = 1; i <= 4; ++i) {
                    st.addSpawn(27180, 120000);
                }
                setLeaderVar(st, "ImpGraveKeeper", "2");
            } else {
                final List<Player> players = World.getAroundPlayers(npc, 900, 200);
                if (players.size() > 0) {
                    final Player player = players.get(Rnd.get(players.size()));
                    if (player != null) {
                        player.teleToLocation(185462, 20342, -3250);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = getLeaderVar(st, "cond");
        switch (cond) {
            case 2: {
                switch (npcId) {
                    case 20282: {
                        if (Rnd.chance(20)) {
                            giveItem(3842, 10L, st);
                            break;
                        }
                        break;
                    }
                    case 20243: {
                        if (Rnd.chance(15)) {
                            giveItem(3842, 10L, st);
                            break;
                        }
                        break;
                    }
                    case 20137: {
                        if (Rnd.chance(20)) {
                            giveItem(3841, 10L, st);
                            break;
                        }
                        break;
                    }
                    case 20285: {
                        if (Rnd.chance(25)) {
                            giveItem(3841, 10L, st);
                            break;
                        }
                        break;
                    }
                    case 27178: {
                        giveItem(3840, 10L, st);
                        break;
                    }
                }
                break;
            }
            case 5: {
                int chance = 0;
                switch (npcId) {
                    case 20654: {
                        chance = 25;
                        break;
                    }
                    case 20656: {
                        chance = 35;
                        break;
                    }
                }
                if (chance > 0 && Rnd.chance(chance)) {
                    switch (Rnd.get(3)) {
                        case 0: {
                            if (getLeaderVar(st, "Kurtz") < 4) {
                                return null;
                            }
                            giveItem(3845, 40L, st);
                            break;
                        }
                        case 1: {
                            giveItem(3846, 10L, st);
                            break;
                        }
                        case 2: {
                            giveItem(3844, 10L, st);
                            break;
                        }
                    }
                }
                break;
            }
            case 10: {
                switch (npcId) {
                    case 20668: {
                        if (Rnd.chance(15)) {
                            st.addSpawn(27179, 120000);
                            break;
                        }
                        break;
                    }
                    case 27179: {
                        if (Rnd.chance(80)) {
                            giveItem(3847, 6L, st);
                            break;
                        }
                        break;
                    }
                    case 27181: {
                        final NpcInstance spawnedNpc = st.addSpawn(30765, 120000);
                        Functions.npcSay(spawnedNpc, "Curse of the gods on the one that defiles the property of the empire!");
                        setLeaderVar(st, "ImpGraveKeeper", "3");
                        break;
                    }
                }
                break;
            }
        }
        return null;
    }
}
