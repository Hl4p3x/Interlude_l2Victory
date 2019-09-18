package ru.j2dev.gameserver.model.entity.events.objects;

import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.instances.DoorInstance;

public class DoorObject implements SpawnableObject, InitableObject {
    private final int _id;
    private DoorInstance _door;
    private boolean _weak;

    public DoorObject(final int id) {
        _id = id;
    }

    @Override
    public void initObject(final GlobalEvent e) {
        _door = e.getReflection().getDoor(_id);
    }

    @Override
    public void spawnObject(final GlobalEvent event) {
        refreshObject(event);
    }

    @Override
    public void despawnObject(final GlobalEvent event) {
        final Reflection ref = event.getReflection();
        if (ref == ReflectionManager.DEFAULT) {
            refreshObject(event);
        }
    }

    @Override
    public void refreshObject(final GlobalEvent event) {
        if (!event.isInProgress()) {
            _door.removeEvent(event);
        } else {
            _door.addEvent(event);
        }
        if (_door.getCurrentHp() <= 0.0) {
            _door.decayMe();
            _door.spawnMe();
        }
        _door.setCurrentHp(_door.getMaxHp() * (isWeak() ? 0.5 : 1.0), true);
        close(event);
    }

    public int getUId() {
        return _door.getDoorId();
    }

    public int getUpgradeValue() {
        return _door.getUpgradeHp();
    }

    public void setUpgradeValue(final GlobalEvent event, final int val) {
        _door.setUpgradeHp(val);
        refreshObject(event);
    }

    public void open(final GlobalEvent e) {
        _door.openMe(null, !e.isInProgress());
    }

    public void close(final GlobalEvent e) {
        _door.closeMe(null, !e.isInProgress());
    }

    public DoorInstance getDoor() {
        return _door;
    }

    public boolean isWeak() {
        return _weak;
    }

    public void setWeak(final boolean weak) {
        _weak = weak;
    }
}
