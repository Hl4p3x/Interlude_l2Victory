package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.CastleSiegeEvent;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CastleSiegeInfo;

public class RequestSetCastleSiegeTime extends L2GameClientPacket {
    private int _id;
    private int _time;

    @Override
    protected void readImpl() {
        _id = readD();
        _time = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _id);
        if (castle == null) {
            return;
        }
        if (player.getClan().getCastle() != castle.getId()) {
            return;
        }
        if ((player.getClanPrivileges() & 0x20000) != 0x20000) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_SIEGE_TIME);
            return;
        }
        final CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
        siegeEvent.setNextSiegeTime(_time);
        player.sendPacket(new CastleSiegeInfo(castle, player));
    }
}
