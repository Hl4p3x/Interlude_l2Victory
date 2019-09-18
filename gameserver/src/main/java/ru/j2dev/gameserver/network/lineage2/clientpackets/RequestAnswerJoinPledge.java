package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.data.xml.holder.EventHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.SubUnit;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;

public class RequestAnswerJoinPledge extends L2GameClientPacket {
    private int _response;

    @Override
    protected void readImpl() {
        _response = (_buf.hasRemaining() ? readD() : 0);
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final Request request = player.getRequest();
        if (request == null || !request.isTypeOf(L2RequestType.CLAN)) {
            return;
        }
        if (!request.isInProgress()) {
            request.cancel();
            player.sendActionFailed();
            return;
        }
        if (player.isOutOfControl()) {
            request.cancel();
            player.sendActionFailed();
            return;
        }
        final Player requestor = request.getRequestor();
        if (requestor == null) {
            request.cancel();
            player.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
            player.sendActionFailed();
            return;
        }
        if (requestor.getRequest() != request) {
            request.cancel();
            player.sendActionFailed();
            return;
        }
        final Clan clan = requestor.getClan();
        if (clan == null) {
            request.cancel();
            player.sendActionFailed();
            return;
        }
        if (_response == 0) {
            request.cancel();
            requestor.sendPacket((new SystemMessage2(SystemMsg.S1_DECLINED_YOUR_CLAN_INVITATION)).addName(player));
            return;
        }
        if (!player.canJoinClan()) {
            request.cancel();
            player.sendPacket(SystemMsg.AFTER_LEAVING_OR_HAVING_BEEN_DISMISSED_FROM_A_CLAN_YOU_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_JOINING_ANOTHER_CLAN);
            return;
        }
        try {
            player.sendPacket(new JoinPledge(requestor.getClanId()));
            final int pledgeType = request.getInteger("pledgeType");
            final SubUnit subUnit = clan.getSubUnit(pledgeType);
            if (subUnit == null) {
                return;
            }
            final UnitMember member = new UnitMember(clan, player.getName(), player.getTitle(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), pledgeType, player.getPowerGrade(), player.getApprentice(), player.getSex(), -128);
            subUnit.addUnitMember(member);
            player.setPledgeType(pledgeType);
            player.setClan(clan);
            member.setPlayerInstance(player, false);
            if (pledgeType == -1) {
                player.setLvlJoinedAcademy(player.getLevel());
            }
            member.setPowerGrade(clan.getAffiliationRank(player.getPledgeType()));
            clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(member), player);
            clan.broadcastToOnlineMembers((new SystemMessage2(SystemMsg.S1_HAS_JOINED_THE_CLAN)).addString(player.getName()), new PledgeShowInfoUpdate(clan));
            player.sendPacket(SystemMsg.ENTERED_THE_CLAN);
            player.sendPacket(player.getClan().listAll());
            player.setLeaveClanTime(0L);
            player.updatePledgeClass();
            clan.addSkillsQuietly(player);
            player.sendPacket(new PledgeSkillList(clan));
            player.sendSkillList();
            EventHolder.getInstance().findEvent(player);
            player.broadcastCharInfo();
            player.store(false);
        } finally {
            request.done();
        }
    }
}
