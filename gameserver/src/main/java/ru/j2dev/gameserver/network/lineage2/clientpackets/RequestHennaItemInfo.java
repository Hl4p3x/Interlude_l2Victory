package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.data.xml.holder.HennaHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.HennaItemInfo;
import ru.j2dev.gameserver.templates.Henna;

public class RequestHennaItemInfo extends L2GameClientPacket {
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
        final Henna henna = HennaHolder.getInstance().getHenna(_symbolId);
        if (henna != null) {
            player.sendPacket(new HennaItemInfo(henna, player));
        }
    }
}
