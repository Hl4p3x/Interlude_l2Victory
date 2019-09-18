package ru.j2dev.gameserver.network.lineage2.serverpackets;

import java.util.Collections;
import java.util.Set;

public class ExMpccPartymasterList extends L2GameServerPacket {
    private Set<String> _members;

    public ExMpccPartymasterList(final Set<String> s) {
        _members = Collections.emptySet();
        _members = s;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xa2);
        writeD(_members.size());
        _members.forEach(this::writeS);
    }
}
