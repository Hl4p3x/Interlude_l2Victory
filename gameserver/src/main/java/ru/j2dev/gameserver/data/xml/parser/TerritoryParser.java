package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractDirParser;
import ru.j2dev.commons.geometry.Polygon;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.TerritoryHolder;
import ru.j2dev.gameserver.model.Territory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author JunkyFunky
 */
public final class TerritoryParser extends AbstractDirParser<TerritoryHolder> {

    private TerritoryParser() {
        super(TerritoryHolder.getInstance());
    }

    public static TerritoryParser getInstance() {
        return SingletonHolder._instance;
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/xml/territory/");
    }

    @Override
    public boolean isIgnored(final File f) {
        return false;
    }

    @Override
    protected void readData(final TerritoryHolder holder, final Element rootElement) {
        rootElement.getChildren().stream().filter(element -> element.getName().equalsIgnoreCase("territory")).forEach(element -> {
            final String terName = element.getAttributeValue("name");
            final Territory territory = parseTerritory(terName, element);
            holder.addTerritory(terName, territory);
        });
    }

    private Territory parseTerritory(final String name, final Element e) {
        final Territory t = new Territory();
        t.add(parseCorrectedPolygon(name, e, false));

        e.getChildren("banned_territory").stream().map(iterator -> parseCorrectedPolygon(name, iterator, false)).forEach(t::addBanned);

        return t;
    }

    private Polygon parseCorrectedPolygon(final String name, final Element e, boolean correct) {
        Polygon temp = new Polygon();
        List<Element> polygons = new ArrayList<>(e.getChildren("add"));
        if (correct) {
            Collections.shuffle(polygons);
        }
        for (final Element addElement : polygons) {
            final int x = Integer.parseInt(addElement.getAttributeValue("x"));
            final int y = Integer.parseInt(addElement.getAttributeValue("y"));
            final int zmin = Integer.parseInt(addElement.getAttributeValue("zmin"));
            final int zmax = Integer.parseInt(addElement.getAttributeValue("zmax"));
            temp.add(x, y).setZmin(zmin).setZmax(zmax);
        }
        if (!temp.validate()) {
            error("Invalid polygon: " + name + '{' + temp.toString() + "}. File: " + getCurrentFileName());
            temp = parseCorrectedPolygon(name, e, true);
        } else if (correct && temp.validate()) {
            info("Corrected polygon: " + name + " " + temp.toString() + " File: " + getCurrentFileName());
        }
        return temp;
    }

    private static class SingletonHolder {
        protected static final TerritoryParser _instance = new TerritoryParser();
    }
}
