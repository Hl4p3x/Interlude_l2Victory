package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Effect;

public class ShortBuffStatusUpdate extends L2GameServerPacket {
    final int _skillId;
    final int _skillLevel;
    final int _skillDuration;

    public ShortBuffStatusUpdate(final Effect effect) {
        _skillId = effect.getSkill().getDisplayId();
        _skillLevel = effect.getSkill().getDisplayLevel();
        _skillDuration = effect.getTimeLeft();
    }

    public ShortBuffStatusUpdate() {
        _skillId = 0;
        _skillLevel = 0;
        _skillDuration = 0;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xf4);
        writeD(_skillId);
        writeD(_skillLevel);
        writeD(_skillDuration);
    }
}
