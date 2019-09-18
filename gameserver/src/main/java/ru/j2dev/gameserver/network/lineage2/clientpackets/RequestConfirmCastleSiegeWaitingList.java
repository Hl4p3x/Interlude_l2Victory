package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.dao.SiegeClanDAO;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.CastleSiegeEvent;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CastleSiegeDefenderList;

public class RequestConfirmCastleSiegeWaitingList extends L2GameClientPacket {
    private boolean _approved;
    private int _unitId;
    private int _clanId;

    @Override
    protected void readImpl() {
        _unitId = readD();
        _clanId = readD();
        _approved = (readD() == 1);
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        if (player.getClan() == null) {
            return;
        }
        final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _unitId);
        if (castle == null || player.getClan().getCastle() != castle.getId()) {
            player.sendActionFailed();
            return;
        }
        final CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
        SiegeClanObject siegeClan = siegeEvent.getSiegeClan("defenders_waiting", _clanId);
        if (siegeClan == null) {
            siegeClan = siegeEvent.getSiegeClan("defenders", _clanId);
        }
        if (siegeClan == null) {
            return;
        }
        if ((player.getClanPrivileges() & 0x20000) != 0x20000) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_CASTLE_DEFENDER_LIST);
            return;
        }
        if (siegeEvent.isRegistrationOver()) {
            player.sendPacket(SystemMsg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATIONS_CANNOT_BE_ACCEPTED_OR_REJECTED);
            return;
        }
        final int allSize = siegeEvent.getObjects("defenders").size();
        if (allSize >= 20) {
            player.sendPacket(SystemMsg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_DEFENDER_SIDE);
            return;
        }
        siegeEvent.removeObject(siegeClan.getType(), siegeClan);
        if (_approved) {
            siegeClan.setType("defenders");
        } else {
            siegeClan.setType("defenders_refused");
        }
        siegeEvent.addObject(siegeClan.getType(), siegeClan);
        SiegeClanDAO.getInstance().update(castle, siegeClan);
        player.sendPacket(new CastleSiegeDefenderList(castle));
    }
}
