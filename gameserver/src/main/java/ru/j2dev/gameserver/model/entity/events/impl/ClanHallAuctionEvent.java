package ru.j2dev.gameserver.model.entity.events.impl;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.dao.SiegeClanDAO;
import ru.j2dev.gameserver.manager.PlayerMessageStack;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.actions.StartStopAction;
import ru.j2dev.gameserver.model.entity.events.objects.AuctionSiegeClanObject;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject.SiegeClanComparatorImpl;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.tables.ClanTable;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ClanHallAuctionEvent extends SiegeEvent<ClanHall, AuctionSiegeClanObject> {
    private final Calendar _endSiegeDate;

    public ClanHallAuctionEvent(final MultiValueSet<String> set) {
        super(set);
        _endSiegeDate = Calendar.getInstance();
    }

    @Override
    public void reCalcNextTime(final boolean onStart) {
        clearActions();
        _onTimeActions.clear();
        final Clan owner = (this).getResidence().getOwner();
        _endSiegeDate.setTimeInMillis(0L);
        if (getResidence().getAuctionLength() == 0 && owner == null) {
            getResidence().getSiegeDate().setTimeInMillis(System.currentTimeMillis());
            getResidence().getSiegeDate().set(Calendar.DAY_OF_WEEK, 2);
            getResidence().getSiegeDate().set(Calendar.HOUR_OF_DAY, 15);
            getResidence().getSiegeDate().set(Calendar.MINUTE, 0);
            getResidence().getSiegeDate().set(Calendar.SECOND, 0);
            getResidence().getSiegeDate().set(Calendar.MILLISECOND, 0);
            getResidence().setAuctionLength(Config.CLNHALL_REWARD_CYCLE / 24);
            getResidence().setAuctionMinBid(getResidence().getBaseMinBid());
            getResidence().setJdbcState(JdbcEntityState.UPDATED);
            getResidence().update();
            _onTimeActions.clear();
            addOnTimeAction(0, new StartStopAction(EVENT, true));
            addOnTimeAction(getResidence().getAuctionLength() * 86400, new StartStopAction(EVENT, false));
            _endSiegeDate.setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis() + getResidence().getAuctionLength() * 86400000L);
            registerActions();
        } else if (getResidence().getAuctionLength() != 0 || owner == null) {
            final long endDate = getResidence().getSiegeDate().getTimeInMillis() + getResidence().getAuctionLength() * 86400000L;
            if (endDate <= System.currentTimeMillis()) {
                getResidence().getSiegeDate().setTimeInMillis(System.currentTimeMillis() + 60000L);
                _endSiegeDate.setTimeInMillis(System.currentTimeMillis() + 60000L);
                _onTimeActions.clear();
                addOnTimeAction(0, new StartStopAction(EVENT, true));
                addOnTimeAction(1, new StartStopAction(EVENT, false));
                registerActions();
            } else {
                _endSiegeDate.setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis() + getResidence().getAuctionLength() * 86400000L);
                _onTimeActions.clear();
                addOnTimeAction(0, new StartStopAction(EVENT, true));
                addOnTimeAction((int) getEndSiegeForCH(), new StartStopAction(EVENT, false));
                registerActions();
            }
        }
    }

    @Override
    public void stopEvent(final boolean step) {
        final List<AuctionSiegeClanObject> siegeClanObjects = removeObjects(ATTACKERS);
        final AuctionSiegeClanObject[] clans = siegeClanObjects.toArray(new AuctionSiegeClanObject[0]);
        Arrays.sort(clans, SiegeClanComparatorImpl.getInstance());
        final Clan oldOwner = getResidence().getOwner();
        final AuctionSiegeClanObject winnerSiegeClan = (clans.length > 0) ? clans[0] : null;
        if (winnerSiegeClan != null) {
            final SystemMessage2 msg = new SystemMessage2(SystemMsg.THE_CLAN_HALL_WHICH_WAS_PUT_UP_FOR_AUCTION_HAS_BEEN_AWARDED_TO_S1_CLAN).addString(winnerSiegeClan.getClan().getName());
            for (final AuctionSiegeClanObject $siegeClan : siegeClanObjects) {
                try {
                    final Player player = $siegeClan.getClan().getLeader().getPlayer();
                    if (player != null) {
                        player.sendPacket(msg);
                    } else {
                        PlayerMessageStack.getInstance().mailto($siegeClan.getClan().getLeaderId(), msg);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if ($siegeClan != winnerSiegeClan) {
                    final long returnBid = $siegeClan.getParam() - (long) ($siegeClan.getParam() * 0.1);
                    $siegeClan.getClan().getWarehouse().addItem(Config.CH_BID_CURRENCY_ITEM_ID, returnBid);
                }
            }
            SiegeClanDAO.getInstance().delete(getResidence());
            if (oldOwner != null) {
                oldOwner.getWarehouse().addItem(Config.CH_BID_CURRENCY_ITEM_ID, getResidence().getDeposit());
            }
            getResidence().setAuctionLength(0);
            getResidence().setAuctionMinBid(0L);
            getResidence().setAuctionDescription("");
            getResidence().getSiegeDate().setTimeInMillis(0L);
            getResidence().getLastSiegeDate().setTimeInMillis(0L);
            getResidence().getOwnDate().setTimeInMillis(System.currentTimeMillis());
            getResidence().setJdbcState(JdbcEntityState.UPDATED);
            getResidence().changeOwner(winnerSiegeClan.getClan());
            getResidence().startCycleTask();
        } else if (oldOwner != null) {
            final Player player2 = oldOwner.getLeader().getPlayer();
            if (player2 != null) {
                player2.sendPacket(SystemMsg.THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED);
            } else {
                PlayerMessageStack.getInstance().mailto(oldOwner.getLeaderId(), SystemMsg.THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED.packet(null));
            }
        } else {
            getResidence().setAuctionLength(0);
            getResidence().setAuctionMinBid(0L);
            getResidence().setAuctionDescription("");
            getResidence().getSiegeDate().setTimeInMillis(0L);
            getResidence().getLastSiegeDate().setTimeInMillis(0L);
            getResidence().getOwnDate().setTimeInMillis(0L);
            getResidence().setJdbcState(JdbcEntityState.UPDATED);
        }
        super.stopEvent(step);
    }

    @Override
    public boolean isParticle(final Player player) {
        return false;
    }

    @Override
    public AuctionSiegeClanObject newSiegeClan(final String type, final int clanId, final long param, final long date) {
        final Clan clan = ClanTable.getInstance().getClan(clanId);
        return (clan == null) ? null : new AuctionSiegeClanObject(type, clan, param, date);
    }

    public long getEndSiegeForCH() {
        final long start_date_msec = getResidence().getSiegeDate().getTimeInMillis();
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis());
        cal.set(Calendar.DAY_OF_WEEK, 2);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final long end_date = cal.getTimeInMillis() + getResidence().getAuctionLength() * 86400000L;
        return (end_date - start_date_msec) / 1000L;
    }

    public Calendar getEndSiegeDate() {
        return _endSiegeDate;
    }
}
