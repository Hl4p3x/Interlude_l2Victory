package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

public class VoteSociality extends L2GameClientPacket {
    private int _targetObjectId;

    @Override
    protected void readImpl() {
        _targetObjectId = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (activeChar.getTarget() == null) {
            activeChar.sendPacket(Msg.SELECT_TARGET);
            return;
        }
        if (!activeChar.getTarget().isPlayer() || activeChar.getTarget().getObjectId() != _targetObjectId) {
            activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            return;
        }
        final Player target = activeChar.getTarget().getPlayer();
        if (target == activeChar) {
            activeChar.sendPacket(Msg.SELECT_TARGET);
            return;
        }
        if (activeChar.getLevel() < 10) {
            activeChar.sendPacket(Msg.ONLY_LEVEL_SUP_10_CAN_RECOMMEND);
            return;
        }
        if (activeChar.getGivableRec() <= 0) {
            activeChar.sendPacket(Msg.NO_MORE_RECOMMENDATIONS_TO_HAVE);
            return;
        }
        if (activeChar.isRecommended(target)) {
            activeChar.sendPacket(Msg.THAT_CHARACTER_HAS_ALREADY_BEEN_RECOMMENDED);
            return;
        }
        if (target.getReceivedRec() >= 255) {
            activeChar.sendPacket(Msg.YOU_NO_LONGER_RECIVE_A_RECOMMENDATION);
            return;
        }
        activeChar.giveRecommendation(target);
        activeChar.sendPacket(new SystemMessage(830).addName(target).addNumber(activeChar.getGivableRec()));
        target.sendPacket(new SystemMessage(831).addName(activeChar));
        activeChar.sendUserInfo(false);
        target.broadcastUserInfo(true);
    }
}
