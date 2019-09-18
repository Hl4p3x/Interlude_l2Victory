package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CharacterSelectionInfo;

public class RequestCharacterRestore extends L2GameClientPacket {
    private int _charSlot;

    @Override
    protected void readImpl() {
        _charSlot = readD();
    }

    @Override
    protected void runImpl() {
        final GameClient client = getClient();
        try {
            client.markRestoredChar(_charSlot);
        } catch (Exception ex) {
            LOGGER.warn(ex.getLocalizedMessage());
        }
        final CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
        sendPacket(cl);
        client.setCharSelection(cl.getCharInfo());
    }
}
