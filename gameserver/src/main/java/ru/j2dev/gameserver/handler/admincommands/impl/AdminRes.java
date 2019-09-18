package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class AdminRes implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().Res) {
            return false;
        }
        if (fullString.startsWith("admin_res ")) {
            handleRes(activeChar, wordList[1]);
        }
        if ("admin_res".equals(fullString)) {
            handleRes(activeChar);
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void handleRes(final Player activeChar) {
        handleRes(activeChar, null);
    }

    private void handleRes(final Player activeChar, final String player) {
        GameObject obj = activeChar.getTarget();
        if (player != null) {
            final Player plyr = World.getPlayer(player);
            if (plyr != null) {
                obj = plyr;
            } else {
                try {
                    final int radius = Math.max(Integer.parseInt(player), 100);
                    for (final Creature character : activeChar.getAroundCharacters(radius, radius)) {
                        handleRes(character);
                    }
                    activeChar.sendMessage("Resurrected within " + radius + " unit radius.");
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
        if (obj instanceof Creature) {
            handleRes((Creature) obj);
        } else {
            activeChar.sendPacket(Msg.INVALID_TARGET);
        }
    }

    private void handleRes(final Creature target) {
        if (!target.isDead()) {
            return;
        }
        if (target.isPlayable()) {
            if (target.isPlayer()) {
                ((Player) target).doRevive(100.0);
            } else {
                ((Playable) target).doRevive();
            }
        } else if (target.isNpc()) {
            ((NpcInstance) target).stopDecay();
        }
        target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp(), true);
        target.setCurrentCp(target.getMaxCp());
    }

    private enum Commands {
        admin_res
    }
}
