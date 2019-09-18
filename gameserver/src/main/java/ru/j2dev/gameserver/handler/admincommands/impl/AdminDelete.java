package ru.j2dev.gameserver.handler.admincommands.impl;

import org.apache.commons.lang3.math.NumberUtils;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Spawner;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class AdminDelete implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanEditNPC) {
            return false;
        }
        switch (command) {
            case admin_delete: {
                final GameObject obj = (wordList.length == 1) ? activeChar.getTarget() : GameObjectsStorage.getNpc(NumberUtils.toInt(wordList[1]));
                if (obj != null && obj.isNpc()) {
                    final NpcInstance target = (NpcInstance) obj;
                    target.deleteMe();
                    final Spawner spawn = target.getSpawn();
                    if (spawn != null) {
                        spawn.stopRespawn();
                    }
                    break;
                }
                activeChar.sendPacket(Msg.INVALID_TARGET);
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
        admin_delete
    }
}
