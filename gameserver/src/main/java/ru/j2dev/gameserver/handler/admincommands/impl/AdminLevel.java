package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.instances.PetInstance;
import ru.j2dev.gameserver.tables.PetDataTable;

public class AdminLevel implements IAdminCommandHandler {
    private void setLevel(final Player activeChar, final GameObject target, final int level) {
        if (target == null || (!target.isPlayer() && !target.isPet())) {
            activeChar.sendPacket(Msg.INVALID_TARGET);
            return;
        }
        if (level < 1 || level > Experience.getMaxLevel()) {
            activeChar.sendMessage("You must specify level 1 - " + Experience.getMaxLevel());
            return;
        }
        if (target.isPlayer()) {
            final Long exp_add = Experience.LEVEL[level] - ((Player) target).getExp();
            ((Player) target).addExpAndSp(exp_add, 0L);
            return;
        }
        if (target.isPet()) {
            final Long exp_add = PetDataTable.getInstance().getInfo(((PetInstance) target).getNpcId(), level).getExp() - ((PetInstance) target).getExp();
            ((PetInstance) target).addExpAndSp(exp_add, 0L);
        }
    }

    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanEditChar) {
            return false;
        }
        final GameObject target = activeChar.getTarget();
        if (target == null || (!target.isPlayer() && !target.isPet())) {
            activeChar.sendPacket(Msg.INVALID_TARGET);
            return false;
        }
        switch (command) {
            case admin_add_level:
            case admin_addLevel: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //addLevel level");
                    return false;
                }
                int level;
                try {
                    level = Integer.parseInt(wordList[1]);
                } catch (NumberFormatException e) {
                    activeChar.sendMessage("You must specify level");
                    return false;
                }
                setLevel(activeChar, target, level + ((Creature) target).getLevel());
                break;
            }
            case admin_set_level:
            case admin_setLevel: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //setLevel level");
                    return false;
                }
                int level;
                try {
                    level = Integer.parseInt(wordList[1]);
                } catch (NumberFormatException e) {
                    activeChar.sendMessage("You must specify level");
                    return false;
                }
                setLevel(activeChar, target, level);
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
        admin_add_level,
        admin_addLevel,
        admin_set_level,
        admin_setLevel
    }
}
