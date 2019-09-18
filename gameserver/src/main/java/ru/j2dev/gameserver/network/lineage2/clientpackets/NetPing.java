package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.network.lineage2.GameClient;

public class NetPing extends L2GameClientPacket {
    public static final int MIN_CLIP_RANGE = 1433;
    public static final int MAX_CLIP_RANGE = 6144;
    private int _timestamp;
    private int _clippingRange;
    private int _fps;

    @Override
    protected void runImpl() {
        final GameClient client = getClient();
        if (client.getRevision() == 0) {
            client.closeNow(false);
        } else {
            client.onPing(_timestamp, _fps, Math.max(MIN_CLIP_RANGE, Math.min(_clippingRange, MAX_CLIP_RANGE)));
        }
    }

    @Override
    protected void readImpl() {
        _timestamp = readD();
        _fps = readD();
        _clippingRange = readD();
    }
}
