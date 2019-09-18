package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.network.lineage2.components.NpcString;

import java.util.Arrays;

public abstract class NpcStringContainer extends L2GameServerPacket {
    private final NpcString _npcString;
    private final String[] _parameters;

    protected NpcStringContainer(final NpcString npcString, final String... arg) {
        _parameters = new String[5];
        _npcString = npcString;
        System.arraycopy(arg, 0, _parameters, 0, arg.length);
    }

    protected void writeElements() {
        Arrays.stream(_parameters).forEach(this::writeS);
    }
}
