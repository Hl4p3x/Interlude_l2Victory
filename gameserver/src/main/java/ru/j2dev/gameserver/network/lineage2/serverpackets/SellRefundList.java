package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.TradeItem;

import java.util.LinkedList;
import java.util.List;

public class SellRefundList extends L2GameServerPacket {
    private final List<TradeItem> _sellList;
    private final int _adena;
    private final boolean _isDone;

    public SellRefundList(final Player player, final boolean isDone) {
        _adena = (int) player.getAdena();
        _isDone = isDone;
        final List<ItemInstance> items = player.getInventory().getItems();
        _sellList = new LinkedList<>();
        items.stream().filter(item -> item.canBeSold(player)).map(TradeItem::new).forEach(_sellList::add);
    }

    @Override
    protected void writeImpl() {
        writeC(0x10);
        writeD(_adena);
        writeD(_isDone);
        writeH(_sellList.size());
        _sellList.forEach(item -> {
            writeH(item.getItem().getType1());
            writeD(item.getObjectId());
            writeD(item.getItemId());
            writeD((int) item.getCount());
            writeH(item.getItem().getType2ForPackets());
            writeH(item.getCustomType1());
            writeD(item.getItem().getBodyPart());
            writeH(item.getEnchantLevel());
            writeH(item.getCustomType2());
            writeH(0);
            writeD((int) Math.max(1L, item.getReferencePrice() / Config.ALT_SHOP_REFUND_SELL_DIVISOR));
        });
    }
}
