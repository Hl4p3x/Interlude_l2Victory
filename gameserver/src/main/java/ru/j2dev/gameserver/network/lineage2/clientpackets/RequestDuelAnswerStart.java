package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.data.xml.holder.EventHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.model.entity.events.EventType;
import ru.j2dev.gameserver.model.entity.events.impl.DuelEvent;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;

public class RequestDuelAnswerStart extends L2GameClientPacket {
    private int _response;
    private int _duelType;

    @Override
    protected void readImpl() {
        _duelType = readD();
        readD();
        _response = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Request request = activeChar.getRequest();
        if (request == null || !request.isTypeOf(L2RequestType.DUEL)) {
            return;
        }
        if (!request.isInProgress()) {
            request.cancel();
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isActionsDisabled()) {
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
        if (_duelType != request.getInteger("duelType")) {
            request.cancel();
            activeChar.sendActionFailed();
            return;
        }
        final DuelEvent duelEvent = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, _duelType);
        switch (_response) {
            case 0: {
                request.cancel();
                if (_duelType == 1) {
                    requestor.sendPacket(SystemMsg.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
                    break;
                }
                requestor.sendPacket((new SystemMessage2(SystemMsg.C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_PARTY_DUEL)).addName(activeChar));
                break;
            }
            case -1: {
                request.cancel();
                requestor.sendPacket((new SystemMessage2(SystemMsg.C1_IS_SET_TO_REFUSE_DUEL_REQUESTS_AND_CANNOT_RECEIVE_A_DUEL_REQUEST)).addName(activeChar));
                break;
            }
            case 1: {
                if (!duelEvent.canDuel(requestor, activeChar, false)) {
                    request.cancel();
                    return;
                }
                SystemMessage2 msg1;
                SystemMessage2 msg2;
                if (_duelType == 1) {
                    msg1 = new SystemMessage2(SystemMsg.YOU_HAVE_ACCEPTED_C1S_CHALLENGE_TO_A_PARTY_DUEL);
                    msg2 = new SystemMessage2(SystemMsg.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY);
                } else {
                    msg1 = new SystemMessage2(SystemMsg.YOU_HAVE_ACCEPTED_C1S_CHALLENGE_A_DUEL);
                    msg2 = new SystemMessage2(SystemMsg.C1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL);
                }
                activeChar.sendPacket((msg1).addName(requestor));
                requestor.sendPacket((msg2).addName(activeChar));
                try {
                    duelEvent.createDuel(requestor, activeChar);
                } finally {
                    request.done();
                }
                break;
            }
        }
    }
}
