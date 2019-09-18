package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PackageToList;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.WarehouseFunctions;

public class WarehouseInstance extends NpcInstance {
    public WarehouseInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }
        if (getTemplate().getHtmRoot() != null) {
            return getTemplate().getHtmRoot() + pom + ".htm";
        }
        return "warehouse/" + pom + ".htm";
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        if (player.getEnchantScroll() != null) {
            Log.add("Player " + player.getName() + " trying to use enchant exploit[Warehouse], ban this player!", "illegal-actions");
            player.setEnchantScroll(null);
            return;
        }
        if (command.startsWith("deposit_items")) {
            player.sendPacket(new PackageToList(player));
        } else if (command.startsWith("withdraw_items")) {
            WarehouseFunctions.showFreightWindow(player);
        } else if (command.startsWith("WithdrawP")) {
            final int val = Integer.parseInt(command.substring(10));
            if (val == 99) {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.setFile("warehouse/personal.htm");
                html.replace("%npcname%", getName());
                player.sendPacket(html);
            } else {
                WarehouseFunctions.showRetrieveWindow(player, val);
            }
        } else if ("DepositP".equals(command)) {
            WarehouseFunctions.showDepositWindow(player);
        } else if (command.startsWith("WithdrawC")) {
            final int val = Integer.parseInt(command.substring(10));
            if (val == 99) {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.setFile("warehouse/clan.htm");
                html.replace("%npcname%", getName());
                player.sendPacket(html);
            } else {
                WarehouseFunctions.showWithdrawWindowClan(player, val);
            }
        } else if ("DepositC".equals(command)) {
            WarehouseFunctions.showDepositWindowClan(player);
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
