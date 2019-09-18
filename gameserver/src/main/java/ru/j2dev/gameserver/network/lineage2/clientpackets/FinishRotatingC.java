package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.FinishRotating;

public class FinishRotatingC extends L2GameClientPacket {
    private int _degree;
    private int _unknown;

    @Override
    protected void readImpl() {
        _degree = readD();
        _unknown = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        activeChar.broadcastPacket(new FinishRotating(activeChar, _degree, 0));
    }
}
