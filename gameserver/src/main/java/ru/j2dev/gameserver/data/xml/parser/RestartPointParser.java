package ru.j2dev.gameserver.data.xml.parser;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Attribute;
import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.commons.geometry.Polygon;
import ru.j2dev.commons.geometry.Rectangle;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.manager.MapRegionManager;
import ru.j2dev.gameserver.model.Territory;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.templates.mapregion.RestartArea;
import ru.j2dev.gameserver.templates.mapregion.RestartPoint;
import ru.j2dev.gameserver.utils.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestartPointParser extends AbstractFileParser<MapRegionManager> {

    private RestartPointParser() {
        super(MapRegionManager.getInstance());
    }

    public static RestartPointParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/mapregion/restart_points.xml");
    }

    @Override
    protected void readData(final MapRegionManager holder, final Element rootElement) {
        final List<Pair<Territory, Map<Race, String>>> restartArea = new ArrayList<>();
        final Map<String, RestartPoint> restartPoint = new HashMap<>();
        for (final Element listElement : rootElement.getChildren()) {
            if ("restart_area".equals(listElement.getName())) {
                Territory territory = null;
                final Map<Race, String> restarts = new HashMap<>();
                for (final Element n : listElement.getChildren()) {
                    if ("region".equalsIgnoreCase(n.getName())) {
                        final Attribute map = n.getAttribute("map");
                        final String s = map.getValue();
                        final String[] val = s.split("_");
                        final int rx = Integer.parseInt(val[0]);
                        final int ry = Integer.parseInt(val[1]);
                        final int x1 = World.MAP_MIN_X + (rx - Config.GEO_X_FIRST << 15);
                        final int y1 = World.MAP_MIN_Y + (ry - Config.GEO_Y_FIRST << 15);
                        final int x2 = x1 + 32768 - 1;
                        final int y2 = y1 + 32768 - 1;
                        final Rectangle shape = new Rectangle(x1, y1, x2, y2);
                        shape.setZmin(World.MAP_MIN_Z);
                        shape.setZmax(World.MAP_MAX_Z);
                        if (territory == null) {
                            territory = new Territory();
                        }
                        territory.add(shape);
                    } else if ("polygon".equalsIgnoreCase(n.getName())) {
                        final Polygon shape2 = ZoneParser.parsePolygon(n);
                        if (!shape2.validate()) {
                            error("RestartPointParser: invalid territory data : " + shape2 + "!");
                        }
                        if (territory == null) {
                            territory = new Territory();
                        }
                        territory.add(shape2);
                    } else {
                        if (!"restart".equalsIgnoreCase(n.getName())) {
                            continue;
                        }
                        final Race race = Race.valueOf(n.getAttributeValue("race"));
                        final String locName = n.getAttributeValue("loc");
                        restarts.put(race, locName);
                    }
                }
                if (territory == null) {
                    throw new RuntimeException("RestartPointParser: empty territory!");
                }
                if (restarts.isEmpty()) {
                    throw new RuntimeException("RestartPointParser: restarts not defined!");
                }
                restartArea.add(new ImmutablePair<>(territory, restarts));
            } else {
                if (!"restart_loc".equals(listElement.getName())) {
                    continue;
                }
                final String name = listElement.getAttributeValue("name");
                final int bbs = Integer.parseInt(listElement.getAttributeValue("bbs", "0"));
                final int msgId = Integer.parseInt(listElement.getAttributeValue("msg_id", "0"));
                final List<Location> restartPoints = new ArrayList<>();
                List<Location> PKrestartPoints = new ArrayList<>();
                for (final Element n2 : listElement.getChildren()) {
                    if ("restart_point".equals(n2.getName())) {
                        n2.getChildren().stream().filter(d -> "coords".equalsIgnoreCase(d.getName())).map(d -> Location.parseLoc(d.getAttribute("loc").getValue())).forEach(restartPoints::add);
                    } else {
                        if (!"PKrestart_point".equals(n2.getName())) {
                            continue;
                        }
                        n2.getChildren().stream().filter(d -> "coords".equalsIgnoreCase(d.getName())).map(d -> Location.parseLoc(d.getAttribute("loc").getValue())).forEach(PKrestartPoints::add);
                    }
                }
                if (restartPoints.isEmpty()) {
                    throw new RuntimeException("RestartPointParser: restart_points not defined for restart_loc : " + name + "!");
                }
                if (PKrestartPoints.isEmpty()) {
                    PKrestartPoints = restartPoints;
                }
                final RestartPoint rp = new RestartPoint(name, bbs, msgId, restartPoints, PKrestartPoints);
                restartPoint.put(name, rp);
            }
        }
        restartArea.forEach(ra -> {
            final Map<Race, RestartPoint> restarts2 = new HashMap<>();
            ra.getValue().forEach((key, value) -> {
                final RestartPoint rp2 = restartPoint.get(value);
                if (rp2 == null) {
                    throw new RuntimeException("RestartPointParser: restart_loc not found : " + value + "!");
                }
                restarts2.put(key, rp2);
                try {
                    holder.addRegionData(new RestartArea(ra.getKey(), restarts2));
                } catch (Exception ex) {
                    System.out.println("Cant add restart area \"" + value + "\"");
                    ex.printStackTrace();
                }
            });
        });
    }

    private static class LazyHolder {
        protected static final RestartPointParser INSTANCE = new RestartPointParser();
    }
}
