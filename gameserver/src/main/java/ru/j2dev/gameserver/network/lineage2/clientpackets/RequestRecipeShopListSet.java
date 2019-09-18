package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ManufactureItem;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RecipeShopMsg;
import ru.j2dev.gameserver.utils.TradeHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RequestRecipeShopListSet extends L2GameClientPacket {
    private int[] _recipes;
    private long[] _prices;
    private int _count;

    @Override
    protected void readImpl() {
        _count = readD();
        if (_count * 8 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _recipes = new int[_count];
        _prices = new long[_count];
        for (int i = 0; i < _count; ++i) {
            _recipes[i] = readD();
            _prices[i] = readD();
            if (_prices[i] < 0L) {
                _count = 0;
                return;
            }
        }
    }

    @Override
    protected void runImpl() {
        final Player manufacturer = getClient().getActiveChar();
        if (manufacturer == null || _count == 0) {
            return;
        }
        if (!TradeHelper.checksIfCanOpenStore(manufacturer, 5)) {
            manufacturer.sendActionFailed();
            return;
        }
        if (_count > Config.MAX_PVTCRAFT_SLOTS) {
            sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        }
        final List<ManufactureItem> createList = new CopyOnWriteArrayList<>();
        for (int i = 0; i < _count; ++i) {
            final int recipeId = _recipes[i];
            final long price = _prices[i];
            if (manufacturer.findRecipe(recipeId)) {
                final ManufactureItem mi = new ManufactureItem(recipeId, price);
                createList.add(mi);
            }
        }
        if (!createList.isEmpty()) {
            manufacturer.setCreateList(createList);
            manufacturer.saveTradeList();
            manufacturer.setPrivateStoreType(Player.STORE_PRIVATE_MANUFACTURE);
            manufacturer.broadcastPacket(new RecipeShopMsg(manufacturer));
            manufacturer.sitDown(null);
            manufacturer.broadcastCharInfo();
        }
        manufacturer.sendActionFailed();
    }
}
