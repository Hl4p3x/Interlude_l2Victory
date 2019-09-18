package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.utils.AdminFunctions;
import ru.j2dev.gameserver.utils.Location;

import java.util.StringTokenizer;

public class AdminMenu implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().Menu) {
            return false;
        }
        if (fullString.startsWith("admin_teleport_character_to_menu")) {
            final String[] data = fullString.split(" ");
            if (data.length == 5) {
                final String playerName = data[1];
                final Player player = World.getPlayer(playerName);
                if (player != null) {
                    teleportCharacter(player, new Location(Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4])), activeChar);
                }
            }
        } else if (fullString.startsWith("admin_recall_char_menu")) {
            try {
                final String targetName = fullString.substring(23);
                final Player player2 = World.getPlayer(targetName);
                teleportCharacter(player2, activeChar.getLoc(), activeChar);
            } catch (StringIndexOutOfBoundsException ignored) {
            }
        } else if (fullString.startsWith("admin_goto_char_menu")) {
            try {
                final String targetName = fullString.substring(21);
                final Player player2 = World.getPlayer(targetName);
                teleportToCharacter(activeChar, player2);
            } catch (StringIndexOutOfBoundsException ignored) {
            }
        } else if ("admin_kill_menu".equals(fullString)) {
            GameObject obj = activeChar.getTarget();
            final StringTokenizer st = new StringTokenizer(fullString);
            if (st.countTokens() > 1) {
                st.nextToken();
                final String player3 = st.nextToken();
                final Player plyr = World.getPlayer(player3);
                if (plyr == null) {
                    activeChar.sendMessage("Player " + player3 + " not found in game.");
                }
                obj = plyr;
            }
            if (obj != null && obj.isCreature()) {
                final Creature target = (Creature) obj;
                target.reduceCurrentHp(target.getMaxHp() + 1, activeChar, null, true, true, true, false, false, false, true);
            } else {
                activeChar.sendPacket(Msg.INVALID_TARGET);
            }
        } else if (fullString.startsWith("admin_kick_menu")) {
            final StringTokenizer st2 = new StringTokenizer(fullString);
            if (st2.countTokens() > 1) {
                st2.nextToken();
                final String player4 = st2.nextToken();
                if (AdminFunctions.kick(player4, "kick")) {
                    activeChar.sendMessage("Player kicked.");
                }
            }
        }
        activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/charmanage.htm"));
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void teleportCharacter(final Player player, final Location loc, final Player activeChar) {
        if (player != null) {
            player.sendMessage("Admin is teleporting you.");
            player.teleToLocation(loc);
        }
    }

    private void teleportToCharacter(final Player activeChar, final GameObject target) {
        if (target != null && target.isPlayer()) {
            final Player player = (Player) target;
            if (player.getObjectId() == activeChar.getObjectId()) {
                activeChar.sendMessage("You cannot self teleport.");
            } else {
                activeChar.teleToLocation(player.getLoc());
                activeChar.sendMessage("You have teleported to character " + player.getName() + ".");
            }
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    private enum Commands {
        admin_char_manage,
        admin_teleport_character_to_menu,
        admin_recall_char_menu,
        admin_goto_char_menu,
        admin_kick_menu,
        admin_kill_menu,
        admin_ban_menu,
        admin_unban_menu
    }
}
