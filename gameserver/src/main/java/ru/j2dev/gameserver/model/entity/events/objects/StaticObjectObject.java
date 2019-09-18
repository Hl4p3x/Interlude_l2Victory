package ru.j2dev.gameserver.model.entity.events.objects;

import ru.j2dev.gameserver.data.xml.holder.StaticObjectHolder;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.instances.StaticObjectInstance;

public class StaticObjectObject implements SpawnableObject {
    private final int _uid;
    private StaticObjectInstance _instance;

    public StaticObjectObject(final int id) {
        _uid = id;
    }

    @Override
    public void spawnObject(final GlobalEvent event) {
        _instance = StaticObjectHolder.getInstance().getObject(_uid);
    }

    @Override
    public void despawnObject(final GlobalEvent event) {
    }

    @Override
    public void refreshObject(final GlobalEvent event) {
        if (!event.isInProgress()) {
            _instance.removeEvent(event);
        } else {
            _instance.addEvent(event);
        }
    }

    public void setMeshIndex(final int id) {
        _instance.setMeshIndex(id);
        _instance.broadcastInfo(false);
    }

    public int getUId() {
        return _uid;
    }
}
