package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;

public class ChangeWaitType extends L2GameServerPacket {
    private final int _objectId;
    private final int _moveType;
    private final int _x;
    private final int _y;
    private final int _z;

    public ChangeWaitType(final Creature cha, final int newMoveType) {
        _objectId = cha.getObjectId();
        _moveType = newMoveType;
        _x = cha.getX();
        _y = cha.getY();
        _z = cha.getZ();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x2f);
        writeD(_objectId);
        writeD(_moveType);
        writeD(_x);
        writeD(_y);
        writeD(_z);
    }

    //todo use this enum
    public enum MoveType {
        WT_SITTING(0),
        WT_STANDING(1),
        WT_START_FAKEDEATH(2),
        WT_STOP_FAKEDEATH(3);

        private final int type;

        MoveType(final int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
}
