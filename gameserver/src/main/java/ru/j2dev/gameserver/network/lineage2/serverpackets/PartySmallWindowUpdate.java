package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class PartySmallWindowUpdate extends L2GameServerPacket {
    private final int obj_id;
    private final int class_id;
    private final int level;
    private final int curCp;
    private final int maxCp;
    private final int curHp;
    private final int maxHp;
    private final int curMp;
    private final int maxMp;
    private final String obj_name;

    public PartySmallWindowUpdate(final Player member) {
        obj_id = member.getObjectId();
        obj_name = member.getName();
        curCp = (int) member.getCurrentCp();
        maxCp = member.getMaxCp();
        curHp = (int) member.getCurrentHp();
        maxHp = member.getMaxHp();
        curMp = (int) member.getCurrentMp();
        maxMp = member.getMaxMp();
        level = member.getLevel();
        class_id = member.getClassId().getId();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x52);
        writeD(obj_id);
        writeS(obj_name);
        writeD(curCp);
        writeD(maxCp);
        writeD(curHp);
        writeD(maxHp);
        writeD(curMp);
        writeD(maxMp);
        writeD(level);
        writeD(class_id);
    }
}
