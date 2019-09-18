package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowAgitInfo;

public class RequestAllAgitInfo extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        getClient().getActiveChar().sendPacket(new ExShowAgitInfo());
    }
}
