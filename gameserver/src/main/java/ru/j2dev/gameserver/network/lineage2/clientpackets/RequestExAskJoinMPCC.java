package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExAskJoinMPCC;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

public class RequestExAskJoinMPCC extends L2GameClientPacket {
    private String _name;

    @Override
    protected void readImpl() {
        _name = readS(16);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (activeChar.isOutOfControl()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isProcessingRequest()) {
            activeChar.sendPacket(Msg.WAITING_FOR_ANOTHER_REPLY);
            return;
        }
        if (!activeChar.isInParty()) {
            activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
            return;
        }
        Player target = World.getPlayer(_name);
        if (target == null) {
            activeChar.sendPacket(Msg.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
            return;
        }
        if (activeChar == target || !target.isInParty() || activeChar.getParty() == target.getParty()) {
            activeChar.sendPacket(Msg.YOU_HAVE_INVITED_WRONG_TARGET);
            return;
        }
        if (target.isInParty() && !target.getParty().isLeader(target)) {
            target = target.getParty().getPartyLeader();
        }
        if (target == null) {
            activeChar.sendPacket(Msg.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
            return;
        }
        if (target.getParty().isInCommandChannel()) {
            activeChar.sendPacket(new SystemMessage(1594).addString(target.getName()));
            return;
        }
        if (target.isBusy()) {
            activeChar.sendPacket(new SystemMessage(153).addString(target.getName()));
            return;
        }
        final Party activeParty = activeChar.getParty();
        if (activeParty.isInCommandChannel()) {
            if (activeParty.getCommandChannel().getChannelLeader() != activeChar) {
                activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
                return;
            }
            sendInvite(activeChar, target);
        } else if (CommandChannel.checkAuthority(activeChar)) {
            sendInvite(activeChar, target);
        }
    }

    private void sendInvite(final Player requestor, final Player target) {
        new Request(L2RequestType.CHANNEL, requestor, target).setTimeout(10000L);
        target.sendPacket(new ExAskJoinMPCC(requestor.getName()));
        requestor.sendMessage("You invited " + target.getName() + " to your Command Channel.");
    }
}
