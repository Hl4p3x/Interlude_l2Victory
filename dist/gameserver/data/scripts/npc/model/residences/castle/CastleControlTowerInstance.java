package npc.model.residences.castle;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Spawner;
import ru.j2dev.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.HashSet;
import java.util.Set;

public class CastleControlTowerInstance extends SiegeToggleNpcInstance {
    private final Set<Spawner> _spawnList;

    public CastleControlTowerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _spawnList = new HashSet<>();
    }

    @Override
    public boolean canInteractWithKarmaPlayer() {
        return true;
    }

    @Override
    public void onDeathImpl(final Creature killer) {
        for (final Spawner spawn : _spawnList) {
            spawn.stopRespawn();
        }
        _spawnList.clear();
    }

    @Override
    public void register(final Spawner spawn) {
        _spawnList.add(spawn);
    }
}
