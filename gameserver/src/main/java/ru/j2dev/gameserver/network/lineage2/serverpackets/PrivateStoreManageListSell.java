package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.TradeItem;

import java.util.ArrayList;
import java.util.List;

public class PrivateStoreManageListSell extends L2GameServerPacket {
    private final int _sellerId;
    private final long _adena;
    private final boolean _package;
    private final List<TradeItem> _sellList;
    private final List<TradeItem> _sellList0;

    public PrivateStoreManageListSell(final Player seller, final boolean pkg) {
        _sellerId = seller.getObjectId();
        _adena = seller.getAdena();
        _package = pkg;
        _sellList0 = seller.getSellList(_package);
        _sellList = new ArrayList<>();
        for (final TradeItem si : _sellList0) {
            if (si.getCount() <= 0L) {
                _sellList0.remove(si);
            } else {
                ItemInstance item = seller.getInventory().getItemByObjectId(si.getObjectId());
                if (item == null) {
                    item = seller.getInventory().getItemByItemId(si.getItemId());
                }
                if (item == null || !item.canBeTraded(seller) || item.getItemId() == 57) {
                    _sellList0.remove(si);
                } else {
                    si.setCount(Math.min(item.getCount(), si.getCount()));
                }
            }
        }
        final List<ItemInstance> items = seller.getInventory().getItems();
        for (final ItemInstance item2 : items) {
            Label_0375:
            {
                if (item2.canBeTraded(seller) && item2.getItemId() != 57) {
                    for (final TradeItem si2 : _sellList0) {
                        if (si2.getObjectId() == item2.getObjectId()) {
                            if (si2.getCount() == item2.getCount()) {
                                break Label_0375;
                            }
                            final TradeItem ti = new TradeItem(item2);
                            ti.setCount(item2.getCount() - si2.getCount());
                            _sellList.add(ti);
                            break Label_0375;
                        }
                    }
                    _sellList.add(new TradeItem(item2));
                }
            }
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0x9a);
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
            writeD((int) si.getStorePrice());
        });
        writeD(_sellList0.size());
        _sellList0.forEach(si -> {
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
