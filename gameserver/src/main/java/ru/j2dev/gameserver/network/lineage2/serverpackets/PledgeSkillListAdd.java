package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class PledgeSkillListAdd extends L2GameServerPacket {
    private final int _skillId;
    private final int _skillLevel;

    public PledgeSkillListAdd(final int skillId, final int skillLevel) {
        _skillId = skillId;
        _skillLevel = skillLevel;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x3a);
        writeD(_skillId);
        writeD(_skillLevel);
    }
}
