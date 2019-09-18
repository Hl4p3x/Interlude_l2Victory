package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.templates.Henna;

import java.util.ArrayList;
import java.util.List;

public class HennaUnequipList extends L2GameServerPacket {
    private final int _emptySlots;
    private final long _adena;
    private final List<Henna> availHenna;

    public HennaUnequipList(final Player player) {
        availHenna = new ArrayList<>(3);
        _adena = player.getAdena();
        _emptySlots = player.getHennaEmptySlots();
        for (int i = 1; i <= 3; ++i) {
            if (player.getHenna(i) != null) {
                availHenna.add(player.getHenna(i));
            }
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0xe5);
        writeD((int) _adena);
        writeD(_emptySlots);
        writeD(availHenna.size());
        availHenna.forEach(henna -> {
            writeD(henna.getSymbolId());
            writeD(henna.getDyeId());
            writeD((int) henna.getDrawCount());
            writeD((int) henna.getPrice());
            writeD(1);
        });
    }
}
