package ru.j2dev.gameserver.model.entity.olympiad;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.gameserver.data.xml.holder.InstantZoneHolder;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.InstantZone;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Objects;

@HideAccess
@StringEncryption
public class OlympiadStadium extends Reflection {
    public static final int OLYMPIAD_HOST = 36402;
    private static final int[] EMPTY_VISITORS = new int[0];

    private final Location _observe_loc;
    private final int _stadium_id;
    private boolean _isFree;

    public OlympiadStadium(final int id, final int ozid, final Location observe_loc) {
        _isFree = true;
        final InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(ozid);
        init(instantZone);
        setName("OlyStadium-" + id);
        _stadium_id = id;
        _observe_loc = observe_loc;
        setCollapseIfEmptyTime(0);
        _isFree = true;
    }

    public boolean isFree() {
        return _isFree;
    }

    public void setFree(final boolean val) {
        _isFree = val;
    }

    public void clear() {
        final ArrayList<Player> teleport_list = new ArrayList<>();
        final ArrayList<GameObject> delete_list = new ArrayList<>();
        lock.lock();
        try {
            _objects.stream().filter(Objects::nonNull).
                    filter(o -> !(o instanceof DoorInstance)).
                    filter(o -> !o.isNpc() || ((NpcInstance) o).getNpcId() != OLYMPIAD_HOST).
                    forEach(o -> {
                        if (o.isPlayer() && !o.getPlayer().isOlyObserver()) {
                            teleport_list.add((Player) o);
                        } else {
                            if (o.isPlayer()) {
                                return;
                            }
                            delete_list.add(o);
                        }
                    });
        } finally {
            lock.unlock();
        }
        teleport_list.forEach(player -> {
            if (player.getParty() != null && equals(player.getParty().getReflection())) {
                player.getParty().setReflection(null);
            }
            if (equals(player.getReflection())) {
                if (getReturnLoc() != null) {
                    player.teleToLocation(getReturnLoc(), 0);
                } else {
                    player.teleToClosestTown();
                }
            }
        });
        delete_list.forEach(GameObject::deleteMe);
    }

    public final int getStadiumId() {
        return _stadium_id;
    }

    public Location getLocForParticipant(final OlympiadPlayer part) {
        return Location.findPointToStay(getInstancedZone().getTeleportCoords().get(part.getSide() - 1), 50, 50, getGeoIndex());
    }

    public Location getObservingLoc() {
        return _observe_loc;
    }

    public int getObserverCount() {
        return (int) getPlayers().stream().filter(Player::isOlyObserver).count();
    }

    public void setZonesActive(final boolean active) {
        getZones().forEach(zone -> zone.setActive(active));
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public int[] getVisitors() {
        return EMPTY_VISITORS;
    }
}
