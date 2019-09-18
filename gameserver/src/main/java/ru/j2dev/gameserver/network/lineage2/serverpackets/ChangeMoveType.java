package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;

public class ChangeMoveType extends L2GameServerPacket {
    public static int WALK;
    public static int RUN = 1;

    private int _chaId;
    private boolean _running;
    private GroundPosition _position;

    public ChangeMoveType(final Creature cha) {
        _chaId = cha.getObjectId();
        _running = cha.isRunning();
    }

    public ChangeMoveType(final Creature cha, final GroundPosition position) {
        _chaId = cha.getObjectId();
        _running = cha.isRunning();
        _position = position;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x2e);
        writeD(_chaId);
        writeD(_running ? RUN : WALK);
        writeC(_position.ordinal()); //todo Ground Position fix
    }

    public enum GroundPosition {
        none(0xffffffff),
        earth(0x00),
        underwater(0x01),
        air(0x02),
        hover(0x03);

        private final int type;

        GroundPosition(final int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
}
