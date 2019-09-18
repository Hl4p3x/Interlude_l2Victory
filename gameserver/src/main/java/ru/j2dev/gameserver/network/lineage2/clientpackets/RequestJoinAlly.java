package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.AskJoinAlliance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

public class RequestJoinAlly extends L2GameClientPacket {
    private int _objectId;

    @Override
    protected void readImpl() {
        _objectId = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || activeChar.getClan() == null || activeChar.getAlliance() == null) {
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
        if (activeChar.getAlliance().getMembersCount() >= Config.ALT_MAX_ALLY_SIZE) {
            activeChar.sendPacket(Msg.YOU_HAVE_FAILED_TO_INVITE_A_CLAN_INTO_THE_ALLIANCE);
            return;
        }
        final GameObject obj = activeChar.getVisibleObject(_objectId);
        if (obj == null || !obj.isPlayer() || obj == activeChar) {
            activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            return;
        }
        final Player target = (Player) obj;
        if (!activeChar.isAllyLeader()) {
            activeChar.sendPacket(Msg.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY);
            return;
        }
        if (target.getAlliance() != null || activeChar.getAlliance().isMember(target.getClan().getClanId())) {
            final SystemMessage sm = new SystemMessage(691);
            sm.addString(target.getClan().getName());
            sm.addString(target.getAlliance().getAllyName());
            activeChar.sendPacket(sm);
            return;
        }
        if (!target.isClanLeader()) {
            activeChar.sendPacket(new SystemMessage(9).addString(target.getName()));
            return;
        }
        if (activeChar.isAtWarWith(target.getClanId()) > 0) {
            activeChar.sendPacket(Msg.YOU_MAY_NOT_ALLY_WITH_A_CLAN_YOU_ARE_AT_BATTLE_WITH);
            return;
        }
        if (!target.getClan().canJoinAlly()) {
            final SystemMessage sm = new SystemMessage(761);
            sm.addString(target.getClan().getName());
            activeChar.sendPacket(sm);
            return;
        }
        if (!activeChar.getAlliance().canInvite()) {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestJoinAlly.InvitePenalty", activeChar));
            return;
        }
        if (target.isBusy()) {
            activeChar.sendPacket(new SystemMessage(153).addString(target.getName()));
            return;
        }
        new Request(L2RequestType.ALLY, activeChar, target).setTimeout(10000L);
        target.sendPacket(new SystemMessage(527).addString(activeChar.getAlliance().getAllyName()).addName(activeChar));
        target.sendPacket(new AskJoinAlliance(activeChar.getObjectId(), activeChar.getName(), activeChar.getAlliance().getAllyName()));
    }
}
