package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class ExOlympiadUserInfo extends L2GameServerPacket {
    private final int _side;
    private final int class_id;
    private final int curHp;
    private final int maxHp;
    private final int curCp;
    private final int maxCp;
    private final String _name;
    private int obj_id;

    public ExOlympiadUserInfo(final Player player, final int side) {
        obj_id = 0;
        _side = side;
        obj_id = player.getObjectId();
        class_id = player.getClassId().getId();
        _name = player.getName();
        curHp = (int) player.getCurrentHp();
        maxHp = player.getMaxHp();
        curCp = (int) player.getCurrentCp();
        maxCp = player.getMaxCp();
    }

    public ExOlympiadUserInfo(final Player player) {
        obj_id = 0;
        _side = player.getOlyParticipant().getSide();
        obj_id = player.getObjectId();
        class_id = player.getClassId().getId();
        _name = player.getName();
        curHp = (int) player.getCurrentHp();
        maxHp = player.getMaxHp();
        curCp = (int) player.getCurrentCp();
        maxCp = player.getMaxCp();
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x29);
        writeC(_side);
        writeD(obj_id);
        writeS(_name);
        writeD(class_id);
        writeD(curHp);
        writeD(maxHp);
        writeD(curCp);
        writeD(maxCp);
    }
}
