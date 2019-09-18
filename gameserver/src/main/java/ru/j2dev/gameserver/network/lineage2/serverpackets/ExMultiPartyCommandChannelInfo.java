package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.CommandChannel;
import ru.j2dev.gameserver.model.Player;

import java.util.ArrayList;
import java.util.List;

public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket {
    private final String ChannelLeaderName;
    private final int MemberCount;
    private final List<ChannelPartyInfo> parties;

    public ExMultiPartyCommandChannelInfo(final CommandChannel channel) {
        ChannelLeaderName = channel.getChannelLeader().getName();
        MemberCount = channel.getMemberCount();
        parties = new ArrayList<>();
        channel.getParties().forEach(party -> {
            final Player leader = party.getPartyLeader();
            if (leader != null) {
                parties.add(new ChannelPartyInfo(leader.getName(), leader.getObjectId(), party.getMemberCount()));
            }
        });
    }

    @Override
    protected void writeImpl() {
        writeEx(0x30);
        writeS(ChannelLeaderName);
        writeD(0);
        writeD(MemberCount);
        writeD(parties.size());
        parties.forEach(party -> {
            writeS(party.Leader_name);
            writeD(party.Leader_obj_id);
            writeD(party.MemberCount);
        });
    }

    static class ChannelPartyInfo {
        public final String Leader_name;
        public final int Leader_obj_id;
        public final int MemberCount;

        public ChannelPartyInfo(final String _Leader_name, final int _Leader_obj_id, final int _MemberCount) {
            Leader_name = _Leader_name;
            Leader_obj_id = _Leader_obj_id;
            MemberCount = _MemberCount;
        }
    }
}
