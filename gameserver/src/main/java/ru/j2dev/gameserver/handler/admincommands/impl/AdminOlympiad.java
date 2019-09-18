package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.entity.olympiad.HeroManager;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadSystemManager;

public class AdminOlympiad implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        switch (command) {
            case admin_oly_save: {
                if (!Config.OLY_ENABLED) {
                    return false;
                }
                try {
                    OlympiadSystemManager.getInstance().save();
                } catch (Exception ignored) {
                }
                activeChar.sendMessage("olympaid data saved.");
                break;
            }
            case admin_add_oly_points: {
                if (wordList.length < 3) {
                    activeChar.sendMessage("Command syntax: //add_oly_points <char_name> <point_to_add>");
                    activeChar.sendMessage("This command can be applied only for online players.");
                    return false;
                }
                final Player player = World.getPlayer(wordList[1]);
                if (player == null) {
                    activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
                    return false;
                }
                int pointToAdd;
                try {
                    pointToAdd = Integer.parseInt(wordList[2]);
                } catch (NumberFormatException e) {
                    activeChar.sendMessage("Please specify integer value for olympiad points.");
                    return false;
                }
                final int curPoints = NoblessManager.getInstance().getPointsOf(player.getObjectId());
                final int newPoints = curPoints + pointToAdd;
                NoblessManager.getInstance().setPointsOf(player.getObjectId(), newPoints);
                activeChar.sendMessage("Added " + pointToAdd + " points to character " + player.getName());
                activeChar.sendMessage("Old points: " + curPoints + ", new points: " + newPoints);
                break;
            }
            case admin_oly_start: {
                Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_STARTED);
                break;
            }
            case admin_oly_stop: {
                Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_ENDED);
                try {
                    OlympiadSystemManager.getInstance().save();
                } catch (Exception ignored) {
                }
                break;
            }
            case admin_add_hero: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("Command syntax: //add_hero <char_name>");
                    activeChar.sendMessage("This command can be applied only for online players.");
                    return false;
                }
                final Player player = World.getPlayer(wordList[1]);
                if (player == null) {
                    activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
                    return false;
                }
                HeroManager.getInstance().activateHero(player);
                activeChar.sendMessage("Hero status added to player " + player.getName());
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
        admin_oly_save,
        admin_add_oly_points,
        admin_oly_start,
        admin_add_hero,
        admin_oly_stop
    }
}
