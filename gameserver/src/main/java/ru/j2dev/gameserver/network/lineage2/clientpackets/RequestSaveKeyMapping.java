package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExUISetting;

public class RequestSaveKeyMapping extends L2GameClientPacket {
    private byte[] _data;

    @Override
    protected void readImpl() {
        final int length = readD();
        if (length > _buf.remaining() || length > 32767 || length < 0) {
            _data = null;
            return;
        }
        readB(_data = new byte[length]);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || _data == null) {
            return;
        }
        activeChar.setKeyBindings(_data);
        activeChar.sendPacket(new ExUISetting(activeChar));
    }
}
