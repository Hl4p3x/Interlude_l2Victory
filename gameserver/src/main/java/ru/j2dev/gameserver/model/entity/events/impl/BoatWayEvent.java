package ru.j2dev.gameserver.model.entity.events.impl;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.BoatHolder;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.entity.boat.Boat;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.entity.events.objects.BoatPoint;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.MapUtils;

import java.util.ArrayList;
import java.util.List;

public class BoatWayEvent extends GlobalEvent {
    public static final String BOAT_POINTS = "boat_points";
    private final int _ticketId;
    private final Location _returnLoc;
    private final Boat _boat;

    public BoatWayEvent(final MultiValueSet<String> set) {
        super(set);
        _ticketId = set.getInteger("ticketId", 0);
        _returnLoc = Location.parseLoc(set.getString("return_point"));
        final String className = set.getString("class", null);
        if (className != null) {
            _boat = BoatHolder.getInstance().initBoat(getName(), className);
            final Location loc = Location.parseLoc(set.getString("spawn_point"));
            _boat.setLoc(loc, true);
            _boat.setHeading(loc.h);
        } else {
            _boat = BoatHolder.getInstance().getBoat(getName());
        }
        _boat.setWay((className != null) ? 1 : 0, this);
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void startEvent() {
        final L2GameServerPacket startPacket = _boat.startPacket();
        for (final Player player : _boat.getPlayers()) {
            if (_ticketId > 0) {
                if (player.consumeItem(_ticketId, 1L)) {
                    if (startPacket == null) {
                        continue;
                    }
                    player.sendPacket(startPacket);
                } else {
                    player.sendPacket(SystemMsg.YOU_DO_NOT_POSSESS_THE_CORRECT_TICKET_TO_BOARD_THE_BOAT);
                    _boat.oustPlayer(player, _returnLoc, true);
                }
            } else {
                if (startPacket == null) {
                    continue;
                }
                player.sendPacket(startPacket);
            }
        }
        moveNext();
    }

    public void moveNext() {
        final List<BoatPoint> points = getObjects(BOAT_POINTS);
        if (_boat.getRunState() >= points.size()) {
            _boat.trajetEnded(true);
            return;
        }
        final BoatPoint bp = points.get(_boat.getRunState());
        if (bp.getSpeed1() >= 0) {
            _boat.setMoveSpeed(bp.getSpeed1());
        }
        if (bp.getSpeed2() >= 0) {
            _boat.setRotationSpeed(bp.getSpeed2());
        }
        if (_boat.getRunState() == 0) {
            _boat.broadcastCharInfo();
        }
        _boat.setRunState(_boat.getRunState() + 1);
        if (bp.isTeleport()) {
            _boat.teleportShip(bp.getX(), bp.getY(), bp.getZ());
        } else {
            _boat.moveToLocation(bp.getX(), bp.getY(), bp.getZ(), 0, false);
        }
    }

    @Override
    public void reCalcNextTime(final boolean onInit) {
        registerActions();
    }

    @Override
    protected long startTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public List<Player> broadcastPlayers(final int range) {
        if (range <= 0) {
            final List<Player> list = new ArrayList<>();
            final int rx = MapUtils.regionX(_boat.getX());
            final int ry = MapUtils.regionY(_boat.getY());
            final int offset = Config.SHOUT_OFFSET;
            GameObjectsStorage.getPlayers().stream().filter(player -> player.getReflection() == _boat.getReflection()).forEach(player -> {
                final int tx = MapUtils.regionX(player);
                final int ty = MapUtils.regionY(player);
                if (tx < rx - offset || tx > rx + offset || ty < ry - offset || ty > ry + offset) {
                    return;
                }
                list.add(player);
            });
            return list;
        }
        return World.getAroundPlayers(_boat, range, Math.max(range / 2, 200));
    }

    @Override
    protected void printInfo() {
    }

    public Location getReturnLoc() {
        return _returnLoc;
    }
}
