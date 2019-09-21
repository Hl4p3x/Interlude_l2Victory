package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.protection.CatsGuard;

public class ReplyGameGuardQuery extends L2GameClientPacket {
    private int[] _reply = new int[4];

    @Override
    protected void readImpl()
    {
        if (CatsGuard.getInstance().isEnabled() && getClient().getHwid() == null)
        {
            _reply[0] = readD();
            _reply[1] = readD();
            _reply[2] = readD();
            _reply[3] = readD();
        }
        else
        {
            byte[] b = new byte[getByteBuffer().remaining()];
            readB(b);
        }
    }

    @Override
    protected void runImpl()
    {
        if (CatsGuard.getInstance().isEnabled())
        {
            CatsGuard.getInstance().initSession(getClient(), _reply);
        }
    }
}
