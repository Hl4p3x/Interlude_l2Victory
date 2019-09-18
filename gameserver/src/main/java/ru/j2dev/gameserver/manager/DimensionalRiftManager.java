package ru.j2dev.gameserver.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import ru.j2dev.commons.geometry.Rectangle;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.SimpleSpawner;
import ru.j2dev.gameserver.model.Territory;
import ru.j2dev.gameserver.model.entity.DimensionalRift;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.TeleportToLocation;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DimensionalRiftManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DimensionalRiftManager.class);
    private static final int DIMENSIONAL_FRAGMENT_ITEM_ID = 7079;

    private Map<Integer, Map<Integer, DimensionalRiftRoom>> _rooms = new ConcurrentHashMap<>();

    public DimensionalRiftManager() {
        load();
    }

    public static DimensionalRiftManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public static void teleToLocation(final Player player, final Location loc, final Reflection ref) {
        if (player.isTeleporting() || player.isDeleted()) {
            return;
        }
        player.setIsTeleporting(true);
        player.setTarget(null);
        player.stopMove();
        if (player.isInBoat()) {
            player.setBoat(null);
        }
        player.breakFakeDeath();
        player.decayMe();
        player.setLoc(loc);
        if (ref == null) {
            player.setReflection(ReflectionManager.DEFAULT);
        }
        player.setLastClientPosition(null);
        player.setLastServerPosition(null);
        player.sendPacket(new TeleportToLocation(player, loc));
    }

    public DimensionalRiftRoom getRoom(final int type, final int room) {
        return _rooms.get(type).get(room);
    }

    public Map<Integer, DimensionalRiftRoom> getRooms(final int type) {
        return _rooms.get(type);
    }

    public void load() {
        int countGood = 0;
        int countBad = 0;
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            final File file = new File(Config.DATAPACK_ROOT, "data/dimensional_rift.xml");
            if (!file.exists()) {
                throw new IOException();
            }
            final Document doc = factory.newDocumentBuilder().parse(file);
            Location tele = new Location();
            int xMin;
            int xMax;
            int yMin;
            int yMax;
            int zMin;
            int zMax;
            for (Node rift = doc.getFirstChild(); rift != null; rift = rift.getNextSibling()) {
                if ("rift".equalsIgnoreCase(rift.getNodeName())) {
                    for (Node area = rift.getFirstChild(); area != null; area = area.getNextSibling()) {
                        if ("area".equalsIgnoreCase(area.getNodeName())) {
                            NamedNodeMap attrs = area.getAttributes();
                            final int type = Integer.parseInt(attrs.getNamedItem("type").getNodeValue());
                            for (Node room = area.getFirstChild(); room != null; room = room.getNextSibling()) {
                                if ("room".equalsIgnoreCase(room.getNodeName())) {
                                    attrs = room.getAttributes();
                                    final int roomId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                                    final Node boss = attrs.getNamedItem("isBossRoom");
                                    final boolean isBossRoom = boss != null && Boolean.parseBoolean(boss.getNodeValue());
                                    Territory territory = null;
                                    for (Node coord = room.getFirstChild(); coord != null; coord = coord.getNextSibling()) {
                                        if ("teleport".equalsIgnoreCase(coord.getNodeName())) {
                                            attrs = coord.getAttributes();
                                            tele = Location.parseLoc(attrs.getNamedItem("loc").getNodeValue());
                                        } else if ("zone".equalsIgnoreCase(coord.getNodeName())) {
                                            attrs = coord.getAttributes();
                                            xMin = Integer.parseInt(attrs.getNamedItem("xMin").getNodeValue());
                                            xMax = Integer.parseInt(attrs.getNamedItem("xMax").getNodeValue());
                                            yMin = Integer.parseInt(attrs.getNamedItem("yMin").getNodeValue());
                                            yMax = Integer.parseInt(attrs.getNamedItem("yMax").getNodeValue());
                                            zMin = Integer.parseInt(attrs.getNamedItem("zMin").getNodeValue());
                                            zMax = Integer.parseInt(attrs.getNamedItem("zMax").getNodeValue());
                                            territory = new Territory().add(new Rectangle(xMin, yMin, xMax, yMax).setZmin(zMin).setZmax(zMax));
                                        }
                                    }
                                    if (territory == null) {
                                        LOGGER.error("DimensionalRiftManager: invalid spawn data for room id " + roomId + "!");
                                    }
                                    if (!_rooms.containsKey(type)) {
                                        _rooms.put(type, new ConcurrentHashMap<>());
                                    }
                                    _rooms.get(type).put(roomId, new DimensionalRiftRoom(territory, tele, isBossRoom));
                                    for (Node spawn = room.getFirstChild(); spawn != null; spawn = spawn.getNextSibling()) {
                                        if ("spawn".equalsIgnoreCase(spawn.getNodeName())) {
                                            attrs = spawn.getAttributes();
                                            final int mobId = Integer.parseInt(attrs.getNamedItem("mobId").getNodeValue());
                                            final int delay = Integer.parseInt(attrs.getNamedItem("delay").getNodeValue());
                                            final int count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
                                            final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(mobId);
                                            if (template == null) {
                                                LOGGER.warn("Template " + mobId + " not found!");
                                            }
                                            if (!_rooms.containsKey(type)) {
                                                LOGGER.warn("Type " + type + " not found!");
                                            } else if (!_rooms.get(type).containsKey(roomId)) {
                                                LOGGER.warn("Room " + roomId + " in Type " + type + " not found!");
                                            }
                                            if (template != null && _rooms.containsKey(type) && _rooms.get(type).containsKey(roomId)) {
                                                final SimpleSpawner spawnDat = new SimpleSpawner(template);
                                                spawnDat.setTerritory(territory);
                                                spawnDat.setHeading(-1);
                                                spawnDat.setRespawnDelay(delay);
                                                spawnDat.setAmount(count);
                                                _rooms.get(type).get(roomId).getSpawns().add(spawnDat);
                                                ++countGood;
                                            } else {
                                                ++countBad;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("DimensionalRiftManager: Error on loading dimensional rift spawns!", e);
        }
        final int typeSize = _rooms.size();
        int roomSize = _rooms.values().stream().mapToInt(Map::size).sum();
        LOGGER.info("DimensionalRiftManager: Loaded " + typeSize + " room types with " + roomSize + " rooms.");
        LOGGER.info("DimensionalRiftManager: Loaded " + countGood + " dimensional rift spawns, " + countBad + " errors.");
    }

    public void reload() {
        _rooms.forEach((key, value) -> value.clear());
        _rooms.clear();
        load();
    }

    public boolean checkIfInRiftZone(final Location loc, final boolean ignorePeaceZone) {
        if (ignorePeaceZone) {
            return _rooms.get(0).get(1).checkIfInZone(loc);
        }
        return _rooms.get(0).get(1).checkIfInZone(loc) && !_rooms.get(0).get(0).checkIfInZone(loc);
    }

    public boolean checkIfInPeaceZone(final Location loc) {
        return _rooms.get(0).get(0).checkIfInZone(loc);
    }

    public void teleportToWaitingRoom(final Player player) {
        teleToLocation(player, Location.findPointToStay(getRoom(0, 0).getTeleportCoords(), 0, 250, ReflectionManager.DEFAULT.getGeoIndex()), null);
    }

    public void start(final Player player, final int type, final NpcInstance npc) {
        if (!player.isInParty()) {
            showHtmlFile(player, "rift/NoParty.htm", npc);
            return;
        }
        if (!player.isGM()) {
            if (!player.getParty().isLeader(player)) {
                showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
                return;
            }
            if (player.getParty().isInDimensionalRift()) {
                showHtmlFile(player, "rift/Cheater.htm", npc);
                if (!player.isGM()) {
                    LOGGER.warn("Player " + player.getName() + "(" + player.getObjectId() + ") was cheating in dimension rift area!");
                }
                return;
            }
            if (player.getParty().getMemberCount() < Config.RIFT_MIN_PARTY_SIZE) {
                showHtmlFile(player, "rift/SmallParty.htm", npc);
                return;
            }
            for (final Player p : player.getParty().getPartyMembers()) {
                if (!checkIfInPeaceZone(p.getLoc())) {
                    showHtmlFile(player, "rift/NotInWaitingRoom.htm", npc);
                    return;
                }
            }
            for (final Player p2 : player.getParty().getPartyMembers()) {
                final ItemInstance i = p2.getInventory().getItemByItemId(DIMENSIONAL_FRAGMENT_ITEM_ID);
                if (i == null || i.getCount() < getNeededItems(type)) {
                    showHtmlFile(player, "rift/NoFragments.htm", npc);
                    return;
                }
            }
            for (final Player p2 : player.getParty().getPartyMembers()) {
                if (!p2.getInventory().destroyItemByItemId(DIMENSIONAL_FRAGMENT_ITEM_ID, getNeededItems(type))) {
                    showHtmlFile(player, "rift/NoFragments.htm", npc);
                    return;
                }
            }
        }
        new DimensionalRift(player.getParty(), type, Rnd.get(1, _rooms.get(type).size() - 1));
    }

    private long getNeededItems(final int type) {
        switch (type) {
            case 1: {
                return Config.RIFT_ENTER_COST_RECRUIT;
            }
            case 2: {
                return Config.RIFT_ENTER_COST_SOLDIER;
            }
            case 3: {
                return Config.RIFT_ENTER_COST_OFFICER;
            }
            case 4: {
                return Config.RIFT_ENTER_COST_CAPTAIN;
            }
            case 5: {
                return Config.RIFT_ENTER_COST_COMMANDER;
            }
            case 6: {
                return Config.RIFT_ENTER_COST_HERO;
            }
            default: {
                return Long.MAX_VALUE;
            }
        }
    }

    public void showHtmlFile(final Player player, final String file, final NpcInstance npc) {
        final NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
        html.setFile(file);
        html.replace("%t_name%", npc.getName());
        player.sendPacket(html);
    }

    private static class LazyHolder {
        private static final DimensionalRiftManager INSTANCE = new DimensionalRiftManager();
    }

    public class DimensionalRiftRoom {
        private final Territory _territory;
        private final Location _teleportCoords;
        private final boolean _isBossRoom;
        private final List<SimpleSpawner> _roomSpawns;

        public DimensionalRiftRoom(final Territory territory, final Location tele, final boolean isBossRoom) {
            _territory = territory;
            _teleportCoords = tele;
            _isBossRoom = isBossRoom;
            _roomSpawns = new ArrayList<>();
        }

        public Location getTeleportCoords() {
            return _teleportCoords;
        }

        public boolean checkIfInZone(final Location loc) {
            return checkIfInZone(loc.x, loc.y, loc.z);
        }

        public boolean checkIfInZone(final int x, final int y, final int z) {
            return _territory.isInside(x, y, z);
        }

        public boolean isBossRoom() {
            return _isBossRoom;
        }

        public List<SimpleSpawner> getSpawns() {
            return _roomSpawns;
        }
    }
}
