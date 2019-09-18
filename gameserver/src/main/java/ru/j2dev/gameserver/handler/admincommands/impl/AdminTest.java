package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;

public class AdminTest implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        switch (command) {
            case admin_collapse_this: {
                if (activeChar.getReflection() != null) {
                    activeChar.getReflection().startCollapseTimer(1000L);
                    break;
                }
                activeChar.sendMessage("No reflection");
                break;
            }
            case admin_collapse_this2: {
                if (activeChar.getReflection() != null) {
                    activeChar.getReflection().collapse();
                    break;
                }
                activeChar.sendMessage("No reflection");
                break;
            }
            case admin_alt_set_target_hwid: {
                final Player targetPlayer = (activeChar.getTarget() != null) ? activeChar.getTarget().getPlayer() : null;
                if (targetPlayer != null) {
                    targetPlayer.getNetConnection().setHwid(wordList[1]);
                }
                break;
            }
            case admin_alt_move_000: {
                Player targetPlayer = (activeChar.getTarget() != null) ? activeChar.getTarget().getPlayer() : null;
                if (targetPlayer == null) {
                    targetPlayer = activeChar;
                }
                targetPlayer.moveToLocation(0, 0, 0, 0, true);
                break;
            }
            case admin_alt_move_rnd: {
                Player targetPlayer = (activeChar.getTarget() != null) ? activeChar.getTarget().getPlayer() : null;
                if (targetPlayer == null) {
                    targetPlayer = activeChar;
                }
                targetPlayer.moveToLocation(Rnd.get(World.MAP_MIN_X, World.MAP_MAX_X), Rnd.get(World.MAP_MIN_Y, World.MAP_MAX_Y), Rnd.get(World.MAP_MIN_Z, World.MAP_MAX_Z), 0, true);
                break;
            }
        }
        return false;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_collapse_this,
        admin_collapse_this2,
        admin_alt_set_target_hwid,
        admin_alt_move_000,
        admin_alt_move_rnd
    }
}
