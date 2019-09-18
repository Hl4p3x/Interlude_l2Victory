package ru.j2dev.gameserver.handler.admincommands.impl;

import org.apache.commons.lang3.math.NumberUtils;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;

public class AdminKill implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanEditNPC) {
            return false;
        }
        switch (command) {
            case admin_kill: {
                if (wordList.length == 1) {
                    handleKill(activeChar);
                    break;
                }
                handleKill(activeChar, wordList[1]);
                break;
            }
            case admin_damage: {
                handleDamage(activeChar, NumberUtils.toInt(wordList[1], 1));
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void handleKill(final Player activeChar) {
        handleKill(activeChar, null);
    }

    private void handleKill(final Player activeChar, final String player) {
        GameObject obj = activeChar.getTarget();
        if (player != null) {
            final Player plyr = World.getPlayer(player);
            if (plyr == null) {
                final int radius = Math.max(Integer.parseInt(player), 100);
                for (final Creature character : activeChar.getAroundCharacters(radius, 200)) {
                    if (!character.isDoor()) {
                        character.doDie(activeChar);
                    }
                }
                activeChar.sendMessage("Killed within " + radius + " unit radius.");
                return;
            }
            obj = plyr;
        }
        if (obj != null && obj.isCreature()) {
            final Creature target = (Creature) obj;
            target.doDie(activeChar);
        } else {
            activeChar.sendPacket(Msg.INVALID_TARGET);
        }
    }

    private void handleDamage(final Player activeChar, final int damage) {
        final GameObject obj = activeChar.getTarget();
        if (obj == null) {
            activeChar.sendPacket(Msg.SELECT_TARGET);
            return;
        }
        if (!obj.isCreature()) {
            activeChar.sendPacket(Msg.INVALID_TARGET);
            return;
        }
        final Creature cha = (Creature) obj;
        cha.reduceCurrentHp(damage, activeChar, null, true, true, false, false, false, false, true);
        activeChar.sendMessage("You gave " + damage + " damage to " + cha.getName() + ".");
    }

    private enum Commands {
        admin_kill,
        admin_damage
    }
}
