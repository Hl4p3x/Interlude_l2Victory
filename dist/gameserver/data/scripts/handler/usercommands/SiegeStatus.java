package handler.usercommands;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;

public class SiegeStatus extends ScriptUserCommand {
    public static final int[] COMMANDS = {99};

    @Override
    public boolean useUserCommand(final int id, final Player player) {
        if (!player.isClanLeader()) {
            player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_MAY_ISSUE_COMMANDS);
            return false;
        }
        final Castle castle = player.getCastle();
        if (castle == null) {
            return false;
        }
        if (castle.getSiegeEvent().isInProgress() && !player.isNoble()) {
            player.sendPacket(SystemMsg.ONLY_A_CLAN_LEADER_THAT_IS_A_NOBLESSE_CAN_VIEW_THE_SIEGE_WAR_STATUS_WINDOW_DURING_A_SIEGE_WAR);
            return false;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5);
        msg.setFile("siege_status.htm");
        msg.replace("%name%", player.getName());
        msg.replace("%kills%", String.valueOf(0));
        msg.replace("%deaths%", String.valueOf(0));
        msg.replace("%type%", String.valueOf(0));
        player.sendPacket(msg);
        return true;
    }

    @Override
    public int[] getUserCommandList() {
        return COMMANDS;
    }
}
