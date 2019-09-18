package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.network.lineage2.serverpackets.CharacterSelectionInfo;

public class GotoLobby extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final CharacterSelectionInfo cl = new CharacterSelectionInfo(getClient().getLogin(), getClient().getSessionKey().playOkID1);
        sendPacket(cl);
    }
}
