package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.model.pledge.Alliance;

public class RequestAnswerJoinAlly extends L2GameClientPacket {
    private int _response;

    @Override
    protected void readImpl() {
        _response = ((_buf.remaining() >= 4) ? readD() : 0);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Request request = activeChar.getRequest();
        if (request == null || !request.isTypeOf(L2RequestType.ALLY)) {
            return;
        }
        if (!request.isInProgress()) {
            request.cancel();
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isOutOfControl()) {
            request.cancel();
            activeChar.sendActionFailed();
            return;
        }
        final Player requestor = request.getRequestor();
        if (requestor == null) {
            request.cancel();
            activeChar.sendPacket(Msg.THAT_PLAYER_IS_NOT_ONLINE);
            activeChar.sendActionFailed();
            return;
        }
        if (requestor.getRequest() != request) {
            request.cancel();
            activeChar.sendActionFailed();
            return;
        }
        if (requestor.getAlliance() == null) {
            request.cancel();
            activeChar.sendActionFailed();
            return;
        }
        if (_response == 0) {
            request.cancel();
            requestor.sendPacket(Msg.YOU_HAVE_FAILED_TO_INVITE_A_CLAN_INTO_THE_ALLIANCE);
            return;
        }
        try {
            final Alliance ally = requestor.getAlliance();
            activeChar.sendPacket(Msg.YOU_HAVE_ACCEPTED_THE_ALLIANCE);
            activeChar.getClan().setAllyId(requestor.getAllyId());
            activeChar.getClan().updateClanInDB();
            ally.addAllyMember(activeChar.getClan(), true);
            ally.broadcastAllyStatus();
        } finally {
            request.done();
        }
    }
}
