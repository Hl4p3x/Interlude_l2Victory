package ru.j2dev.gameserver.model.pledge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NickNameChanged;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UnitMember {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnitMember.class);
    private final int _objectId;
    private Player _player;
    private Clan _clan;
    private String _name;
    private String _title;
    private int _level;
    private int _classId;
    private int _sex;
    private int _pledgeType;
    private int _powerGrade;
    private int _apprentice;
    private int _leaderOf;

    public UnitMember(final Clan clan, final String name, final String title, final int level, final int classId, final int objectId, final int pledgeType, final int powerGrade, final int apprentice, final int sex, final int leaderOf) {
        _leaderOf = -128;
        _clan = clan;
        _objectId = objectId;
        _name = name;
        _title = title;
        _level = level;
        _classId = classId;
        _pledgeType = pledgeType;
        _powerGrade = powerGrade;
        _apprentice = apprentice;
        _sex = sex;
        _leaderOf = leaderOf;
        if (powerGrade != 0) {
            final RankPrivs r = clan.getRankPrivs(powerGrade);
            r.setParty(clan.countMembersByRank(powerGrade));
        }
    }

    public UnitMember(final Player player) {
        _leaderOf = -128;
        _objectId = player.getObjectId();
        _player = player;
    }

    public void setPlayerInstance(final Player player, final boolean exit) {
        _player = (exit ? null : player);
        if (player == null) {
            return;
        }
        _clan = player.getClan();
        _name = player.getName();
        _title = player.getTitle();
        _level = player.getLevel();
        _classId = player.getClassId().getId();
        _pledgeType = player.getPledgeType();
        _powerGrade = player.getPowerGrade();
        _apprentice = player.getApprentice();
        _sex = player.getSex();
    }

    public Player getPlayer() {
        return _player;
    }

    public boolean isOnline() {
        final Player player = getPlayer();
        return player != null && !player.isInOfflineMode();
    }

    public Clan getClan() {
        final Player player = getPlayer();
        return (player == null) ? _clan : player.getClan();
    }

    public int getClassId() {
        final Player player = getPlayer();
        return (player == null) ? _classId : player.getClassId().getId();
    }

    public int getSex() {
        final Player player = getPlayer();
        return (player == null) ? _sex : player.getSex();
    }

    public int getLevel() {
        final Player player = getPlayer();
        return (player == null) ? _level : player.getLevel();
    }

    public String getName() {
        final Player player = getPlayer();
        return (player == null) ? _name : player.getName();
    }

    public int getObjectId() {
        return _objectId;
    }

    public String getTitle() {
        final Player player = getPlayer();
        return (player == null) ? _title : player.getTitle();
    }

    public void setTitle(final String title) {
        final Player player = getPlayer();
        _title = title;
        if (player != null) {
            player.setTitle(title);
            player.broadcastPacket(new NickNameChanged(player));
        } else {
            Connection con = null;
            PreparedStatement statement = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("UPDATE characters SET title=? WHERE obj_Id=?");
                statement.setString(1, title);
                statement.setInt(2, getObjectId());
                statement.execute();
            } catch (Exception e) {
                LOGGER.error("", e);
            } finally {
                DbUtils.closeQuietly(con, statement);
            }
        }
    }

    public SubUnit getSubUnit() {
        return _clan.getSubUnit(_pledgeType);
    }

    public int getPledgeType() {
        final Player player = getPlayer();
        return (player == null) ? _pledgeType : player.getPledgeType();
    }

    public void setPledgeType(final int pledgeType) {
        final Player player = getPlayer();
        _pledgeType = pledgeType;
        if (player != null) {
            player.setPledgeType(pledgeType);
        } else {
            updatePledgeType();
        }
    }

    private void updatePledgeType() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE characters SET pledge_type=? WHERE obj_Id=?");
            statement.setInt(1, _pledgeType);
            statement.setInt(2, getObjectId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public int getPowerGrade() {
        final Player player = getPlayer();
        return (player == null) ? _powerGrade : player.getPowerGrade();
    }

    public void setPowerGrade(final int newPowerGrade) {
        final Player player = getPlayer();
        final int oldPowerGrade = getPowerGrade();
        _powerGrade = newPowerGrade;
        if (player != null) {
            player.setPowerGrade(newPowerGrade);
        } else {
            updatePowerGrade();
        }
        updatePowerGradeParty(oldPowerGrade, newPowerGrade);
    }

    private void updatePowerGradeParty(final int oldGrade, final int newGrade) {
        if (oldGrade != 0) {
            final RankPrivs r1 = getClan().getRankPrivs(oldGrade);
            r1.setParty(getClan().countMembersByRank(oldGrade));
        }
        if (newGrade != 0) {
            final RankPrivs r2 = getClan().getRankPrivs(newGrade);
            r2.setParty(getClan().countMembersByRank(newGrade));
        }
    }

    private void updatePowerGrade() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE characters SET pledge_rank=? WHERE obj_Id=?");
            statement.setInt(1, _powerGrade);
            statement.setInt(2, getObjectId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private int getApprentice() {
        final Player player = getPlayer();
        return (player == null) ? _apprentice : player.getApprentice();
    }

    public void setApprentice(final int apprentice) {
        final Player player = getPlayer();
        _apprentice = apprentice;
        if (player != null) {
            player.setApprentice(apprentice);
        } else {
            updateApprentice();
        }
    }

    private void updateApprentice() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE characters SET apprentice=? WHERE obj_Id=?");
            statement.setInt(1, _apprentice);
            statement.setInt(2, getObjectId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public String getApprenticeName() {
        if (getApprentice() != 0 && getClan().getAnyMember(getApprentice()) != null) {
            return getClan().getAnyMember(getApprentice()).getName();
        }
        return "";
    }

    public boolean hasApprentice() {
        return getApprentice() != 0;
    }

    public int getSponsor() {
        if (getPledgeType() != -1) {
            return 0;
        }
        final int id = getObjectId();
        for (final UnitMember element : getClan()) {
            if (element.getApprentice() == id) {
                return element.getObjectId();
            }
        }
        return 0;
    }

    private String getSponsorName() {
        final int sponsorId = getSponsor();
        if (sponsorId == 0) {
            return "";
        }
        if (getClan().getAnyMember(sponsorId) != null) {
            return getClan().getAnyMember(sponsorId).getName();
        }
        return "";
    }

    public boolean hasSponsor() {
        return getSponsor() != 0;
    }

    public String getRelatedName() {
        if (getPledgeType() == -1) {
            return getSponsorName();
        }
        return getApprenticeName();
    }

    public boolean isClanLeader() {
        final Player player = getPlayer();
        return (player == null) ? (_leaderOf == 0) : player.isClanLeader();
    }

    public int isSubLeader() {
        for (final SubUnit pledge : getClan().getAllSubUnits()) {
            if (pledge.getLeaderObjectId() == getObjectId()) {
                return pledge.getType();
            }
        }
        return 0;
    }

    public int getLeaderOf() {
        return _leaderOf;
    }

    public void setLeaderOf(final int leaderOf) {
        _leaderOf = leaderOf;
    }
}
