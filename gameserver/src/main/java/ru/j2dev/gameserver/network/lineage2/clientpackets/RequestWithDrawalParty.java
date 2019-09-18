package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.DimensionalRift;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;

public class RequestWithDrawalParty extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Party party = activeChar.getParty();
        if (party == null) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isOlyParticipant()) {
            activeChar.sendMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u0435\u0439\u0447\u0430\u0441 \u0432\u044b\u0439\u0442\u0438 \u0438\u0437 \u0433\u0440\u0443\u043f\u043f\u044b.");
            return;
        }
        final Reflection r = activeChar.getParty().getReflection();
        if (r instanceof DimensionalRift && activeChar.getReflection().equals(r)) {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestWithDrawalParty.Rift", activeChar));
        } else if (r != null && activeChar.isInCombat()) {
            activeChar.sendMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u0435\u0439\u0447\u0430\u0441 \u0432\u044b\u0439\u0442\u0438 \u0438\u0437 \u0433\u0440\u0443\u043f\u043f\u044b.");
        } else {
            activeChar.leaveParty();
        }
    }
}
