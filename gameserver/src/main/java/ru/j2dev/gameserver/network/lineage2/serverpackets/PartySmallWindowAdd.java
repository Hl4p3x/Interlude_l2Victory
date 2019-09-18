package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PartySmallWindowAll.PartySmallWindowMemberInfo;

public class PartySmallWindowAdd extends L2GameServerPacket {
    private final PartySmallWindowMemberInfo member;
    private final int objectId;

    public PartySmallWindowAdd(final Player player, final Player member) {
        objectId = player.getObjectId();
        this.member = new PartySmallWindowMemberInfo(member);
    }

    @Override
    protected final void writeImpl() {
        writeC(0x4f);
        writeD(objectId);
        writeD(0);
        writeD(member._id);
        writeS(member._name);
        writeD(member.curCp);
        writeD(member.maxCp);
        writeD(member.curHp);
        writeD(member.maxHp);
        writeD(member.curMp);
        writeD(member.maxMp);
        writeD(member.level);
        writeD(member.class_id);
        writeD(0);
        writeD(member.race_id);
    }
}
