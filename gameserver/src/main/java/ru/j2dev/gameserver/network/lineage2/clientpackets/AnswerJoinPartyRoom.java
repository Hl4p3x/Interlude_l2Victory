package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.model.matching.MatchingRoom;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;

public class AnswerJoinPartyRoom extends L2GameClientPacket {
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
        if (request == null || !request.isTypeOf(L2RequestType.PARTY_ROOM)) {
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
        if (_response == 0) {
            request.cancel();
            requestor.sendPacket(SystemMsg.THE_PLAYER_DECLINED_TO_JOIN_YOUR_PARTY);
            return;
        }
        if (activeChar.getMatchingRoom() != null) {
            request.cancel();
            activeChar.sendActionFailed();
            return;
        }
        try {
            final MatchingRoom room = requestor.getMatchingRoom();
            if (room == null || room.getType() != MatchingRoom.PARTY_MATCHING) {
                return;
            }
            room.addMember(activeChar);
        } finally {
            request.done();
        }
    }
}
