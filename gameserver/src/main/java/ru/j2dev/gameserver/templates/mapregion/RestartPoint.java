package ru.j2dev.gameserver.templates.mapregion;

import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public class RestartPoint {
    private final String _name;
    private final int _bbs;
    private final int _msgId;
    private final List<Location> _restartPoints;
    private final List<Location> _PKrestartPoints;

    public RestartPoint(final String name, final int bbs, final int msgId, final List<Location> restartPoints, final List<Location> PKrestartPoints) {
        _name = name;
        _bbs = bbs;
        _msgId = msgId;
        _restartPoints = restartPoints;
        _PKrestartPoints = PKrestartPoints;
    }

    public String getName() {
        return _name;
    }

    public int getBbs() {
        return _bbs;
    }

    public int getMsgId() {
        return _msgId;
    }

    public List<Location> getRestartPoints() {
        return _restartPoints;
    }

    public List<Location> getPKrestartPoints() {
        return _PKrestartPoints;
    }
}
