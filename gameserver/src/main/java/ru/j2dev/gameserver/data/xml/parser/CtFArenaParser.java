package ru.j2dev.gameserver.data.xml.parser;


import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.CtFArenaHolder;
import ru.j2dev.gameserver.templates.arenas.CtFArena;
import ru.j2dev.gameserver.utils.Location;

import java.io.File;

public final class CtFArenaParser extends AbstractFileParser<CtFArenaHolder> {

    private CtFArenaParser() {
        super(CtFArenaHolder.getInstance());
    }

    public static CtFArenaParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/arenas/ctf_arena.xml");
    }

    @Override
    protected void readData(final CtFArenaHolder holder, final Element rootElement) {
        rootElement.getChildren().forEach(element -> {
            final CtFArena arena = new CtFArena(Integer.parseInt(element.getAttributeValue("id")));
            element.getChildren().forEach(subElement -> {
                if ("teleport_locations".equalsIgnoreCase(subElement.getName())) {
                    final int teamId = Integer.parseInt(subElement.getAttributeValue("team"));
                    subElement.getChildren().forEach(e -> {
                        final int x = Integer.parseInt(e.getAttributeValue("x"));
                        final int y = Integer.parseInt(e.getAttributeValue("y"));
                        final int z = Integer.parseInt(e.getAttributeValue("z"));
                        arena.addTeleportLocation(teamId, new Location(x, y, z));
                    });
                }
                if ("redbase_locations".equalsIgnoreCase(subElement.getName())) {
                    subElement.getChildren().forEach(e -> {
                        final int x = Integer.parseInt(e.getAttributeValue("x"));
                        final int y = Integer.parseInt(e.getAttributeValue("y"));
                        final int z = Integer.parseInt(e.getAttributeValue("z"));
                        arena.setRedBaseLocation(new Location(x, y, z));
                    });
                }
                if ("bluebase_locations".equalsIgnoreCase(subElement.getName())) {
                    subElement.getChildren().forEach(e -> {
                        final int x = Integer.parseInt(e.getAttributeValue("x"));
                        final int y = Integer.parseInt(e.getAttributeValue("y"));
                        final int z = Integer.parseInt(e.getAttributeValue("z"));
                        arena.setBlueBaseLocation(new Location(x, y, z));
                    });
                }
                if ("zones".equalsIgnoreCase(subElement.getName())) {
                    subElement.getChildren().forEach(e -> {
                        arena.addZone(e.getAttributeValue("name"));
                    });
                }
            });
            holder.addArena(arena);
        });
    }

    private static class LazyHolder {
        protected static final CtFArenaParser INSTANCE = new CtFArenaParser();
    }
}
