package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ManagePledgePower;

public class RequestPledgePower extends L2GameClientPacket {
    private int _rank;
    private int _action;
    private int _privs;

    @Override
    protected void readImpl() {
        _rank = readD();
        _action = readD();
        if (_action == 2) {
            _privs = readD();
        }
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (_action == 2) {
            if (_rank < 1 || _rank > 9) {
                return;
            }
            if (activeChar.getClan() != null && (activeChar.getClanPrivileges() & 0x10) == 0x10) {
                if (_rank == 9) {
                    _privs = (_privs & 0x8) + (_privs & 0x400) + (_privs & 0x8000) + (_privs & 0x800) + (_privs & 0x40000);
                }
                activeChar.getClan().setRankPrivs(_rank, _privs);
                activeChar.getClan().updatePrivsForRank(_rank);
            }
        } else if (activeChar.getClan() != null) {
            activeChar.sendPacket(new ManagePledgePower(activeChar, _action, _rank));
        } else {
            activeChar.sendActionFailed();
        }
    }
}
