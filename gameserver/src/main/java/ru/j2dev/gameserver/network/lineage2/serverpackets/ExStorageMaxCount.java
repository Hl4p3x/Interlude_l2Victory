package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;

public class ExStorageMaxCount extends L2GameServerPacket {
    private final int _inventory;
    private final int _warehouse;
    private final int _clan;
    private final int _privateSell;
    private final int _privateBuy;
    private final int _recipeDwarven;
    private final int _recipeCommon;
    private final int _questItemsLimit;
    private int _inventoryExtraSlots;

    public ExStorageMaxCount(final Player player) {
        _inventory = player.getInventoryLimit();
        _warehouse = player.getWarehouseLimit();
        _clan = Config.WAREHOUSE_SLOTS_CLAN;
        final int tradeLimit = player.getTradeLimit();
        _privateSell = tradeLimit;
        _privateBuy = tradeLimit;
        _recipeDwarven = player.getDwarvenRecipeLimit();
        _recipeCommon = player.getCommonRecipeLimit();
        _questItemsLimit = Config.QUEST_INVENTORY_MAXIMUM;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x2e);
        writeD(_inventory);
        writeD(_warehouse);
        writeD(_clan);
        writeD(_privateSell);
        writeD(_privateBuy);
        writeD(_recipeDwarven);
        writeD(_recipeCommon);
    }
}
