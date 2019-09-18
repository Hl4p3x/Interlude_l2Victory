package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.JoinParty;

public class RequestAnswerJoinParty extends L2GameClientPacket {
    private int _response;

    @Override
    protected void readImpl() {
        if (_buf.hasRemaining()) {
            _response = readD();
        } else {
            _response = 0;
        }
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Request request = activeChar.getRequest();
        if (request == null || !request.isTypeOf(L2RequestType.PARTY)) {
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
            activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
            activeChar.sendActionFailed();
            return;
        }
        if (requestor.getRequest() != request) {
            request.cancel();
            activeChar.sendActionFailed();
            return;
        }
        if (_response <= 0) {
            request.cancel();
            requestor.sendPacket(JoinParty.FAIL);
            return;
        }
        if (activeChar.isOlyParticipant()) {
            request.cancel();
            activeChar.sendPacket(SystemMsg.A_PARTY_CANNOT_BE_FORMED_IN_THIS_AREA);
            requestor.sendPacket(JoinParty.FAIL);
            return;
        }
        if (requestor.isOlyParticipant()) {
            request.cancel();
            requestor.sendPacket(JoinParty.FAIL);
            return;
        }
        Party party = requestor.getParty();
        if (party != null && party.getMemberCount() >= Config.ALT_MAX_PARTY_SIZE) {
            request.cancel();
            activeChar.sendPacket(SystemMsg.THE_PARTY_IS_FULL);
            requestor.sendPacket(SystemMsg.THE_PARTY_IS_FULL);
            requestor.sendPacket(JoinParty.FAIL);
            return;
        }
        final IStaticPacket problem = activeChar.canJoinParty(requestor);
        if (problem != null) {
            request.cancel();
            activeChar.sendPacket(problem, ActionFail.STATIC);
            requestor.sendPacket(JoinParty.FAIL);
            return;
        }
        if (party == null) {
            final int itemDistribution = request.getInteger("itemDistribution");
            requestor.setParty(party = new Party(requestor, itemDistribution));
        }
        try {
            activeChar.joinParty(party);
            requestor.sendPacket(JoinParty.SUCCESS);
        } finally {
            request.done();
        }
    }
}
