package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractDirParser;
import ru.j2dev.commons.geometry.Circle;
import ru.j2dev.commons.geometry.Polygon;
import ru.j2dev.commons.geometry.Rectangle;
import ru.j2dev.commons.geometry.Shape;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ZoneHolder;
import ru.j2dev.gameserver.model.Territory;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.ZoneTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ZoneParser extends AbstractDirParser<ZoneHolder> {

    protected ZoneParser() {
        super(ZoneHolder.getInstance());
    }

    public static ZoneParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static Rectangle parseRectangle(final Element n) {
        int zmin = World.MAP_MIN_Z;
        int zmax = World.MAP_MAX_Z;
        final Iterator<Element> i = n.getChildren().iterator();
        Element d = i.next();
        String[] coord = d.getAttributeValue("loc").split("[\\s,;]+");
        final int x1 = Integer.parseInt(coord[0]);
        final int y1 = Integer.parseInt(coord[1]);
        if (coord.length > 2) {
            zmin = Integer.parseInt(coord[2]);
            zmax = Integer.parseInt(coord[3]);
        }
        d = i.next();
        coord = d.getAttributeValue("loc").split("[\\s,;]+");
        final int x2 = Integer.parseInt(coord[0]);
        final int y2 = Integer.parseInt(coord[1]);
        if (coord.length > 2) {
            zmin = Integer.parseInt(coord[2]);
            zmax = Integer.parseInt(coord[3]);
        }
        final Rectangle rectangle = new Rectangle(x1, y1, x2, y2);
        rectangle.setZmin(zmin);
        rectangle.setZmax(zmax);
        return rectangle;
    }

    public static Polygon parsePolygon(final Element shape) {
        final Polygon poly = new Polygon();
        shape.getChildren().stream().filter(d -> "coords".equals(d.getName())).map(d -> d.getAttributeValue("loc").split("[\\s,;]+")).forEach(coord -> {
            if (coord.length < 4) {
                poly.add(Integer.parseInt(coord[0]), Integer.parseInt(coord[1])).setZmin(World.MAP_MIN_Z).setZmax(World.MAP_MAX_Z);
            } else {
                poly.add(Integer.parseInt(coord[0]), Integer.parseInt(coord[1])).setZmin(Integer.parseInt(coord[2])).setZmax(Integer.parseInt(coord[3]));
            }
        });
        return poly;
    }

    private static Circle parseCircle(final Element shape) {
        final String[] coord = shape.getAttribute("loc").getValue().split("[\\s,;]+");
        Circle circle;
        if (coord.length < 5) {
            circle = new Circle(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2])).setZmin(World.MAP_MIN_Z).setZmax(World.MAP_MAX_Z);
        } else {
            circle = new Circle(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2])).setZmin(Integer.parseInt(coord[3])).setZmax(Integer.parseInt(coord[4]));
        }
        return circle;
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/xml/zone/");
    }

    @Override
    public boolean isIgnored(final File f) {
        return false;
    }

    @Override
    protected void readData(final ZoneHolder holder, final Element rootElement) {
        for (final Element zoneElement : rootElement.getChildren()) {
            final StatsSet zoneDat = new StatsSet();
            if ("zone".equals(zoneElement.getName())) {
                zoneDat.set("name", zoneElement.getAttribute("name").getValue());
                zoneDat.set("type", zoneElement.getAttribute("type").getValue());
                Territory territory = null;
                for (final Element n : zoneElement.getChildren()) {
                    switch (n.getName()) {
                        case "set":
                            zoneDat.set(n.getAttributeValue("name"), n.getAttributeValue("val"));
                            break;
                        case "restart_point": {
                            final List<Location> restartPoints = n.getChildren().stream().
                                    filter(d -> "coords".equalsIgnoreCase(d.getName())).
                                    map(d -> Location.parseLoc(d.getAttribute("loc").getValue())).
                                    collect(Collectors.toList());
                            zoneDat.set("restart_points", restartPoints);
                            break;
                        }
                        case "PKrestart_point": {
                            final List<Location> PKrestartPoints = n.getChildren().stream().
                                    filter(d -> "coords".equalsIgnoreCase(d.getName())).
                                    map(d -> Location.parseLoc(d.getAttribute("loc").getValue())).
                                    collect(Collectors.toList());
                            zoneDat.set("PKrestart_points", PKrestartPoints);
                            break;
                        }
                        default:
                            boolean isShape;
                            if ((isShape = "rectangle".equalsIgnoreCase(n.getName())) || "banned_rectangle".equalsIgnoreCase(n.getName())) {
                                final Shape shape = parseRectangle(n);
                                if (territory == null) {
                                    territory = new Territory();
                                    zoneDat.set("territory", territory);
                                }
                                if (isShape) {
                                    territory.add(shape);
                                } else {
                                    territory.addBanned(shape);
                                }
                            } else if ((isShape = "circle".equalsIgnoreCase(n.getName())) || "banned_cicrcle".equalsIgnoreCase(n.getName())) {
                                final Shape shape = parseCircle(n);
                                if (territory == null) {
                                    territory = new Territory();
                                    zoneDat.set("territory", territory);
                                }
                                if (isShape) {
                                    territory.add(shape);
                                } else {
                                    territory.addBanned(shape);
                                }
                            } else {
                                if (!(isShape = "polygon".equalsIgnoreCase(n.getName())) && !"banned_polygon".equalsIgnoreCase(n.getName())) {
                                    continue;
                                }
                                final Polygon shape2 = parsePolygon(n);
                                if (!shape2.validate()) {
                                    error("ZoneParser: invalid territory data : " + shape2 + ", zone: " + zoneDat.getString("name") + "!");
                                }
                                if (territory == null) {
                                    territory = new Territory();
                                    zoneDat.set("territory", territory);
                                }
                                if (isShape) {
                                    territory.add(shape2);
                                } else {
                                    territory.addBanned(shape2);
                                }
                            }
                            break;
                    }
                }
                if (territory == null || territory.getTerritories().isEmpty()) {
                    error("Empty territory for zone: " + zoneDat.get("name"));
                }
                final ZoneTemplate template = new ZoneTemplate(zoneDat);
                holder.addTemplate(template);
            }
        }
    }

    private static class LazyHolder {
        protected static final ZoneParser INSTANCE = new ZoneParser();
    }
}
