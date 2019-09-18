package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractDirParser;
import ru.j2dev.commons.geometry.Polygon;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.DoorHolder;
import ru.j2dev.gameserver.templates.DoorTemplate;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.utils.Location;

import java.io.File;

public final class DoorParser extends AbstractDirParser<DoorHolder> {

    protected DoorParser() {
        super(DoorHolder.getInstance());
    }

    public static DoorParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/xml/doors/");
    }

    @Override
    public boolean isIgnored(final File f) {
        return false;
    }

    private StatsSet initBaseStats() {
        final StatsSet baseDat = new StatsSet();
        baseDat.set("level", 0);
        baseDat.set("baseSTR", 0);
        baseDat.set("baseCON", 0);
        baseDat.set("baseDEX", 0);
        baseDat.set("baseINT", 0);
        baseDat.set("baseWIT", 0);
        baseDat.set("baseMEN", 0);
        baseDat.set("baseShldDef", 0);
        baseDat.set("baseShldRate", 0);
        baseDat.set("baseAccCombat", 38);
        baseDat.set("baseEvasRate", 38);
        baseDat.set("baseCritRate", 38);
        baseDat.set("baseAtkRange", 0);
        baseDat.set("baseMpMax", 0);
        baseDat.set("baseCpMax", 0);
        baseDat.set("basePAtk", 0);
        baseDat.set("baseMAtk", 0);
        baseDat.set("basePAtkSpd", 0);
        baseDat.set("baseMAtkSpd", 0);
        baseDat.set("baseWalkSpd", 0);
        baseDat.set("baseRunSpd", 0);
        baseDat.set("baseHpReg", 0);
        baseDat.set("baseCpReg", 0);
        baseDat.set("baseMpReg", 0);
        return baseDat;
    }

    @Override
    protected void readData(final DoorHolder holder, final Element rootElement) {
        for (final Element doorElement : rootElement.getChildren()) {
            if ("door".equals(doorElement.getName())) {
                final StatsSet doorSet = initBaseStats();
                StatsSet aiParams = null;
                doorSet.set("door_type", doorElement.getAttributeValue("type"));
                final Element posElement = doorElement.getChild("pos");
                final int x = Integer.parseInt(posElement.getAttributeValue("x"));
                final int y = Integer.parseInt(posElement.getAttributeValue("y"));
                final int z = Integer.parseInt(posElement.getAttributeValue("z"));
                final Location doorPos;
                doorSet.set("pos", doorPos = new Location(x, y, z));
                final Polygon shape = new Polygon();
                int minz;
                int maxz;
                final Element shapeElement = doorElement.getChild("shape");
                minz = Integer.parseInt(shapeElement.getAttributeValue("minz"));
                maxz = Integer.parseInt(shapeElement.getAttributeValue("maxz"));
                shape.add(Integer.parseInt(shapeElement.getAttributeValue("ax")), Integer.parseInt(shapeElement.getAttributeValue("ay")));
                shape.add(Integer.parseInt(shapeElement.getAttributeValue("bx")), Integer.parseInt(shapeElement.getAttributeValue("by")));
                shape.add(Integer.parseInt(shapeElement.getAttributeValue("cx")), Integer.parseInt(shapeElement.getAttributeValue("cy")));
                shape.add(Integer.parseInt(shapeElement.getAttributeValue("dx")), Integer.parseInt(shapeElement.getAttributeValue("dy")));
                shape.setZmin(minz);
                shape.setZmax(maxz);
                doorSet.set("shape", shape);
                doorPos.setZ(minz + 32);
                for (final Element n : doorElement.getChildren()) {
                    if ("set".equals(n.getName())) {
                        doorSet.set(n.getAttributeValue("name"), n.getAttributeValue("value"));
                    } else {
                        if (!"ai_params".equals(n.getName())) {
                            continue;
                        }
                        if (aiParams == null) {
                            aiParams = new StatsSet();
                            doorSet.set("ai_params", aiParams);
                        }
                        for (final Element aiParamElement : n.getChildren()) {
                            aiParams.set(aiParamElement.getAttributeValue("name"), aiParamElement.getAttributeValue("value"));
                        }
                    }
                }
                doorSet.set("uid", doorElement.getAttributeValue("id"));
                doorSet.set("name", doorElement.getAttributeValue("name"));
                doorSet.set("baseHpMax", doorElement.getAttributeValue("hp"));
                doorSet.set("basePDef", doorElement.getAttributeValue("pdef"));
                doorSet.set("baseMDef", doorElement.getAttributeValue("mdef"));
                doorSet.set("collision_height", maxz - minz & 0xFFF0);
                doorSet.set("collision_radius", Math.max(50, Math.min(doorPos.x - shape.getXmin(), doorPos.y - shape.getYmin())));
                final DoorTemplate template = new DoorTemplate(doorSet);
                holder.addTemplate(template);
            }
        }
    }

    private static class LazyHolder {
        protected static final DoorParser INSTANCE = new DoorParser();
    }
}
