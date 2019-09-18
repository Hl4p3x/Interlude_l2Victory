package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.SubUnit;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PledgeShowMemberListDelete;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PledgeShowMemberListDeleteAll;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

public class RequestWithdrawalPledge extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (activeChar.getClanId() == 0) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isInCombat()) {
            activeChar.sendPacket(Msg.ONE_CANNOT_LEAVE_ONES_CLAN_DURING_COMBAT);
            return;
        }
        final Clan clan = activeChar.getClan();
        if (clan == null) {
            return;
        }
        final UnitMember member = clan.getAnyMember(activeChar.getObjectId());
        if (member == null) {
            activeChar.sendActionFailed();
            return;
        }
        final SubUnit mainUnit = clan.getSubUnit(0);
        if (member.isClanLeader() || mainUnit.getNextLeaderObjectId() == member.getObjectId()) {
            activeChar.sendPacket(SystemMsg.A_CLAN_LEADER_CANNOT_WITHDRAW_FROM_THEIR_OWN_CLAN);
            return;
        }
        activeChar.removeEventsByClass(SiegeEvent.class);
        final int subUnitType = activeChar.getPledgeType();
        clan.removeClanMember(subUnitType, activeChar.getObjectId());
        clan.broadcastToOnlineMembers(new SystemMessage(SystemMsg.S1_HAS_WITHDRAWN_FROM_THE_CLAN).addString(activeChar.getName()), new PledgeShowMemberListDelete(activeChar.getName()));
        if (subUnitType == -1) {
            activeChar.setLvlJoinedAcademy(0);
        }
        activeChar.setClan(null);
        if (!activeChar.isNoble()) {
            activeChar.setTitle("");
        }
        activeChar.setLeaveClanCurTime();
        activeChar.broadcastCharInfo();
        activeChar.sendPacket(SystemMsg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN, PledgeShowMemberListDeleteAll.STATIC);
    }
}
