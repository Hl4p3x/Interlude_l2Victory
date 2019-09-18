package ru.j2dev.gameserver.model.entity.residence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.dao.ClanDataDAO;
import ru.j2dev.gameserver.dao.ClanHallDAO;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.manager.PlayerMessageStack;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.templates.StatsSet;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ClanHall extends Residence {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClanHall.class);

    private final int _grade;
    private final long _rentalFee;
    private final long _minBid;
    private final long _deposit;
    private int _auctionLength;
    private long _auctionMinBid;
    private String _auctionDescription = "";

    public ClanHall(final StatsSet set) {
        super(set);
        _grade = set.getInteger("grade", 0);
        _rentalFee = set.getInteger("rental_fee", 0);
        _minBid = set.getInteger("min_bid", 0);
        _deposit = set.getInteger("deposit", 0);
    }

    @Override
    public void init() {
        initZone();
        initEvent();
        loadData();
        loadFunctions();
        rewardSkills();
        if (getSiegeEvent().getClass() == ClanHallAuctionEvent.class && _owner != null && getAuctionLength() == 0) {
            startCycleTask();
        }
    }

    @Override
    public void changeOwner(final Clan clan) {
        final Clan oldOwner = getOwner();
        if (oldOwner != null && (clan == null || clan.getClanId() != oldOwner.getClanId())) {
            removeSkills();
            oldOwner.setHasHideout(0);
            cancelCycleTask();
        }
        updateOwnerInDB(clan);
        rewardSkills();
        update();
        if (clan == null && getSiegeEvent().getClass() == ClanHallAuctionEvent.class) {
            getSiegeEvent().reCalcNextTime(false);
        }
    }

    @Override
    public ResidenceType getType() {
        return ResidenceType.ClanHall;
    }

    @Override
    protected void loadData() {
        _owner = ClanDataDAO.getInstance().getOwner(this);
        ClanHallDAO.getInstance().select(this);
    }

    private void updateOwnerInDB(final Clan clan) {
        _owner = clan;
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE clan_data SET hasHideout=0 WHERE hasHideout=?");
            statement.setInt(1, getId());
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("UPDATE clan_data SET hasHideout=? WHERE clan_id=?");
            statement.setInt(1, getId());
            statement.setInt(2, getOwnerId());
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM residence_functions WHERE id=?");
            statement.setInt(1, getId());
            statement.execute();
            DbUtils.close(statement);
            if (clan != null) {
                clan.setHasHideout(getId());
                clan.broadcastClanStatus(false, true, false);
            }
        } catch (Exception e) {
            LOGGER.warn("Exception: updateOwnerInDB(L2Clan clan): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public int getGrade() {
        return _grade;
    }

    @Override
    public void update() {
        ClanHallDAO.getInstance().update(this);
    }

    public int getAuctionLength() {
        return _auctionLength;
    }

    public void setAuctionLength(final int auctionLength) {
        _auctionLength = auctionLength;
    }

    public String getAuctionDescription() {
        return _auctionDescription;
    }

    public void setAuctionDescription(final String auctionDescription) {
        _auctionDescription = ((auctionDescription == null) ? "" : auctionDescription);
    }

    public long getAuctionMinBid() {
        return _auctionMinBid;
    }

    public void setAuctionMinBid(final long auctionMinBid) {
        _auctionMinBid = auctionMinBid;
    }

    public long getRentalFee() {
        return _rentalFee;
    }

    public long getBaseMinBid() {
        return _minBid;
    }

    public long getDeposit() {
        return _deposit;
    }

    @Override
    public void chanceCycle() {
        super.chanceCycle();
        setPaidCycle(getPaidCycle() + 1);
        if (getPaidCycle() >= Config.CLNHALL_REWARD_CYCLE) {
            if (_owner.getWarehouse().getCountOf(Config.CH_BID_CURRENCY_ITEM_ID) > _rentalFee) {
                _owner.getWarehouse().destroyItemByItemId(Config.CH_BID_CURRENCY_ITEM_ID, _rentalFee);
                setPaidCycle(0);
            } else {
                final UnitMember member = _owner.getLeader();
                if (member.isOnline()) {
                    member.getPlayer().sendPacket(SystemMsg.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED);
                } else {
                    PlayerMessageStack.getInstance().mailto(member.getObjectId(), SystemMsg.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED.packet(null));
                }
                changeOwner(null);
            }
        }
    }
}
