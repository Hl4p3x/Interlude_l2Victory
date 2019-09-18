package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.manager.PetitionManager;
import ru.j2dev.gameserver.model.Player;

public final class RequestPetition extends L2GameClientPacket {
    private String _content;
    private int _type;

    @Override
    protected void readImpl() {
        _content = readS();
        _type = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        PetitionManager.getInstance().handle(player, _type, _content);
    }
}
