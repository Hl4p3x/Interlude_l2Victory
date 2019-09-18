package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PledgeReceiveMemberInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PledgeShowMemberListUpdate;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

public class RequestPledgeSetAcademyMaster extends L2GameClientPacket {
    private int _mode;
    private String _sponsorName;
    private String _apprenticeName;

    @Override
    protected void readImpl() {
        _mode = readD();
        _sponsorName = readS(16);
        _apprenticeName = readS(16);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Clan clan = activeChar.getClan();
        if (clan == null) {
            return;
        }
        if ((activeChar.getClanPrivileges() & 0x100) == 0x100) {
            final UnitMember sponsor = activeChar.getClan().getAnyMember(_sponsorName);
            final UnitMember apprentice = activeChar.getClan().getAnyMember(_apprenticeName);
            if (sponsor != null && apprentice != null) {
                if (apprentice.getPledgeType() != -1 || sponsor.getPledgeType() == -1) {
                    return;
                }
                if (_mode == 1) {
                    if (sponsor.hasApprentice()) {
                        activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.MemberAlreadyHasApprentice", activeChar));
                        return;
                    }
                    if (apprentice.hasSponsor()) {
                        activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.ApprenticeAlreadyHasSponsor", activeChar));
                        return;
                    }
                    sponsor.setApprentice(apprentice.getObjectId());
                    clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(apprentice));
                    clan.broadcastToOnlineMembers(new SystemMessage(1755).addString(sponsor.getName()).addString(apprentice.getName()));
                } else {
                    if (!sponsor.hasApprentice()) {
                        activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.MemberHasNoApprentice", activeChar));
                        return;
                    }
                    sponsor.setApprentice(0);
                    clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(apprentice));
                    clan.broadcastToOnlineMembers(new SystemMessage(1763).addString(sponsor.getName()).addString(apprentice.getName()));
                }
                if (apprentice.isOnline()) {
                    apprentice.getPlayer().broadcastCharInfo();
                }
                activeChar.sendPacket(new PledgeReceiveMemberInfo(sponsor));
            }
        } else {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.NoMasterRights", activeChar));
        }
    }
}
