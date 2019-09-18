package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.data.xml.AbstractDirParser;
import ru.j2dev.commons.geometry.Point2D;
import ru.j2dev.commons.geometry.Polygon;
import ru.j2dev.commons.time.cron.AddPattern;
import ru.j2dev.commons.time.cron.NextTime;
import ru.j2dev.commons.time.cron.SchedulingPattern;
import ru.j2dev.commons.time.cron.SchedulingPattern.InvalidPatternException;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.SpawnHolder;
import ru.j2dev.gameserver.model.Territory;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.spawn.PeriodOfDay;
import ru.j2dev.gameserver.templates.spawn.SpawnNpcInfo;
import ru.j2dev.gameserver.templates.spawn.SpawnTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.SpawnMesh;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public final class SpawnParser extends AbstractDirParser<SpawnHolder> {

    protected SpawnParser() {
        super(SpawnHolder.getInstance());
    }

    public static SpawnParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/xml/spawn/");
    }

    @Override
    public boolean isIgnored(final File f) {
        return false;
    }

    @Override
    protected void readData(final SpawnHolder holder, final Element rootElement) {
        for (final Element spawnListElement : rootElement.getChildren()) {
            if ("spawn".equalsIgnoreCase(spawnListElement.getName())) {
                final String makerName = spawnListElement.getAttributeValue("name");
                final String eventName = spawnListElement.getAttributeValue("event_name");
                SpawnMesh spawnMesh = null;
                for (final Element spawnElement : spawnListElement.getChildren()) {
                    if ("mesh".equalsIgnoreCase(spawnElement.getName())) {
                        spawnMesh = parseSpawnMesh(spawnElement);
                    } else {
                        if (!"npc".equalsIgnoreCase(spawnElement.getName())) {
                            continue;
                        }
                        final int npcTemplateId = Integer.parseInt(spawnElement.getAttributeValue("id", "0"));
                        final int count = Integer.parseInt(spawnElement.getAttributeValue("count"));
                        final long respawn = Long.parseLong(spawnElement.getAttributeValue("respawn", "60"));
                        final long respawnRand = Long.parseLong(spawnElement.getAttributeValue("respawn_rand", "0"));
                        if (respawnRand > respawn) {
                            throw new RuntimeException("Invalid respawn respawn_rand > respawn of " + spawnListElement.getName());
                        }
                        final String respawnCronPattern = spawnElement.getAttributeValue("respawn_cron");
                        NextTime respawnCron = null;
                        if (respawnCronPattern != null) {
                            try {
                                respawnCron = new SchedulingPattern(respawnCronPattern);
                            } catch (InvalidPatternException ipe) {
                                try {
                                    respawnCron = new AddPattern(respawnCronPattern);
                                } catch (Exception ex) {
                                    throw new RuntimeException("Invalid respawn data of " + spawnListElement.getName(), ex);
                                }
                                if (respawnCron == null) {
                                    throw new RuntimeException("Invalid respawn data of " + spawnListElement.getName(), ipe);
                                }
                            }
                        }
                        final PeriodOfDay pod = PeriodOfDay.valueOf(spawnElement.getAttributeValue("period_of_day", PeriodOfDay.ALL.name()));
                        Location spawnPos = null;
                        if (spawnElement.getAttributeValue("pos") != null) {
                            spawnPos = Location.parseLoc(spawnElement.getAttributeValue("pos"));
                        } else if (spawnMesh == null) {
                            throw new RuntimeException("Neither mesh nor pos defined " + spawnListElement.getName());
                        }
                        MultiValueSet<String> aiParams = StatsSet.EMPTY;
                        for (final Element npcElement : spawnElement.getChildren()) {
                            if ("ai_params".equalsIgnoreCase(npcElement.getName())) {
                                for (final Element npcAiParamsElement : npcElement.getChildren()) {
                                    if ("set".equalsIgnoreCase(npcAiParamsElement.getName())) {
                                        if (aiParams == StatsSet.EMPTY) {
                                            aiParams = new MultiValueSet<>();
                                        }
                                        aiParams.set(npcAiParamsElement.getAttributeValue("name"), npcAiParamsElement.getAttributeValue("val"));
                                    }
                                }
                            }
                        }
                        try {
                            final SpawnTemplate spawnTemplate = new SpawnTemplate(makerName, eventName, pod, count, respawn, respawnRand, respawnCron);
                            final SpawnNpcInfo sni = new SpawnNpcInfo(npcTemplateId, count, aiParams);
                            spawnTemplate.addNpc(sni);
                            spawnTemplate.addSpawnRange((spawnPos != null) ? spawnPos : spawnMesh);
                            holder.addSpawn((eventName != null) ? eventName : PeriodOfDay.ALL.name(), spawnTemplate);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private Territory parseTerritory(final String name, final Element e) {
        final Territory t = new Territory();
        t.add(parsePolygon0(name, e));
        e.getChildren("banned_territory").stream().map(element -> parsePolygon0(name, element)).forEach(t::addBanned);
        return t;
    }

    private Polygon parsePolygon0(final String name, final Element e) {
        final Polygon temp = new Polygon();
        e.getChildren("add").forEach(addElement -> {
            final int x = Integer.parseInt(addElement.getAttributeValue("x"));
            final int y = Integer.parseInt(addElement.getAttributeValue("y"));
            final int zmin = Integer.parseInt(addElement.getAttributeValue("zmin"));
            final int zmax = Integer.parseInt(addElement.getAttributeValue("zmax"));
            temp.add(x, y).setZmin(zmin).setZmax(zmax);
        });
        if (!temp.validate()) {
            error("Invalid polygon: " + name + "{" + temp + "}. File: " + getCurrentFileName());
        }
        return temp;
    }

    private SpawnMesh parseSpawnMesh(final Element e) {
        short meshZMin = 32767;
        short meshZMax = -32768;
        final List<Point2D> vertexes = new LinkedList<>();
        for (final Element vertexElement : e.getChildren("vertex")) {
            final int vertexX = Integer.parseInt(vertexElement.getAttributeValue("x"));
            final int vertexY = Integer.parseInt(vertexElement.getAttributeValue("y"));
            meshZMin = (short) Math.min(meshZMin, Short.parseShort(vertexElement.getAttributeValue("minz")));
            meshZMax = (short) Math.max(meshZMax, Short.parseShort(vertexElement.getAttributeValue("maxz")));
            vertexes.add(new Point2D(vertexX, vertexY));
        }
        final SpawnMesh spawnMesh = new SpawnMesh();
        vertexes.forEach(vertex -> spawnMesh.add(vertex.getX(), vertex.getY()));
        assert meshZMax >= meshZMin;
        spawnMesh.setZmax(meshZMax);
        spawnMesh.setZmin(meshZMin);
        if (!spawnMesh.validate() || spawnMesh.getZmin() > spawnMesh.getZmax()) {
            throw new RuntimeException("Invalid spawn mesh " + spawnMesh + " defined for the node " + e.getName());
        }
        return spawnMesh;
    }

    private static class LazyHolder {
        protected static final SpawnParser INSTANCE = new SpawnParser();
    }
}
