package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.data.xml.holder.HennaHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.templates.Henna;

import java.util.ArrayList;
import java.util.List;

public class HennaEquipList extends L2GameServerPacket {
    private final int _emptySlots;
    private final long _adena;
    private final List<Henna> _hennas;

    public HennaEquipList(final Player player) {
        _hennas = new ArrayList<>();
        _adena = player.getAdena();
        _emptySlots = player.getHennaEmptySlots();
        final List<Henna> list = HennaHolder.getInstance().generateList(player);
        list.stream().filter(element -> player.getInventory().getItemByItemId(element.getDyeId()) != null).forEach(_hennas::add);
    }

    @Override
    protected final void writeImpl() {
        writeC(0xe2);
        writeD((int) _adena);
        writeD(_emptySlots);
        if (_hennas.size() != 0) {
            writeD(_hennas.size());
            _hennas.forEach(henna -> {
                writeD(henna.getSymbolId());
                writeD(henna.getDyeId());
                writeD((int) henna.getDrawCount());
                writeD((int) henna.getPrice());
                writeD(1);
            });
        } else {
            writeD(1);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
        }
    }
}
