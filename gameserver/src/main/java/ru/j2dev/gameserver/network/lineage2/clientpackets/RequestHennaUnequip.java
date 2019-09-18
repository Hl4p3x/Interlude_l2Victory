package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.templates.Henna;

public class RequestHennaUnequip extends L2GameClientPacket {
    private int _symbolId;

    @Override
    protected void readImpl() {
        _symbolId = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        for (int i = 1; i <= 3; ++i) {
            final Henna henna = player.getHenna(i);
            if (henna != null) {
                if (henna.getSymbolId() == _symbolId) {
                    final long price = henna.getPrice() / 5L;
                    if (player.getAdena() < price) {
                        player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                        break;
                    }
                    player.reduceAdena(price);
                    player.removeHenna(i);
                    player.sendPacket(SystemMsg.THE_SYMBOL_HAS_BEEN_DELETED);
                    break;
                }
            }
        }
    }
}
