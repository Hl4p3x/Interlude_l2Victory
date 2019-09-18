package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.network.lineage2.serverpackets.ExSendManorList;

public class RequestManorList extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        sendPacket(new ExSendManorList());
    }
}
