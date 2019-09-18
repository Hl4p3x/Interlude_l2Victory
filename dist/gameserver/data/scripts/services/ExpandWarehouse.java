package services;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.ItemFunctions;

public class ExpandWarehouse extends Functions {
    public void get() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_EXPAND_WAREHOUSE_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (ItemFunctions.removeItem(player, Config.SERVICES_EXPAND_WAREHOUSE_ITEM, (long) Config.SERVICES_EXPAND_WAREHOUSE_PRICE, true) >= Config.SERVICES_EXPAND_WAREHOUSE_PRICE) {
            player.setExpandWarehouse(player.getExpandWarehouse() + Config.SERVICES_EXPAND_WAREHOUSE_SLOT_AMOUNT);
            player.setVar("ExpandWarehouse", String.valueOf(player.getExpandWarehouse()), -1L);
        } else if (Config.SERVICES_EXPAND_WAREHOUSE_ITEM == 57) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        } else {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
        }
        show();
    }

    public void show() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_EXPAND_WAREHOUSE_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/expand_warehouse.htm");
        msg.replace("%wh_limit%", String.valueOf(player.getWarehouseLimit()));
        msg.replace("%wh_exp_price%", String.valueOf(Config.SERVICES_EXPAND_WAREHOUSE_PRICE));
        msg.replace("%wh_exp_item%", String.valueOf(Config.SERVICES_EXPAND_WAREHOUSE_ITEM));
        msg.replace("%wh_exp_slot_amount%", String.valueOf(Config.SERVICES_EXPAND_WAREHOUSE_SLOT_AMOUNT));
        player.sendPacket(msg);
    }
}
