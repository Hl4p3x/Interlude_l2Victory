package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;

public class AdminHeal implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().Heal) {
            return false;
        }
        switch (command) {
            case admin_heal: {
                if (wordList.length == 1) {
                    handleRes(activeChar);
                    break;
                }
                handleRes(activeChar, wordList[1]);
                break;
            }
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
            if (plyr == null) {
                final int radius = Math.max(Integer.parseInt(player), 100);
                for (final Creature character : activeChar.getAroundCharacters(radius, 200)) {
                    character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
                    if (character.isPlayer()) {
                        character.setCurrentCp(character.getMaxCp());
                    }
                }
                activeChar.sendMessage("Healed within " + radius + " unit radius.");
                return;
            }
            obj = plyr;
        }
        if (obj == null) {
            obj = activeChar;
        }
        if (obj instanceof Creature) {
            final Creature target = (Creature) obj;
            target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
            if (target.isPlayer()) {
                target.setCurrentCp(target.getMaxCp());
            }
        } else {
            activeChar.sendPacket(Msg.INVALID_TARGET);
        }
    }

    private enum Commands {
        admin_heal
    }
}
