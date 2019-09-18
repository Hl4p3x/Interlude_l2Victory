package ru.j2dev.gameserver.data.xml.parser;


import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.TvTArenaHolder;
import ru.j2dev.gameserver.templates.arenas.TvTArena;
import ru.j2dev.gameserver.utils.Location;

import java.io.File;

public final class TvTArenaParser extends AbstractFileParser<TvTArenaHolder> {

    private TvTArenaParser() {
        super(TvTArenaHolder.getInstance());
    }

    public static TvTArenaParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/arenas/tvt_arena.xml");
    }

    @Override
    protected void readData(final TvTArenaHolder holder, final Element rootElement) {
        rootElement.getChildren().forEach(element -> {
            final TvTArena arena = new TvTArena(Integer.parseInt(element.getAttributeValue("id")));
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
        protected static final TvTArenaParser INSTANCE = new TvTArenaParser();
    }
}
