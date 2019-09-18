package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.lineage2.GameClient;

public class RequestReload extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final GameClient client = getClient();
        final Player player = client.getActiveChar();
        if (player == null) {
            return;
        }
        final long now = System.currentTimeMillis();
        if (now - client.getLastIncomePacketTimeStamp(RequestReload.class) < Config.RELOAD_PACKET_DELAY) {
            player.sendActionFailed();
            return;
        }
        client.setLastIncomePacketTimeStamp(RequestReload.class, now);
        player.sendUserInfo(true);
        World.showObjectsToPlayer(player);
    }
}
