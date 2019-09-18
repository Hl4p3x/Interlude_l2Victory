package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;

public class MagicSkillCanceled extends L2GameServerPacket {
    private final int _casterX;
    private final int _casterY;
    private final int _casterId;

    public MagicSkillCanceled(final Creature caster) {
        _casterId = caster.getObjectId();
        _casterX = caster.getX();
        _casterY = caster.getY();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x49);
        writeD(_casterId);
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
