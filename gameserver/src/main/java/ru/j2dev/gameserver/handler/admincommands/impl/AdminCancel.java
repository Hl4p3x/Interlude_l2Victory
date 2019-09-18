package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;

public class AdminCancel implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanEditChar) {
            return false;
        }
        switch (command) {
            case admin_cancel: {
                handleCancel(activeChar, (wordList.length > 1) ? wordList[1] : null);
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void handleCancel(final Player activeChar, final String targetName) {
        GameObject obj = activeChar.getTarget();
        if (targetName != null) {
            final Player plyr = World.getPlayer(targetName);
            if (plyr != null) {
                obj = plyr;
            } else {
                try {
                    final int radius = Math.max(Integer.parseInt(targetName), 100);
                    for (final Creature character : activeChar.getAroundCharacters(radius, 200)) {
                        character.getEffectList().stopAllEffects();
                    }
                    activeChar.sendMessage("Apply Cancel within " + radius + " unit radius.");
                    return;
                } catch (NumberFormatException e) {
                    activeChar.sendMessage("Enter valid player name or radius");
                    return;
                }
            }
        }
        if (obj == null) {
            obj = activeChar;
        }
        if (obj.isCreature()) {
            ((Creature) obj).getEffectList().stopAllEffects();
        } else {
            activeChar.sendPacket(Msg.INVALID_TARGET);
        }
    }

    private enum Commands {
        admin_cancel
    }
}
