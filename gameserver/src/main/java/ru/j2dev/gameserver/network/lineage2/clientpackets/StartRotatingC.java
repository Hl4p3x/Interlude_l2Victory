package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.StartRotating;

public class StartRotatingC extends L2GameClientPacket {
    private int _degree;
    private int _side;

    @Override
    protected void readImpl() {
        _degree = readD();
        _side = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        activeChar.setHeading(_degree);
        activeChar.broadcastPacket(new StartRotating(activeChar, _degree, _side, 0));
    }
}
