package ru.j2dev.gameserver.data.xml.parser;


import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.LastHeroArenaHolder;
import ru.j2dev.gameserver.templates.arenas.LastHeroArena;
import ru.j2dev.gameserver.utils.Location;

import java.io.File;

public final class LastHeroArenaParser extends AbstractFileParser<LastHeroArenaHolder> {

    private LastHeroArenaParser() {
        super(LastHeroArenaHolder.getInstance());
    }

    public static LastHeroArenaParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/arenas/lasthero_arena.xml");
    }

    @Override
    protected void readData(final LastHeroArenaHolder holder, final Element rootElement) {
        rootElement.getChildren().forEach(element -> {
            final LastHeroArena arena = new LastHeroArena(Integer.parseInt(element.getAttributeValue("id")));
            element.getChildren().forEach(subElement -> {
                if ("teleport_locations".equalsIgnoreCase(subElement.getName())) {
                    subElement.getChildren().forEach(e -> {
                        final int x = Integer.parseInt(e.getAttributeValue("x"));
                        final int y = Integer.parseInt(e.getAttributeValue("y"));
                        final int z = Integer.parseInt(e.getAttributeValue("z"));
                        arena.addTeleportLocation(new Location(x, y, z));
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
        protected static final LastHeroArenaParser INSTANCE = new LastHeroArenaParser();
    }
}
