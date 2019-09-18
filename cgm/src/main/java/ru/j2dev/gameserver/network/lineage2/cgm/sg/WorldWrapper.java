package ru.j2dev.gameserver.network.lineage2.cgm.sg;

import ru.akumu.smartguard.core.wrappers.ISmartPlayer;
import ru.akumu.smartguard.core.wrappers.IWorld;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class WorldWrapper extends IWorld {
    private static final WorldWrapper INSTANCE = new WorldWrapper();

    public static WorldWrapper getInstance() {
        return WorldWrapper.INSTANCE;
    }

    @Override
    public ISmartPlayer getPlayerByObjId(final int objId) {
        final Player player = GameObjectsStorage.getPlayer(objId);
        return (player != null) ? new PlayerWrapper(player) : null;
    }

    @Override
    public List<ISmartPlayer> getAllPlayers() {
        return GameObjectsStorage.getAllPlayersForIterate().stream().map(player -> new PlayerWrapper(player, new ClientWrapper(player.getNetConnection()))).collect(Collectors.toCollection(LinkedList::new));
    }
}