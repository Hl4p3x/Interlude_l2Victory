package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.manager.CursedWeaponsManager;
import ru.j2dev.gameserver.model.CursedWeapon;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.utils.ItemFunctions;

public class AdminCursedWeapons implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().Menu) {
            return false;
        }
        final CursedWeaponsManager cwm = CursedWeaponsManager.getInstance();
        CursedWeapon cw = null;
        switch (command) {
            case admin_cw_remove:
            case admin_cw_goto:
            case admin_cw_add:
            case admin_cw_drop: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("\u0412\u044b \u043d\u0435 \u0443\u043a\u0430\u0437\u0430\u043b\u0438 id");
                    return false;
                }
                for (final CursedWeapon cwp : CursedWeaponsManager.getInstance().getCursedWeapons()) {
                    if (cwp.getName().toLowerCase().contains(wordList[1].toLowerCase())) {
                        cw = cwp;
                    }
                }
                if (cw == null) {
                    activeChar.sendMessage("\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u044b\u0439 id");
                    return false;
                }
                break;
            }
        }
        switch (command) {
            case admin_cw_info: {
                activeChar.sendMessage("======= Cursed Weapons: =======");
                for (final CursedWeapon c : cwm.getCursedWeapons()) {
                    activeChar.sendMessage("> " + c.getName() + " (" + c.getItemId() + ")");
                    if (c.isActivated()) {
                        final Player pl = c.getPlayer();
                        activeChar.sendMessage("  Player holding: " + pl.getName());
                        activeChar.sendMessage("  Player karma: " + c.getPlayerKarma());
                        activeChar.sendMessage("  Time Remaining: " + c.getTimeLeft() / 60000L + " min.");
                        activeChar.sendMessage("  Kills : " + c.getNbKills());
                    } else if (c.isDropped()) {
                        activeChar.sendMessage("  Lying on the ground.");
                        activeChar.sendMessage("  Time Remaining: " + c.getTimeLeft() / 60000L + " min.");
                        activeChar.sendMessage("  Kills : " + c.getNbKills());
                    } else {
                        activeChar.sendMessage("  Don't exist in the world.");
                    }
                }
                break;
            }
            case admin_cw_reload: {
                activeChar.sendMessage("Cursed weapons can't be reloaded.");
                break;
            }
            case admin_cw_check: {
                CursedWeaponsManager.getInstance().checkConditions();
                break;
            }
            case admin_cw_remove: {
                CursedWeaponsManager.getInstance().endOfLife(cw);
                break;
            }
            case admin_cw_goto: {
                activeChar.teleToLocation(cw.getLoc());
                break;
            }
            case admin_cw_add: {
                if (cw.isActive()) {
                    activeChar.sendMessage("This cursed weapon is already active.");
                    break;
                }
                final GameObject target = activeChar.getTarget();
                if (target != null && target.isPlayer() && !target.isOlyParticipant()) {
                    final Player player = (Player) target;
                    final ItemInstance item = ItemFunctions.createItem(cw.getItemId());
                    cwm.activate(player, player.getInventory().addItem(item));
                    cwm.showUsageTime(player, cw);
                }
                break;
            }
            case admin_cw_drop: {
                if (cw == null) {
                    return false;
                }
                if (cw.isActive()) {
                    activeChar.sendMessage("This cursed weapon is already active.");
                    break;
                }
                final GameObject target = activeChar.getTarget();
                if (target != null && target.isPlayer() && !target.isOlyParticipant()) {
                    final Player player = (Player) target;
                    cw.create(null, player);
                    break;
                }
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_cw_info,
        admin_cw_remove,
        admin_cw_goto,
        admin_cw_reload,
        admin_cw_add,
        admin_cw_drop,
        admin_cw_check
    }
}
