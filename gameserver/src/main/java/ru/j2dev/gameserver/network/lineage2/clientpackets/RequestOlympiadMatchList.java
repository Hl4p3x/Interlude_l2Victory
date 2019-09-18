package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadGameManager;

public class RequestOlympiadMatchList extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        OlympiadGameManager.getInstance().showCompetitionList(player);
    }
}
