package ru.j2dev.gameserver.templates.moveroute;

import java.util.ArrayList;
import java.util.List;

public class MoveRoute {
    private final List<MoveNode> _nodes;
    private final String _name;
    private final MoveRouteType _type;
    private final boolean _isRunning;

    public MoveRoute(final String name, final MoveRouteType type, final boolean isRunning) {
        _nodes = new ArrayList<>();
        _name = name;
        _type = type;
        _isRunning = isRunning;
    }

    public List<MoveNode> getNodes() {
        return _nodes;
    }

    public String getName() {
        return _name;
    }

    public MoveRouteType getType() {
        return _type;
    }

    public boolean isRunning() {
        return _isRunning;
    }
}
