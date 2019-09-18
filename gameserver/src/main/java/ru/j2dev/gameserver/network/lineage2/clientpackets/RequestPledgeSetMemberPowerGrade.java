package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;

public class RequestPledgeSetMemberPowerGrade extends L2GameClientPacket {
    private int _powerGrade;
    private String _name;

    @Override
    protected void readImpl() {
        _name = readS(16);
        _powerGrade = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (_powerGrade < 1 || _powerGrade > 9) {
            return;
        }
        final Clan clan = activeChar.getClan();
        if (clan == null) {
            return;
        }
        if ((activeChar.getClanPrivileges() & 0x10) == 0x10) {
            final UnitMember member = activeChar.getClan().getAnyMember(_name);
            if (member != null) {
                if (Clan.isAcademy(member.getPledgeType())) {
                    activeChar.sendMessage("You cannot change academy member grade.");
                    return;
                }
                if (_powerGrade > 5) {
                    member.setPowerGrade(clan.getAffiliationRank(member.getPledgeType()));
                } else {
                    member.setPowerGrade(_powerGrade);
                }
                if (member.isOnline()) {
                    member.getPlayer().sendUserInfo();
                }
            } else {
                activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeSetMemberPowerGrade.NotBelongClan", activeChar));
            }
        } else {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeSetMemberPowerGrade.HaveNotAuthority", activeChar));
        }
    }
}
