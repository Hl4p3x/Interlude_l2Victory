package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
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

public class RequestOustPledgeMember extends L2GameClientPacket {
    private String _target;

    @Override
    protected void readImpl() {
        _target = readS(Config.CNAME_MAXLEN);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || (activeChar.getClanPrivileges() & 0x40) != 0x40) {
            return;
        }
        final Clan clan = activeChar.getClan();
        final UnitMember member = clan.getAnyMember(_target);
        if (member == null) {
            activeChar.sendPacket(SystemMsg.THE_TARGET_MUST_BE_A_CLAN_MEMBER);
            return;
        }
        final SubUnit mainUnit = clan.getSubUnit(0);
        final Player memberPlayer = member.getPlayer();
        if (member.isOnline() && member.getPlayer().isInCombat()) {
            activeChar.sendPacket(SystemMsg.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT);
            return;
        }
        if (member.isClanLeader() || mainUnit.getNextLeaderObjectId() == member.getObjectId()) {
            activeChar.sendPacket(SystemMsg.A_CLAN_LEADER_CANNOT_WITHDRAW_FROM_THEIR_OWN_CLAN);
            return;
        }
        final int subUnitType = member.getPledgeType();
        clan.removeClanMember(subUnitType, member.getObjectId());
        clan.broadcastToOnlineMembers(new SystemMessage(191).addString(_target), new PledgeShowMemberListDelete(_target));
        if (subUnitType != Clan.SUBUNIT_ACADEMY) {
            clan.setExpelledMember();
        }
        if (memberPlayer == null) {
            return;
        }
        memberPlayer.removeEventsByClass(SiegeEvent.class);
        if (subUnitType == -1) {
            memberPlayer.setLvlJoinedAcademy(0);
        }
        memberPlayer.setClan(null);
        if (!memberPlayer.isNoble()) {
            memberPlayer.setTitle("");
        }
        memberPlayer.setLeaveClanCurTime();
        memberPlayer.broadcastCharInfo();
        memberPlayer.broadcastRelationChanged();
        memberPlayer.store(true);
        memberPlayer.sendPacket(Msg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS, PledgeShowMemberListDeleteAll.STATIC);
    }
}
