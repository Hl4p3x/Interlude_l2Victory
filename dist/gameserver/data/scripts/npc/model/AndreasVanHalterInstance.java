package npc.model;

import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.RaidBossInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.Objects;
import java.util.stream.IntStream;

public class AndreasVanHalterInstance extends RaidBossInstance {
    private static final String SPAWN_GROUP_NAME = "[guard_of_andreas]";
    private static final int[] DOORS_IDS = {19160016, 19160017};

    public AndreasVanHalterInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        SpawnManager.getInstance().spawn(SPAWN_GROUP_NAME);
    }

    @Override
    protected void onDespawn() {
        super.onDespawn();
        SpawnManager.getInstance().despawn(SPAWN_GROUP_NAME);
        IntStream.of(DOORS_IDS).mapToObj(doorId -> getReflection().getDoor(doorId)).filter(Objects::nonNull).forEach(DoorInstance::openMe);
    }
}