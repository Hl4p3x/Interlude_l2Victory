package ru.j2dev.gameserver.model;


import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.dao.CharacterDAO;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.SummonInstance;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GameObjectsStorage {

    private static final ConcurrentMap<Integer, GameObject> _objects = new ConcurrentHashMap<>(60000 * Config.RATE_MOB_SPAWN + Config.MAXIMUM_ONLINE_USERS);
    private static final ConcurrentMap<Integer, NpcInstance> _npcs = new ConcurrentHashMap<>(60000 * Config.RATE_MOB_SPAWN);
    private static final ConcurrentMap<Integer, Player> _players = new ConcurrentHashMap<>(Config.MAXIMUM_ONLINE_USERS);
    private static final ConcurrentMap<Integer, SummonInstance> _summons = new ConcurrentHashMap<>(Config.MAXIMUM_ONLINE_USERS);
    private static final ConcurrentMap<Integer, DoorInstance> _doors = new ConcurrentHashMap<>();

    public static Player getPlayer(final String name) {
        Optional<Player> player = Optional.ofNullable(getPlayer(CharacterDAO.getInstance().getObjectIdByName(name)));
        if(!player.isPresent()) {
            player = getPlayers().stream().filter(p -> p.getName().equalsIgnoreCase(name)).findAny();
        }
        return player.orElse(null);
    }

    public static void sendPacketToAllPlayers(IStaticPacket packet) {
        _players.values().forEach(player -> player.sendPacket(packet));
    }

    public static Player getPlayer(final int objId) {
        return _players.get(objId);
    }

    public static Collection<Player> getPlayers() {
        return _players.values();
    }

    public static Collection<Player> getAllPlayersForIterate() {
        return _players.values();
    }

    public static Collection<GameObject> getAllObjects() {
        return _objects.values();
    }

    public static Collection<Player> getAllPlayers() {
        return _players.values();
    }

    public static Collection<DoorInstance> getAllDoors() {
        return _doors.values();
    }

    public static Collection<NpcInstance> getNpcs() {
        return _npcs.values();
    }

    public static Collection<SummonInstance> getSummons() {
        return _summons.values();
    }

    public static int getAllOnlinePlayerCount() {
        return getPlayers(player -> !player.isPhantom()).size();
    }

    public static int getAllOnlinePhantomsCount() {
        return getPlayers(Player::isPhantom).size();
    }

    public static int getAllOfflineCount() {
        if (!Config.SERVICES_OFFLINE_TRADE_ALLOW) {
            return 0;
        }

        return (int) _players.values().stream().filter(Player::isInOfflineMode).count();
    }

    public static GameObject findObject(final int objId) {
        return _objects.get(objId);
    }

	/*
     Методы возвращающие различных игроков по параметрам
	 */

    public static NpcInstance getByNpcId(final int npcId) {
        return _npcs.values().stream().filter(npcInstance -> npcInstance.getNpcId() == npcId).findFirst().orElse(null);
    }

    public static NpcInstance getNpcByName(final String name) {
        return getNpcs().stream().filter(npc -> npc.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static List<Player> getPlayers(Predicate<Player> predicate) {
        return _players.values().stream().filter(predicate).collect(Collectors.toList());
    }


    public static List<NpcInstance> getNpcs(Predicate<NpcInstance> predicate) {
        return _npcs.values().stream().filter(predicate).collect(Collectors.toList());
    }

    public static NpcInstance getNpc(final int objId) {
        return _npcs.get(objId);
    }

    public static <T extends GameObject> void put(final T gameObject) {
        final ConcurrentMap<Integer, T> map = getMapForObject(gameObject);
        if (map != null) {
            if (map.containsKey(gameObject.getObjectId())) {
                return;
            }
            map.put(gameObject.getObjectId(), gameObject);
        }
        if (!_objects.containsKey(gameObject.getObjectId())) {
            _objects.put(gameObject.getObjectId(), gameObject);
        }
    }

    public static <T extends GameObject> void remove(final T gameObject) {
        final ConcurrentMap<Integer, T> map = getMapForObject(gameObject);
        if (map != null) {
            map.remove(gameObject.getObjectId());
        }
        _objects.remove(gameObject.getObjectId());
    }

    @SuppressWarnings("unchecked")
    private static <T extends GameObject> ConcurrentMap<Integer, T> getMapForObject(final T gameObject) {
        if (gameObject.isNpc()) {
            return (ConcurrentMap<Integer, T>) _npcs;
        }
        if (gameObject.isSummon()) {
            return (ConcurrentMap<Integer, T>) _summons;
        }
        if (gameObject.isPlayer()) {
            return (ConcurrentMap<Integer, T>) _players;
        }
        if (gameObject.isDoor()) {
            return (ConcurrentMap<Integer, T>) _doors;
        }

        return null;
    }
}