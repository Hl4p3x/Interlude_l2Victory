package ru.j2dev.gameserver.model.entity.events.objects;

import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import ru.j2dev.gameserver.utils.Location;

import java.util.Set;

public class SiegeToggleNpcObject implements SpawnableObject {
    private final SiegeToggleNpcInstance _toggleNpc;
    private final Location _location;

    public SiegeToggleNpcObject(final int id, final int fakeNpcId, final Location loc, final int hp, final Set<String> set) {
        _location = loc;
        (_toggleNpc = (SiegeToggleNpcInstance) NpcTemplateHolder.getInstance().getTemplate(id).getNewInstance()).initFake(fakeNpcId);
        _toggleNpc.setMaxHp(hp);
        _toggleNpc.setZoneList(set);
    }

    @Override
    public void spawnObject(final GlobalEvent event) {
        _toggleNpc.decayFake();
        if (event.isInProgress()) {
            _toggleNpc.addEvent(event);
        } else {
            _toggleNpc.removeEvent(event);
        }
        _toggleNpc.setCurrentHp(_toggleNpc.getMaxHp(), true);
        _toggleNpc.spawnMe(_location);
    }

    @Override
    public void despawnObject(final GlobalEvent event) {
        _toggleNpc.removeEvent(event);
        _toggleNpc.decayFake();
        _toggleNpc.decayMe();
    }

    @Override
    public void refreshObject(final GlobalEvent event) {
    }

    public SiegeToggleNpcInstance getToggleNpc() {
        return _toggleNpc;
    }

    public boolean isAlive() {
        return _toggleNpc.isVisible();
    }
}
