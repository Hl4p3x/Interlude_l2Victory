package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.moveroute.MoveRoute;

import java.util.HashMap;
import java.util.Map;

public class MoveRouteHolder extends AbstractHolder {
    private final Map<String, MoveRoute> _routes;

    private MoveRouteHolder() {
        _routes = new HashMap<>();
    }

    public static MoveRouteHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addRoute(final MoveRoute route) {
        if (route.getNodes().isEmpty()) {
            LOGGER.warn("Route \"" + route.getName() + "\" is empty.");
        }
        _routes.put(route.getName(), route);
    }

    public MoveRoute getRoute(final String name) {
        return _routes.get(name);
    }

    @Override
    public int size() {
        return _routes.size();
    }

    @Override
    public void clear() {
        _routes.clear();
    }

    private static class LazyHolder {
        protected static final MoveRouteHolder INSTANCE = new MoveRouteHolder();
    }
}
