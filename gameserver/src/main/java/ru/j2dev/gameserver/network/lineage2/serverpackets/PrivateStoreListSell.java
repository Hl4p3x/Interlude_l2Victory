package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.TradeItem;

import java.util.List;

public class PrivateStoreListSell extends L2GameServerPacket {
    private final boolean _package;
    private final int _sellerId;
    private final long _adena;
    private final List<TradeItem> _sellList;

    public PrivateStoreListSell(final Player buyer, final Player seller) {
        _sellerId = seller.getObjectId();
        _adena = buyer.getAdena();
        _package = (seller.getPrivateStoreType() == 8);
        _sellList = seller.getSellList();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x9b);
        writeD(_sellerId);
        writeD(_package ? 1 : 0);
        writeD((int) _adena);
        writeD(_sellList.size());
        _sellList.forEach(si -> {
            writeD(si.getItem().getType2ForPackets());
            writeD(si.getObjectId());
            writeD(si.getItemId());
            writeD((int) si.getCount());
            writeH(0);
            writeH(si.getEnchantLevel());
            writeH(0);
            writeD(si.getItem().getBodyPart());
            writeD((int) si.getOwnersPrice());
            writeD((int) si.getStorePrice());
        });
    }
}
