package ru.j2dev.gameserver.data.xml.parser;


import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.KoreanStyleArenaHolder;
import ru.j2dev.gameserver.templates.arenas.KoreanStyleArena;
import ru.j2dev.gameserver.utils.Location;

import java.io.File;

public final class KoreanStyleArenaParser extends AbstractFileParser<KoreanStyleArenaHolder> {

    private KoreanStyleArenaParser() {
        super(KoreanStyleArenaHolder.getInstance());
    }

    public static KoreanStyleArenaParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/arenas/koreanstyle_arena.xml");
    }

    @Override
    protected void readData(final KoreanStyleArenaHolder holder, final Element rootElement) {
        rootElement.getChildren().forEach(element -> {
            final KoreanStyleArena arena = new KoreanStyleArena(Integer.parseInt(element.getAttributeValue("id")));
            element.getChildren().forEach(subElement -> {
                if ("team_left_corner".equalsIgnoreCase(subElement.getName())) {
                    final int teamId = Integer.parseInt(subElement.getAttributeValue("team"));
                    subElement.getChildren().forEach(e -> {
                        final int x = Integer.parseInt(e.getAttributeValue("x"));
                        final int y = Integer.parseInt(e.getAttributeValue("y"));
                        final int z = Integer.parseInt(e.getAttributeValue("z"));
                        final int h = Integer.parseInt(e.getAttributeValue("h"));
                        arena.addTeamLeftCorner(teamId, new Location(x, y, z, h));
                    });
                }
                if ("team_right_corner".equalsIgnoreCase(subElement.getName())) {
                    final int teamId = Integer.parseInt(subElement.getAttributeValue("team"));
                    subElement.getChildren().forEach(e -> {
                        final int x = Integer.parseInt(e.getAttributeValue("x"));
                        final int y = Integer.parseInt(e.getAttributeValue("y"));
                        final int z = Integer.parseInt(e.getAttributeValue("z"));
                        final int h = Integer.parseInt(e.getAttributeValue("h"));
                        arena.addTeamRightCorner(teamId, new Location(x, y, z, h));
                    });
                }
                if ("fight_location".equalsIgnoreCase(subElement.getName())) {
                    final int teamId = Integer.parseInt(subElement.getAttributeValue("team"));
                    subElement.getChildren().forEach(e -> {
                        final int x = Integer.parseInt(e.getAttributeValue("x"));
                        final int y = Integer.parseInt(e.getAttributeValue("y"));
                        final int z = Integer.parseInt(e.getAttributeValue("z"));
                        final int h = Integer.parseInt(e.getAttributeValue("h"));
                        arena.addFightLocation(teamId, new Location(x, y, z, h));
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
        protected static final KoreanStyleArenaParser INSTANCE = new KoreanStyleArenaParser();
    }
}
