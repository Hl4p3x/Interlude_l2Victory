package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.matching.MatchingRoom;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExAskJoinPartyRoom;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SysMsgContainer;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;

public class RequestAskJoinPartyRoom extends L2GameClientPacket {
    private String _name;

    @Override
    protected void readImpl() {
        _name = readS(16);
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final Player targetPlayer = World.getPlayer(_name);
        if (targetPlayer == null || targetPlayer == player) {
            player.sendActionFailed();
            return;
        }
        if (player.isProcessingRequest()) {
            player.sendPacket(SystemMsg.WAITING_FOR_ANOTHER_REPLY);
            return;
        }
        if (targetPlayer.isProcessingRequest()) {
            player.sendPacket(((SysMsgContainer) new SystemMessage2(SystemMsg.C1_IS_ON_ANOTHER_TASK)).addName(targetPlayer));
            return;
        }
        if (targetPlayer.getMatchingRoom() != null) {
            return;
        }
        final MatchingRoom room = player.getMatchingRoom();
        if (room == null || room.getType() != MatchingRoom.PARTY_MATCHING) {
            return;
        }
        if (room.getLeader() != player) {
            player.sendPacket(SystemMsg.ONLY_A_ROOM_LEADER_MAY_INVITE_OTHERS_TO_A_PARTY_ROOM);
            return;
        }
        if (room.getPlayers().size() >= room.getMaxMembersSize()) {
            player.sendPacket(SystemMsg.THE_PARTY_ROOM_IS_FULL);
            return;
        }
        new Request(L2RequestType.PARTY_ROOM, player, targetPlayer).setTimeout(10000L);
        targetPlayer.sendPacket(new ExAskJoinPartyRoom(player.getName(), room.getTopic()));
        player.sendPacket(((SysMsgContainer) new SystemMessage2(SystemMsg.S1_HAS_SENT_AN_INVITATION_TO_ROOM_S2).addName(player)).addString(room.getTopic()));
        targetPlayer.sendPacket(((SysMsgContainer) new SystemMessage2(SystemMsg.S1_HAS_SENT_AN_INVITATION_TO_ROOM_S2).addName(player)).addString(room.getTopic()));
    }
}
