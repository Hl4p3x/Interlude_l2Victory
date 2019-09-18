package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;

public class MagicSkillLaunched extends L2GameServerPacket {
    private final int _casterId;
    private final int _skillId;
    private final int _skillLevel;
    private final int _casterX;
    private final int _casterY;
    private final int[] _targets;

    public MagicSkillLaunched(final Creature caster, final Skill skill, final int... target) {
        _casterId = caster.getObjectId();
        _casterX = caster.getX();
        _casterY = caster.getY();
        _skillId = skill.getDisplayId();
        _skillLevel = skill.getDisplayLevel();
        _targets = target;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x76);
        writeD(_casterId);
        writeD(_skillId);
        writeD(_skillLevel);
        writeD(_targets.length);
        for (int ints : _targets) {
            writeD(ints);
        }
    }

    @Override
    public L2GameServerPacket packet(final Player player) {
        if (player == null || player.isInObserverMode()) {
            return super.packet(player);
        }
        if (player.buffAnimRange() < 0) {
            return null;
        }
        if (player.buffAnimRange() == 0) {
            return (_casterId == player.getObjectId()) ? super.packet(player) : null;
        }
        return (player.getDistance(_casterX, _casterY) < player.buffAnimRange()) ? super.packet(player) : null;
    }
}
