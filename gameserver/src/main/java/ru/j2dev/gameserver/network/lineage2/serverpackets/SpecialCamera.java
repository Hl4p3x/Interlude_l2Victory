package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class SpecialCamera extends L2GameServerPacket {
    private final int _turn;
    private final int _rise;
    private final int _widescreen;
    private final int _unknown;
    private final int _id;
    private final int _dist;
    private final int _yaw;
    private final int _pitch;
    private final int _time;
    private final int _duration;

    public SpecialCamera(final int id, final int dist, final int yaw, final int pitch, final int time, final int duration) {
        _id = id;
        _dist = dist;
        _yaw = yaw;
        _pitch = pitch;
        _time = time;
        _duration = duration;
        _turn = 0;
        _rise = 0;
        _widescreen = 0;
        _unknown = 0;
    }

    public SpecialCamera(final int id, final int dist, final int yaw, final int pitch, final int time, final int duration, final int turn, final int rise, final int widescreen, final int unk) {
        _id = id;
        _dist = dist;
        _yaw = yaw;
        _pitch = pitch;
        _time = time;
        _duration = duration;
        _turn = turn;
        _rise = rise;
        _widescreen = widescreen;
        _unknown = unk;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xc7);
        writeD(_id);
        writeD(_dist);
        writeD(_yaw);
        writeD(_pitch);
        writeD(_time);
        writeD(_duration);
        writeD(_turn);
        writeD(_rise);
        writeD(_widescreen);
        writeD(_unknown);
    }
}
