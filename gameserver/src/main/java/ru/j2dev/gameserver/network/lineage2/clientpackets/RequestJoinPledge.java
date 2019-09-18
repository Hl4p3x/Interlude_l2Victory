package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.AskJoinPledge;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

public class RequestJoinPledge extends L2GameClientPacket {
    private int _objectId;
    private int _pledgeType;

    @Override
    protected void readImpl() {
        _objectId = readD();
        _pledgeType = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || activeChar.getClan() == null) {
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
        final Clan clan = activeChar.getClan();
        if (clan.isPlacedForDisband()) {
            activeChar.sendPacket(SystemMsg.DISPERSION_HAS_ALREADY_BEEN_REQUESTED);
            return;
        }
        if (!clan.canInvite()) {
            activeChar.sendPacket(Msg.AFTER_A_CLAN_MEMBER_IS_DISMISSED_FROM_A_CLAN_THE_CLAN_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_ACCEPTING_A_NEW_MEMBER);
            return;
        }
        if (_objectId == activeChar.getObjectId()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN);
            return;
        }
        if ((activeChar.getClanPrivileges() & 0x2) != 0x2) {
            activeChar.sendPacket(Msg.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS);
            return;
        }
        final GameObject object = activeChar.getVisibleObject(_objectId);
        if (object == null || !object.isPlayer()) {
            activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            return;
        }
        final Player member = (Player) object;
        if (member.getClan() == activeChar.getClan()) {
            activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            return;
        }
        if (!member.getPlayerAccess().CanJoinClan) {
            activeChar.sendPacket(new SystemMessage(760).addName(member));
            return;
        }
        if (member.getClan() != null) {
            activeChar.sendPacket(new SystemMessage(10).addName(member));
            return;
        }
        if (member.isBusy()) {
            activeChar.sendPacket(new SystemMessage(153).addName(member));
            return;
        }
        if (_pledgeType == -1 && (member.getLevel() > 40 || member.getClassId().getLevel() > 2)) {
            activeChar.sendPacket(Msg.TO_JOIN_A_CLAN_ACADEMY_CHARACTERS_MUST_BE_LEVEL_40_OR_BELOW_NOT_BELONG_ANOTHER_CLAN_AND_NOT_YET_COMPLETED_THEIR_2ND_CLASS_TRANSFER);
            return;
        }
        if (clan.getUnitMembersSize(_pledgeType) >= clan.getSubPledgeLimit(_pledgeType)) {
            if (_pledgeType == 0) {
                activeChar.sendPacket(new SystemMessage(1835).addString(clan.getName()));
            } else {
                activeChar.sendPacket(Msg.THE_ACADEMY_ROYAL_GUARD_ORDER_OF_KNIGHTS_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS_AT_THIS_TIME);
            }
            return;
        }
        final Request request = new Request(L2RequestType.CLAN, activeChar, member).setTimeout(10000L);
        request.set("pledgeType", _pledgeType);
        member.sendPacket(new AskJoinPledge(activeChar.getObjectId(), activeChar.getClan().getName()));
    }
}
