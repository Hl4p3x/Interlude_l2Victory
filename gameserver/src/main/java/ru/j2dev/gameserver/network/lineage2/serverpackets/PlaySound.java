package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.utils.Location;

public class PlaySound extends L2GameServerPacket {
    public static final L2GameServerPacket SIEGE_VICTORY = new PlaySound("Siege_Victory");
    public static final L2GameServerPacket B04_S01 = new PlaySound("B04_S01");
    public static final L2GameServerPacket HB01 = new PlaySound(Type.MUSIC, "HB01", 0, 0, 0, 0, 0);

    private Type _type;
    private String _soundFile;
    private int _hasCenterObject;
    private int _objectId;
    private int _x;
    private int _y;
    private int _z;

    public PlaySound(final String soundFile) {
        this(Type.SOUND, soundFile, 0, 0, 0, 0, 0);
    }

    public PlaySound(final Type type, final String soundFile, final int c, final int objectId, final Location loc) {
        this(type, soundFile, c, objectId, (loc == null) ? 0 : loc.x, (loc == null) ? 0 : loc.y, (loc == null) ? 0 : loc.z);
    }

    public PlaySound(final Type type, final String soundFile, final int c, final int objectId, final int x, final int y, final int z) {
        _type = type;
        _soundFile = soundFile;
        _hasCenterObject = c;
        _objectId = objectId;
        _x = x;
        _y = y;
        _z = z;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x98);
        writeD(_type.ordinal());
        writeS(_soundFile);
        writeD(_hasCenterObject);
        writeD(_objectId);
        writeD(_x);
        writeD(_y);
        writeD(_z);
    }

    public enum Type {
        SOUND,
        MUSIC,
        VOICE
    }
}
