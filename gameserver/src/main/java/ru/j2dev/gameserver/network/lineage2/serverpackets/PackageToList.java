package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

import java.util.Collections;
import java.util.Map;

public class PackageToList extends L2GameServerPacket {
    private Map<Integer, String> _characters;

    public PackageToList(final Player player) {
        _characters = Collections.emptyMap();
        _characters = player.getAccountChars();
    }

    @Override
    protected void writeImpl() {
        writeC(0xc2);
        writeD(_characters.size());
        _characters.forEach((key, value) -> {
            writeD(key);
            writeS(value);
        });
    }
}
