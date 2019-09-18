package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class ExDuelUpdateUserInfo extends L2GameServerPacket {
    private final String _name;
    private final int obj_id;
    private final int class_id;
    private final int level;
    private final int curHp;
    private final int maxHp;
    private final int curMp;
    private final int maxMp;
    private final int curCp;
    private final int maxCp;

    public ExDuelUpdateUserInfo(final Player attacker) {
        _name = attacker.getName();
        obj_id = attacker.getObjectId();
        class_id = attacker.getClassId().getId();
        level = attacker.getLevel();
        curHp = (int) attacker.getCurrentHp();
        maxHp = attacker.getMaxHp();
        curMp = (int) attacker.getCurrentMp();
        maxMp = attacker.getMaxMp();
        curCp = (int) attacker.getCurrentCp();
        maxCp = attacker.getMaxCp();
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x4f);
        writeS(_name);
        writeD(obj_id);
        writeD(class_id);
        writeD(level);
        writeD(curHp);
        writeD(maxHp);
        writeD(curMp);
        writeD(maxMp);
        writeD(curCp);
        writeD(maxCp);
    }
}
