package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInfo;
import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class TradeStart extends L2GameServerPacket {
    private final List<ItemInfo> _tradelist;
    private final int targetId;

    public TradeStart(final Player player, final Player target) {
        _tradelist = new ArrayList<>();
        targetId = target.getObjectId();
        final List<ItemInstance> items = player.getInventory().getItems();
        items.stream().filter(item -> item.canBeTraded(player)).map(ItemInfo::new).forEach(_tradelist::add);
    }

    @Override
    protected final void writeImpl() {
        writeC(0x1e);
        writeD(targetId);
        writeH(_tradelist.size());
        _tradelist.forEach(item -> {
            writeH(item.getItem().getType1());
            writeD(item.getObjectId());
            writeD(item.getItemId());
            writeD((int) item.getCount());
            writeH(item.getItem().getType2ForPackets());
            writeH(0);
            writeD(item.getItem().getBodyPart());
            writeH(item.getEnchantLevel());
            writeH(0);
            writeH(0);
        });
    }
}
