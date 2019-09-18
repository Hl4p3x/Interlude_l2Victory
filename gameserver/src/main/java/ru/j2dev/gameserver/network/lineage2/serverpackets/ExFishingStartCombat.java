package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;

public class ExFishingStartCombat extends L2GameServerPacket {
    final int _time;
    final int _hp;
    final int _lureType;
    final int _deceptiveMode;
    final int _mode;
    private final int char_obj_id;

    public ExFishingStartCombat(final Creature character, final int time, final int hp, final int mode, final int lureType, final int deceptiveMode) {
        char_obj_id = character.getObjectId();
        _time = time;
        _hp = hp;
        _mode = mode;
        _lureType = lureType;
        _deceptiveMode = deceptiveMode;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x15);
        writeD(char_obj_id);
        writeD(_time);
        writeD(_hp);
        writeC(_mode);
        writeC(_lureType);
        writeC(_deceptiveMode);
    }
}
