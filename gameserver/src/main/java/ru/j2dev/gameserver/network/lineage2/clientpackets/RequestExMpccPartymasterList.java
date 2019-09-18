package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.matching.MatchingRoom;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExMpccPartymasterList;

import java.util.HashSet;
import java.util.Set;

public class RequestExMpccPartymasterList extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final MatchingRoom room = player.getMatchingRoom();
        if (room == null || room.getType() != MatchingRoom.CC_MATCHING) {
            return;
        }
        final Set<String> set = new HashSet<>();
        for (final Player $member : room.getPlayers()) {
            if ($member.getParty() != null) {
                set.add($member.getParty().getPartyLeader().getName());
            }
        }
        player.sendPacket(new ExMpccPartymasterList(set));
    }
}
