package ru.j2dev.gameserver.utils;

import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.dao.CharacterDAO;
import ru.j2dev.gameserver.manager.CursedWeaponsManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;

public final class AdminFunctions {
    public static final Location JAIL_SPAWN = new Location(-114648, -249384, -2984);

    public static boolean kick(final String player, final String reason) {
        final Player plyr = World.getPlayer(player);
        return plyr != null && kick(plyr, reason);
    }

    public static boolean kick(final Player player, final String reason) {
        if (Config.ALLOW_CURSED_WEAPONS && Config.DROP_CURSED_WEAPONS_ON_KICK && player.isCursedWeaponEquipped()) {
            player.setPvpFlag(0);
            CursedWeaponsManager.getInstance().dropPlayer(player);
        }
        player.kick();
        return true;
    }

    public static String banChat(final Player adminChar, String adminName, String charName, final int val, String reason) {
        final Player player = World.getPlayer(charName);
        if (player != null) {
            charName = player.getName();
        } else if (CharacterDAO.getInstance().getObjectIdByName(charName) == 0) {
            return "Player " + charName + " not found.";
        }
        if ((adminName == null || adminName.isEmpty()) && adminChar != null) {
            adminName = adminChar.getName();
        }
        if (reason == null || reason.isEmpty()) {
            reason = "no arguments";
        }
        String announce = null;
        String result;
        if (val == 0) {
            if (adminChar != null && !adminChar.getPlayerAccess().CanUnBanChat) {
                return "You do not have permission to remove the chat ban.";
            }
            if (Config.BANCHAT_ANNOUNCE) {
                announce = ((Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty()) ? (adminName + " chat ban removed from player " + charName + ".") : ("From player " + charName + " unban chat."));
            }
            Log.add(adminName + " chat ban removed from player " + charName + ".", "banchat", adminChar);
            result = "You remove a chat ban from player " + charName + ".";
        } else if (val < 0) {
            if (adminChar != null && adminChar.getPlayerAccess().BanChatMaxValue > 0) {
                return "You can ban no more than " + adminChar.getPlayerAccess().BanChatMaxValue + " minutes.";
            }
            if (Config.BANCHAT_ANNOUNCE) {
                announce = ((Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty()) ? (adminName + " banned the chat player " + charName + " for an indefinite period, the reason: " + reason + ".") : ("banPlayer chat to the player " + charName + " for an indefinite period, the reason is: " + reason + "."));
            }
            Log.add(adminName + " banned the chat player " + charName + " for an indefinite period, the reason: " + reason + ".", "banchat", adminChar);
            result = "You have banned the chat player " + charName + " for an indefinite period.";
        } else {
            if (adminChar != null && !adminChar.getPlayerAccess().CanUnBanChat && (player == null || player.getNoChannel() != 0L)) {
                return "You do not have the right to change the time of the ban.";
            }
            if (adminChar != null && adminChar.getPlayerAccess().BanChatMaxValue != -1 && val > adminChar.getPlayerAccess().BanChatMaxValue) {
                return "You can ban no more than " + adminChar.getPlayerAccess().BanChatMaxValue + " minutes.";
            }
            if (Config.BANCHAT_ANNOUNCE) {
                announce = ((Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty()) ? (adminName + " banned the chat player " + charName + " on " + val + " minutes, the reason: " + reason + ".") : ("banPlayer chat to the player " + charName + " on " + val + " minutes, reason: " + reason + "."));
            }
            Log.add(adminName + " banned the chat player " + charName + " on " + val + " minutes, the reason: " + reason + ".", "banchat", adminChar);
            result = "You have banned the chat player " + charName + " on " + val + " minutes.";
        }
        if (player != null) {
            updateNoChannel(player, val, reason);
        } else {
            AutoBan.ChatBan(charName, val, reason, adminName);
        }
        if (announce != null) {
            if (Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD) {
                Announcements.getInstance().announceToAll(announce);
            } else if (adminChar != null) {
                Announcements.shout(adminChar, announce, ChatType.CRITICAL_ANNOUNCE);
            }
        }
        return result;
    }

    private static void updateNoChannel(final Player player, final int time, final String reason) {
        player.updateNoChannel(time * 60000);
        if (time == 0) {
            player.sendMessage(new CustomMessage("common.ChatUnBanned", player));
        } else if (time > 0) {
            if (reason == null || reason.isEmpty()) {
                player.sendMessage(new CustomMessage("common.ChatBanned", player).addNumber(time));
            } else {
                player.sendMessage(new CustomMessage("common.ChatBannedWithReason", player).addNumber(time).addString(reason));
            }
        } else if (reason == null || reason.isEmpty()) {
            player.sendMessage(new CustomMessage("common.ChatBannedPermanently", player));
        } else {
            player.sendMessage(new CustomMessage("common.ChatBannedPermanentlyWithReason", player).addString(reason));
        }
    }
}
