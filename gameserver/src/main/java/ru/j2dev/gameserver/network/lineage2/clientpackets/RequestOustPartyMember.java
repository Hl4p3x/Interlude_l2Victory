package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.DimensionalRift;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;

public class RequestOustPartyMember extends L2GameClientPacket {
    private String _name;

    @Override
    protected void readImpl() {
        _name = readS(16);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Party party = activeChar.getParty();
        if (party == null || !activeChar.getParty().isLeader(activeChar)) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isOlyParticipant()) {
            activeChar.sendMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u0435\u0439\u0447\u0430\u0441 \u0432\u044b\u0439\u0442\u0438 \u0438\u0437 \u0433\u0440\u0443\u043f\u043f\u044b.");
            return;
        }
        final Player member = party.getPlayerByName(_name);
        if (member == activeChar) {
            activeChar.sendActionFailed();
            return;
        }
        if (member == null) {
            activeChar.sendActionFailed();
            return;
        }
        final Reflection r = party.getReflection();
        if (r instanceof DimensionalRift && member.getReflection().equals(r)) {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustPartyMember.CantOustInRift", activeChar));
        } else if (r != null && !(r instanceof DimensionalRift)) {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustPartyMember.CantOustInDungeon", activeChar));
        } else {
            party.removePartyMember(member, true);
        }
    }
}
