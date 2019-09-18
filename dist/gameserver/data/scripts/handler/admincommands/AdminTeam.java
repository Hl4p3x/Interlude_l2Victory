package handler.admincommands;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;

public class AdminTeam extends ScriptAdminCommand {
    @Override
    public boolean useAdminCommand(final Enum comm, final String[] wordList, final String fullString, final Player activeChar) {
        TeamType team = TeamType.NONE;
        if (wordList.length >= 2) {
            for (final TeamType t : TeamType.values()) {
                if (wordList[1].equalsIgnoreCase(t.name())) {
                    team = t;
                }
            }
        }
        final GameObject object = activeChar.getTarget();
        if (object == null || !object.isCreature()) {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET);
            return false;
        }
        ((Creature) object).setTeam(team);
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    enum Commands {
        admin_setteam
    }
}
