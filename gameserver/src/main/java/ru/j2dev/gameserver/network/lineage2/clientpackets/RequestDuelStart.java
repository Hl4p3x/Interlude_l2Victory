package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.EventHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.entity.events.EventType;
import ru.j2dev.gameserver.model.entity.events.impl.DuelEvent;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;

public class RequestDuelStart extends L2GameClientPacket {
    private String _name;
    private int _duelType;

    @Override
    protected void readImpl() {
        _name = readS(Config.CNAME_MAXLEN);
        _duelType = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        if (player.isActionsDisabled()) {
            player.sendActionFailed();
            return;
        }
        if (player.isProcessingRequest()) {
            player.sendPacket(SystemMsg.WAITING_FOR_ANOTHER_REPLY);
            return;
        }
        final Player target = World.getPlayer(_name);
        if (target == null || target == player) {
            player.sendPacket(SystemMsg.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
            return;
        }
        final DuelEvent duelEvent = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, _duelType);
        if (duelEvent == null) {
            return;
        }
        if (!duelEvent.canDuel(player, target, true)) {
            return;
        }
        if (target.isBusy()) {
            player.sendPacket((new SystemMessage2(SystemMsg.C1_IS_ON_ANOTHER_TASK)).addName(target));
            return;
        }
        duelEvent.askDuel(player, target);
    }
}
