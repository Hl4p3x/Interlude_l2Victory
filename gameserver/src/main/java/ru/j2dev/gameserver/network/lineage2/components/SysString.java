package ru.j2dev.gameserver.network.lineage2.components;

public enum SysString {
    PASSENGER_BOAT_INFO(801),
    PREVIOUS(1037),
    NEXT(1038);

    private static final SysString[] VALUES = values();

    private final int _id;

    SysString(final int i) {
        _id = i;
    }

    public static SysString valueOf2(final String id) {
        for (final SysString m : VALUES) {
            if (m.name().equals(id)) {
                return m;
            }
        }
        return null;
    }

    public static SysString valueOf(final int id) {
        for (final SysString m : VALUES) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;
    }

    public int getId() {
        return _id;
    }
}
