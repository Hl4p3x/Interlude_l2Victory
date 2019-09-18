package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.data.xml.holder.BuyListHolder;
import ru.j2dev.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.BuyList;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.utils.GameStats;

public class AdminShop implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().UseGMShop) {
            return false;
        }
        switch (command) {
            case admin_buy: {
                try {
                    handleBuyRequest(activeChar, fullString.substring(10));
                } catch (IndexOutOfBoundsException e) {
                    activeChar.sendMessage("Please specify buylist.");
                }
                break;
            }
            case admin_gmshop: {
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/gmshops.htm"));
                break;
            }
            case admin_tax: {
                activeChar.sendMessage("TaxSum: " + GameStats.getTaxSum());
                break;
            }
            case admin_taxclear: {
                GameStats.addTax(-GameStats.getTaxSum());
                activeChar.sendMessage("TaxSum: " + GameStats.getTaxSum());
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void handleBuyRequest(final Player activeChar, final String command) {
        int val = -1;
        try {
            val = Integer.parseInt(command);
        } catch (Exception ignored) {
        }
        final NpcTradeList list = BuyListHolder.getInstance().getBuyList(val);
        if (list != null) {
            activeChar.sendPacket(new BuyList(list, activeChar, 0.0));
        }
        activeChar.sendActionFailed();
    }

    private enum Commands {
        admin_buy,
        admin_gmshop,
        admin_tax,
        admin_taxclear
    }
}
