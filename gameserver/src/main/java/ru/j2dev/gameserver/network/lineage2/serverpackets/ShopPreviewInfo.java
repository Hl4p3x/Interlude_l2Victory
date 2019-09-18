package ru.j2dev.gameserver.network.lineage2.serverpackets;

import java.util.Map;
import java.util.stream.IntStream;

public class ShopPreviewInfo extends L2GameServerPacket {
    private final Map<Integer, Integer> _itemlist;

    public ShopPreviewInfo(final Map<Integer, Integer> itemlist) {
        _itemlist = itemlist;
    }

    @Override
    protected void writeImpl() {
        writeC(0xf0);
        writeD(17);
        //todo chek this shit
        IntStream.range(0, 16).forEach(key -> writeD(getFromList(key)));
    }

    private int getFromList(final int key) {
        return (_itemlist.get(key) != null) ? _itemlist.get(key) : 0;
    }
}
