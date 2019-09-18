package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractDirParser;
import ru.j2dev.commons.geometry.Polygon;
import ru.j2dev.commons.time.cron.SchedulingPattern;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.DoorHolder;
import ru.j2dev.gameserver.data.xml.holder.InstantZoneHolder;
import ru.j2dev.gameserver.data.xml.holder.SpawnHolder;
import ru.j2dev.gameserver.data.xml.holder.ZoneHolder;
import ru.j2dev.gameserver.model.Territory;
import ru.j2dev.gameserver.templates.DoorTemplate;
import ru.j2dev.gameserver.templates.InstantZone;
import ru.j2dev.gameserver.templates.InstantZone.DoorInfo;
import ru.j2dev.gameserver.templates.InstantZone.SpawnInfo;
import ru.j2dev.gameserver.templates.InstantZone.SpawnInfo2;
import ru.j2dev.gameserver.templates.InstantZone.ZoneInfo;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.ZoneTemplate;
import ru.j2dev.gameserver.templates.spawn.SpawnTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.io.File;
import java.util.*;

public class InstantZoneParser extends AbstractDirParser<InstantZoneHolder> {

    public InstantZoneParser() {
        super(InstantZoneHolder.getInstance());
    }

    public static InstantZoneParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/xml/instances/");
    }

    @Override
    public boolean isIgnored(final File f) {
        return false;
    }

    @Override
    protected void readData(final InstantZoneHolder holder, final Element rootElement) {
        for (final Element element : rootElement.getChildren()) {
            SchedulingPattern resetReuse = new SchedulingPattern("30 6 * * *");
            int timelimit = -1;
            int timer = 60;
            int mapx = -1;
            int mapy = -1;
            boolean dispelBuffs;
            boolean onPartyDismiss = true;
            int sharedReuseGroup = 0;
            int collapseIfEmpty;
            int spawnType;
            SpawnInfo spawnDat;
            int removedItemId = 0;
            int removedItemCount = 0;
            int giveItemId = 0;
            int givedItemCount = 0;
            int requiredQuestId = 0;
            int maxChannels;
            boolean removedItemNecessity = false;
            boolean setReuseUponEntry = true;
            final StatsSet params = new StatsSet();
            final List<SpawnInfo> spawns = new ArrayList<>();
            Map<Integer, DoorInfo> doors = Collections.emptyMap();
            Map<String, ZoneInfo> zones = Collections.emptyMap();
            Map<String, SpawnInfo2> spawns2 = Collections.emptyMap();
            final int instanceId = Integer.parseInt(element.getAttributeValue("id"));
            final String name = element.getAttributeValue("name");
            String n = element.getAttributeValue("timelimit");
            if (n != null) {
                timelimit = Integer.parseInt(n);
            }
            n = element.getAttributeValue("collapseIfEmpty");
            collapseIfEmpty = Integer.parseInt(n);
            n = element.getAttributeValue("maxChannels");
            maxChannels = Integer.parseInt(n);
            n = element.getAttributeValue("dispelBuffs");
            dispelBuffs = (Boolean.parseBoolean(n));
            int minLevel = 0;
            int maxLevel = 0;
            int minParty = 1;
            int maxParty = 9;
            List<Location> teleportLocs = Collections.emptyList();
            Location ret = null;
            for (final Element subElement : element.getChildren()) {
                if ("level".equalsIgnoreCase(subElement.getName())) {
                    minLevel = Integer.parseInt(subElement.getAttributeValue("min"));
                    maxLevel = Integer.parseInt(subElement.getAttributeValue("max"));
                } else if ("collapse".equalsIgnoreCase(subElement.getName())) {
                    onPartyDismiss = Boolean.parseBoolean(subElement.getAttributeValue("on-party-dismiss"));
                    timer = Integer.parseInt(subElement.getAttributeValue("timer"));
                } else if ("party".equalsIgnoreCase(subElement.getName())) {
                    minParty = Integer.parseInt(subElement.getAttributeValue("min"));
                    maxParty = Integer.parseInt(subElement.getAttributeValue("max"));
                } else if ("return".equalsIgnoreCase(subElement.getName())) {
                    ret = Location.parseLoc(subElement.getAttributeValue("loc"));
                } else if ("teleport".equalsIgnoreCase(subElement.getName())) {
                    if (teleportLocs.isEmpty()) {
                        teleportLocs = new ArrayList<>(1);
                    }
                    teleportLocs.add(Location.parseLoc(subElement.getAttributeValue("loc")));
                } else if ("remove".equalsIgnoreCase(subElement.getName())) {
                    removedItemId = Integer.parseInt(subElement.getAttributeValue("itemId"));
                    removedItemCount = Integer.parseInt(subElement.getAttributeValue("count"));
                    removedItemNecessity = Boolean.parseBoolean(subElement.getAttributeValue("necessary"));
                } else if ("give".equalsIgnoreCase(subElement.getName())) {
                    giveItemId = Integer.parseInt(subElement.getAttributeValue("itemId"));
                    givedItemCount = Integer.parseInt(subElement.getAttributeValue("count"));
                } else if ("quest".equalsIgnoreCase(subElement.getName())) {
                    requiredQuestId = Integer.parseInt(subElement.getAttributeValue("id"));
                } else if ("reuse".equalsIgnoreCase(subElement.getName())) {
                    resetReuse = new SchedulingPattern(subElement.getAttributeValue("resetReuse"));
                    sharedReuseGroup = Integer.parseInt(subElement.getAttributeValue("sharedReuseGroup"));
                    setReuseUponEntry = Boolean.parseBoolean(subElement.getAttributeValue("setUponEntry"));
                } else if ("geodata".equalsIgnoreCase(subElement.getName())) {
                    final String[] rxy = subElement.getAttributeValue("map").split("_");
                    mapx = Integer.parseInt(rxy[0]);
                    mapy = Integer.parseInt(rxy[1]);
                } else if ("doors".equalsIgnoreCase(subElement.getName())) {
                    for (final Element e : subElement.getChildren()) {
                        if (doors.isEmpty()) {
                            doors = new HashMap<>();
                        }
                        final boolean opened = e.getAttributeValue("opened") != null && Boolean.parseBoolean(e.getAttributeValue("opened"));
                        final boolean invul = e.getAttributeValue("invul") == null || Boolean.parseBoolean(e.getAttributeValue("invul"));
                        final DoorTemplate template = DoorHolder.getInstance().getTemplate(Integer.parseInt(e.getAttributeValue("id")));
                        doors.put(template.getNpcId(), new DoorInfo(template, opened, invul));
                    }
                } else if ("zones".equalsIgnoreCase(subElement.getName())) {
                    for (final Element e : subElement.getChildren()) {
                        if (zones.isEmpty()) {
                            zones = new HashMap<>();
                        }
                        final boolean active = e.getAttributeValue("active") != null && Boolean.parseBoolean(e.getAttributeValue("active"));
                        final ZoneTemplate template2 = ZoneHolder.getInstance().getTemplate(e.getAttributeValue("name"));
                        if (template2 == null) {
                            error("Zone: " + e.getAttributeValue("name") + " not found; file: " + getCurrentFileName());
                        } else {
                            zones.put(template2.getName(), new ZoneInfo(template2, active));
                        }
                    }
                } else if ("add_parameters".equalsIgnoreCase(subElement.getName())) {
                    subElement.getChildren().stream().filter(e -> "param".equalsIgnoreCase(e.getName())).forEach(e -> params.set(e.getAttributeValue("name"), e.getAttributeValue("value")));
                } else {
                    if (!"spawns".equalsIgnoreCase(subElement.getName())) {
                        continue;
                    }
                    for (final Element e : subElement.getChildren()) {
                        if ("group".equalsIgnoreCase(e.getName())) {
                            final String group = e.getAttributeValue("name");
                            final boolean spawned = e.getAttributeValue("spawned") != null && Boolean.parseBoolean(e.getAttributeValue("spawned"));
                            final List<SpawnTemplate> templates = SpawnHolder.getInstance().getSpawn(group);
                            if (templates == null) {
                                info("not find spawn group: " + group + " in file: " + getCurrentFileName());
                            } else {
                                if (spawns2.isEmpty()) {
                                    spawns2 = new Hashtable<>();
                                }
                                spawns2.put(group, new SpawnInfo2(templates, spawned));
                            }
                        } else {
                            if (!"spawn".equalsIgnoreCase(e.getName())) {
                                continue;
                            }
                            final String[] mobs = e.getAttributeValue("mobId").split(" ");
                            final String respawnNode = e.getAttributeValue("respawn");
                            final int respawn = (respawnNode != null) ? Integer.parseInt(respawnNode) : 0;
                            final String respawnRndNode = e.getAttributeValue("respawnRnd");
                            final int respawnRnd = (respawnRndNode != null) ? Integer.parseInt(respawnRndNode) : 0;
                            final String countNode = e.getAttributeValue("count");
                            final int count = (countNode != null) ? Integer.parseInt(countNode) : 1;
                            final List<Location> coords = new ArrayList<>();
                            spawnType = 0;
                            final String spawnTypeNode = e.getAttributeValue("type");
                            if (spawnTypeNode == null || "point".equalsIgnoreCase(spawnTypeNode)) {
                                spawnType = 0;
                            } else if ("rnd".equalsIgnoreCase(spawnTypeNode)) {
                                spawnType = 1;
                            } else if ("loc".equalsIgnoreCase(spawnTypeNode)) {
                                spawnType = 2;
                            } else {
                                error("Spawn type  '" + spawnTypeNode + "' is unknown!");
                            }
                            for (final Element e2 : e.getChildren()) {
                                if ("coords".equalsIgnoreCase(e2.getName())) {
                                    coords.add(Location.parseLoc(e2.getAttributeValue("loc")));
                                }
                            }
                            Territory territory = null;
                            if (spawnType == 2) {
                                final Polygon poly = new Polygon();
                                for (final Location loc : coords) {
                                    poly.add(loc.x, loc.y).setZmin(loc.z).setZmax(loc.z);
                                }
                                if (!poly.validate()) {
                                    error("invalid spawn territory for instance id : " + instanceId + " - " + poly + "!");
                                }
                                territory = new Territory().add(poly);
                            }
                            for (final String mob : mobs) {
                                final int mobId = Integer.parseInt(mob);
                                spawnDat = new SpawnInfo(spawnType, mobId, count, respawn, respawnRnd, coords, territory);
                                spawns.add(spawnDat);
                            }
                        }
                    }
                }
            }
            final InstantZone instancedZone = new InstantZone(instanceId, name, resetReuse, sharedReuseGroup, timelimit, dispelBuffs, minLevel, maxLevel, minParty, maxParty, timer, onPartyDismiss, teleportLocs, ret, mapx, mapy, doors, zones, spawns2, spawns, collapseIfEmpty, maxChannels, removedItemId, removedItemCount, removedItemNecessity, giveItemId, givedItemCount, requiredQuestId, setReuseUponEntry, params);
            holder.addInstantZone(instancedZone);
        }
    }

    private static class LazyHolder {
        protected static final InstantZoneParser INSTANCE = new InstantZoneParser();
    }
}
