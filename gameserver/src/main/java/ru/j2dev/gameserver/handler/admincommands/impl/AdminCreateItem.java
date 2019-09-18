package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.InventoryUpdate;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

public class AdminCreateItem implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().UseGMShop) {
            return false;
        }
        switch (command) {
            case admin_itemcreate: {
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
                break;
            }
            case admin_ci:
            case admin_create_item: {
                try {
                    if (wordList.length < 2) {
                        activeChar.sendMessage("USAGE: create_item id [count]");
                        return false;
                    }
                    final int item_id = Integer.parseInt(wordList[1]);
                    final long item_count = (wordList.length < 3) ? 1L : Long.parseLong(wordList[2]);
                    createItem(activeChar, item_id, item_count);
                } catch (NumberFormatException nfe) {
                    activeChar.sendMessage("USAGE: create_item id [count]");
                }
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
                break;
            }
            case admin_spreaditem: {
                try {
                    final int id = Integer.parseInt(wordList[1]);
                    final int num = (wordList.length > 2) ? Integer.parseInt(wordList[2]) : 1;
                    final long count = (wordList.length > 3) ? Long.parseLong(wordList[3]) : 1L;
                    for (int i = 0; i < num; ++i) {
                        final ItemInstance createditem = ItemFunctions.createItem(id);
                        createditem.setCount(count);
                        createditem.dropMe(activeChar, Location.findPointToStay(activeChar, 100));
                    }
                } catch (NumberFormatException nfe) {
                    activeChar.sendMessage("Specify a valid number.");
                } catch (StringIndexOutOfBoundsException e) {
                    activeChar.sendMessage("Can't create this item.");
                }
                break;
            }
            case admin_create_item_element: {
                try {
                    if (wordList.length < 4) {
                        activeChar.sendMessage("USAGE: create_item_attribue [id] [element id] [value]");
                        return false;
                    }
                    final int item_id = Integer.parseInt(wordList[1]);
                    final int elementId = Integer.parseInt(wordList[2]);
                    final int value = Integer.parseInt(wordList[3]);
                    if (elementId > 5 || elementId < 0) {
                        activeChar.sendMessage("Improper element Id");
                        return false;
                    }
                    if (value < 1 || value > 300) {
                        activeChar.sendMessage("Improper element value");
                        return false;
                    }
                    final ItemInstance item = createItem(activeChar, item_id, 1L);
                    final Element element = Element.getElementById(elementId);
                    item.setAttributeElement(element, item.getAttributeElementValue(element, false) + value);
                    activeChar.sendPacket(new InventoryUpdate().addModifiedItem(item));
                } catch (NumberFormatException nfe) {
                    activeChar.sendMessage("USAGE: create_item id [count]");
                }
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/itemcreation.htm"));
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private ItemInstance createItem(final Player activeChar, final int itemId, final long count) {
        ItemInstance createditem = ItemFunctions.createItem(itemId);
        createditem.setCount(count);
        Log.LogItem(activeChar, ItemLog.Create, createditem);
        activeChar.getInventory().addItem(createditem);
        if (!createditem.isStackable()) {
            for (long i = 0L; i < count - 1L; ++i) {
                createditem = ItemFunctions.createItem(itemId);
                Log.LogItem(activeChar, ItemLog.Create, createditem);
                activeChar.getInventory().addItem(createditem);
            }
        }
        activeChar.sendPacket(SystemMessage2.obtainItems(itemId, count, 0));
        return createditem;
    }

    private enum Commands {
        admin_itemcreate,
        admin_create_item,
        admin_ci,
        admin_spreaditem,
        admin_create_item_element
    }
}
