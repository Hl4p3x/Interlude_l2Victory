package ru.j2dev.gameserver.model.entity.events.objects;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;

import java.util.List;

public class ZoneObject implements InitableObject {
    private final String _name;
    private Zone _zone;

    public ZoneObject(final String name) {
        _name = name;
    }

    @Override
    public void initObject(final GlobalEvent e) {
        final Reflection r = e.getReflection();
        _zone = r.getZone(_name);
    }

    public void setActive(final boolean a) {
        _zone.setActive(a);
    }

    public void setActive(final boolean a, final GlobalEvent event) {
        setActive(a);
    }

    public Zone getZone() {
        return _zone;
    }

    public List<Player> getInsidePlayers() {
        return _zone.getInsidePlayers();
    }

    public boolean checkIfInZone(final Creature c) {
        return _zone.checkIfInZone(c);
    }
}
