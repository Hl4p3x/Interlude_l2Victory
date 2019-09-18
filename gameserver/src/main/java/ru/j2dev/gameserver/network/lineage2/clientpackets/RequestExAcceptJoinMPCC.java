package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.CommandChannel;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;

public class RequestExAcceptJoinMPCC extends L2GameClientPacket {
    private int _response;
    private int _unk;

    @Override
    protected void readImpl() {
        _response = (_buf.hasRemaining() ? readD() : 0);
        _unk = (_buf.hasRemaining() ? readD() : 0);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Request request = activeChar.getRequest();
        if (request == null || !request.isTypeOf(L2RequestType.CHANNEL)) {
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
        if (_response == 0) {
            request.cancel();
            requestor.sendPacket(new SystemMessage(1680).addString(activeChar.getName()));
            return;
        }
        if (!requestor.isInParty() || !activeChar.isInParty() || activeChar.getParty().isInCommandChannel()) {
            request.cancel();
            requestor.sendPacket(Msg.NO_USER_HAS_BEEN_INVITED_TO_THE_COMMAND_CHANNEL);
            return;
        }
        if (activeChar.isTeleporting()) {
            request.cancel();
            activeChar.sendPacket(Msg.YOU_CANNOT_JOIN_A_COMMAND_CHANNEL_WHILE_TELEPORTING);
            requestor.sendPacket(Msg.NO_USER_HAS_BEEN_INVITED_TO_THE_COMMAND_CHANNEL);
            return;
        }
        try {
            if (requestor.getParty().isInCommandChannel()) {
                requestor.getParty().getCommandChannel().addParty(activeChar.getParty());
            } else if (CommandChannel.checkAuthority(requestor)) {
                final boolean haveSkill = requestor.getSkillLevel(391) > 0;
                boolean haveItem = false;
                if (!haveSkill && (haveItem = requestor.getInventory().destroyItemByItemId(8871, 1L))) {
                    requestor.sendPacket(SystemMessage2.removeItems(8871, 1L));
                }
                if (!haveSkill && !haveItem) {
                    return;
                }
                final CommandChannel channel = new CommandChannel(requestor);
                requestor.sendPacket(Msg.THE_COMMAND_CHANNEL_HAS_BEEN_FORMED);
                channel.addParty(activeChar.getParty());
            }
        } finally {
            request.done();
        }
    }
}
