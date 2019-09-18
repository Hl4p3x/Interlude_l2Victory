package ru.j2dev.gameserver.model.entity.boat;

import ru.j2dev.gameserver.ai.BoatAI;
import ru.j2dev.gameserver.ai.CharacterAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.entity.events.impl.BoatWayEvent;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.templates.CharTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PositionUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Boat extends Creature {
    protected final Set<Player> _players;
    private final BoatWayEvent[] _ways;
    protected int _fromHome;
    protected int _runState;
    private int _moveSpeed;
    private int _rotationSpeed;

    public Boat(final int objectId, final CharTemplate template) {
        super(objectId, template);
        _ways = new BoatWayEvent[2];
        _players = new CopyOnWriteArraySet<>();
    }

    @Override
    public void onSpawn() {
        _fromHome = 1;
        getCurrentWay().reCalcNextTime(false);
    }

    @Override
    public void setXYZ(final int x, final int y, final int z, final boolean MoveTask) {
        super.setXYZ(x, y, z, MoveTask);
        updatePeopleInTheBoat(x, y, z);
    }

    public void onEvtArrived() {
        getCurrentWay().moveNext();
    }

    protected void updatePeopleInTheBoat(final int x, final int y, final int z) {
        for (final Player player : _players) {
            if (player != null) {
                player.setXYZ(x, y, z, true);
            }
        }
    }

    public void addPlayer(final Player player, final Location boatLoc) {
        synchronized (_players) {
            _players.add(player);
            player.setBoat(this);
            player.setLoc(getLoc(), true);
            player.setInBoatPosition(boatLoc);
            player.stopMove(true, false, true);
            player.broadcastPacket(getOnPacket(player, boatLoc), inStopMovePacket(player));
        }
    }

    public void moveInBoat(final Player player, final Location ori, final Location loc) {
        if (player.getPet() != null) {
            player.sendPacket(SystemMsg.YOU_SHOULD_RELEASE_YOUR_PET_OR_SERVITOR_SO_THAT_IT_DOES_NOT_FALL_OFF_OF_THE_BOAT_AND_DROWN, ActionFail.STATIC);
            return;
        }
        if (player.getTransformation() != 0) {
            player.sendPacket(SystemMsg.YOU_CANNOT_BOARD_A_SHIP_WHILE_YOU_ARE_POLYMORPHED, ActionFail.STATIC);
            return;
        }
        if (player.isMovementDisabled() || player.isSitting()) {
            player.sendActionFailed();
            return;
        }
        if (!player.isInBoat()) {
            player.setBoat(this);
        }
        loc.h = PositionUtils.getHeadingTo(ori, loc);
        player.setInBoatPosition(loc);
        player.broadcastPacket(inMovePacket(player, ori, loc));
    }

    public void trajetEnded(final boolean oust) {
        _runState = 0;
        _fromHome = ((_fromHome != 1) ? 1 : 0);
        final L2GameServerPacket checkLocation = checkLocationPacket();
        if (checkLocation != null) {
            broadcastPacket(infoPacket(), checkLocation);
        }
        if (oust) {
            oustPlayers();
            getCurrentWay().reCalcNextTime(false);
        }
    }

    public void teleportShip(final int x, final int y, final int z) {
        if (isMoving()) {
            stopMove(false);
        }
        _players.forEach(player -> player.teleToLocation(x, y, z));
        setHeading(calcHeading(x, y));
        setXYZ(x, y, z, true);
        getCurrentWay().moveNext();
    }

    public void oustPlayer(final Player player, final Location loc, final boolean teleport) {
        synchronized (_players) {
            player._stablePoint = null;
            player.setBoat(null);
            player.setInBoatPosition(null);
            player.broadcastPacket(getOffPacket(player, loc));
            if (getLoc().distance3D(loc) < 2 * getActingRange()) {
                player.setLoc(loc, true);
            }
            if (teleport) {
                player.teleToLocation(loc);
            }
            _players.remove(player);
        }
    }

    public void removePlayer(final Player player) {
        synchronized (_players) {
            _players.remove(player);
        }
    }

    public void broadcastPacketToPassengers(final IStaticPacket packet) {
        _players.forEach(player -> player.sendPacket(packet));
    }

    @Override
    public int getActingRange() {
        return 150;
    }

    public abstract L2GameServerPacket infoPacket();

    @Override
    public abstract L2GameServerPacket movePacket();

    public abstract L2GameServerPacket inMovePacket(final Player p0, final Location p1, final Location p2);

    @Override
    public abstract L2GameServerPacket stopMovePacket();

    public abstract L2GameServerPacket inStopMovePacket(final Player p0);

    public abstract L2GameServerPacket startPacket();

    public abstract L2GameServerPacket validateLocationPacket(final Player p0);

    public abstract L2GameServerPacket checkLocationPacket();

    public abstract L2GameServerPacket getOnPacket(final Player p0, final Location p1);

    public abstract L2GameServerPacket getOffPacket(final Player p0, final Location p1);

    public abstract void oustPlayers();

    @Override
    public CharacterAI getAI() {
        if (_ai == null) {
            _ai = new BoatAI(this);
        }
        return _ai;
    }

    @Override
    public void broadcastCharInfo() {
        broadcastPacket(infoPacket());
    }

    @Override
    public void broadcastPacket(final L2GameServerPacket... packets) {
        final List<Player> players = new ArrayList<>();
        players.addAll(_players);
        players.addAll(World.getAroundPlayers(this));
        players.stream().filter(Objects::nonNull).forEach(player -> player.sendPacket(packets));
    }

    @Override
    public void validateLocation(final int broadcast) {
    }

    @Override
    public void sendChanges() {
    }

    @Override
    public int getMoveSpeed() {
        return _moveSpeed;
    }

    public void setMoveSpeed(final int moveSpeed) {
        _moveSpeed = moveSpeed;
    }

    @Override
    public int getRunSpeed() {
        return _moveSpeed;
    }

    @Override
    public ItemInstance getActiveWeaponInstance() {
        return null;
    }

    @Override
    public WeaponTemplate getActiveWeaponItem() {
        return null;
    }

    @Override
    public ItemInstance getSecondaryWeaponInstance() {
        return null;
    }

    @Override
    public WeaponTemplate getSecondaryWeaponItem() {
        return null;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public boolean isAutoAttackable(final Creature attacker) {
        return false;
    }

    public int getRunState() {
        return _runState;
    }

    public void setRunState(final int runState) {
        _runState = runState;
    }

    public int getRotationSpeed() {
        return _rotationSpeed;
    }

    public void setRotationSpeed(final int rotationSpeed) {
        _rotationSpeed = rotationSpeed;
    }

    public BoatWayEvent getCurrentWay() {
        return _ways[_fromHome];
    }

    public void setWay(final int id, final BoatWayEvent v) {
        _ways[id] = v;
    }

    public Set<Player> getPlayers() {
        return _players;
    }

    public boolean isDocked() {
        return _runState == 0;
    }

    public Location getReturnLoc() {
        return getCurrentWay().getReturnLoc();
    }

    @Override
    public boolean isBoat() {
        return true;
    }

    @Override
    public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper) {
        if (!isMoving()) {
            return Collections.singletonList(infoPacket());
        }
        final List<L2GameServerPacket> list = new ArrayList<>(2);
        list.add(infoPacket());
        list.add(movePacket());
        return list;
    }
}
