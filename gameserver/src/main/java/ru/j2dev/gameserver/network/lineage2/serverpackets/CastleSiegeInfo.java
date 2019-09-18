package ru.j2dev.gameserver.network.lineage2.serverpackets;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.CastleSiegeEvent;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.pledge.Alliance;
import ru.j2dev.gameserver.model.pledge.Clan;

import java.util.Calendar;

public class CastleSiegeInfo extends L2GameServerPacket {
    private final int _id;
    private final int _ownerObjectId;
    private long _startTime;
    private int _allyId;
    private boolean _isLeader;
    private String _ownerName;
    private String _leaderName;
    private String _allyName;
    private int[] _nextTimeMillis = ArrayUtils.EMPTY_INT_ARRAY;

    protected CastleSiegeInfo(final Residence residence, final Player player) {
        _ownerName = "NPC";
        _leaderName = "";
        _allyName = "";

        _id = residence.getId();
        _ownerObjectId = residence.getOwnerId();
        final Clan owner = residence.getOwner();
        if (owner != null) {
            _isLeader = (owner.getLeaderId(0) == player.getObjectId());
            _ownerName = owner.getName();
            _leaderName = owner.getLeaderName(0);
            final Alliance ally = owner.getAlliance();
            if (ally != null) {
                _allyId = ally.getAllyId();
                _allyName = ally.getAllyName();
            }
        }
    }

    public CastleSiegeInfo(final Castle castle, final Player player) {
        this((Residence) castle, player);
        final CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
        final long siegeTimeMillis = castle.getSiegeDate().getTimeInMillis();
        if (siegeTimeMillis == 0L) {
            _nextTimeMillis = ArrayUtils.toPrimitive(siegeEvent.getNextSiegeTimes());
        } else {
            _startTime = (int) (siegeTimeMillis / 1000L);
        }
    }

    public CastleSiegeInfo(final ClanHall ch, final Player player) {
        this((Residence) ch, player);
        _startTime = (int) (ch.getSiegeDate().getTimeInMillis() / 1000L);
    }

    @Override
    protected void writeImpl() {
        writeC(0xc9);
        writeD(_id);
        writeD(_isLeader ? 1 : 0);
        writeD(_ownerObjectId);
        writeS(_ownerName);
        writeS(_leaderName);
        writeD(_allyId);
        writeS(_allyName);
        writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000L));
        writeD((int) _startTime);
        if (_startTime == 0L) {
            writeDD(_nextTimeMillis, true);
        }
    }
}
