package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ManufactureItem;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RecipeShopItemInfo;

public class RequestRecipeShopMakeInfo extends L2GameClientPacket {
    private int _manufacturerId;
    private int _recipeId;

    @Override
    protected void readImpl() {
        _manufacturerId = readD();
        _recipeId = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
            return;
        }
        final Player manufacturer = (Player) activeChar.getVisibleObject(_manufacturerId);
        if (manufacturer == null || manufacturer.getPrivateStoreType() != 5 || !manufacturer.isInActingRange(activeChar)) {
            activeChar.sendActionFailed();
            return;
        }
        long price = -1L;
        for (final ManufactureItem i : manufacturer.getCreateList()) {
            if (i.getRecipeId() == _recipeId) {
                price = i.getCost();
                break;
            }
        }
        if (price == -1L) {
            activeChar.sendActionFailed();
            return;
        }
        activeChar.sendPacket(new RecipeShopItemInfo(activeChar, manufacturer, _recipeId, price, -1));
    }
}
