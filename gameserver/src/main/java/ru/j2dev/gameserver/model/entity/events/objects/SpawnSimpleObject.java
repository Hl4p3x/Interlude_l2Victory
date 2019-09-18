package ru.j2dev.gameserver.model.entity.events.objects;

import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.NpcUtils;

public class SpawnSimpleObject implements SpawnableObject {
    private final int _npcId;
    private final Location _loc;
    private NpcInstance _npc;

    public SpawnSimpleObject(final int npcId, final Location loc) {
        _npcId = npcId;
        _loc = loc;
    }

    @Override
    public void spawnObject(final GlobalEvent event) {
        (_npc = NpcUtils.spawnSingle(_npcId, _loc, event.getReflection())).addEvent(event);
    }

    @Override
    public void despawnObject(final GlobalEvent event) {
        _npc.removeEvent(event);
        _npc.deleteMe();
    }

    @Override
    public void refreshObject(final GlobalEvent event) {
    }
}
