package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Alliance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.tables.ClanTable;

public class RequestOustAlly extends L2GameClientPacket {
    private String _clanName;

    @Override
    protected void readImpl() {
        _clanName = readS(32);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Clan leaderClan = activeChar.getClan();
        if (leaderClan == null) {
            activeChar.sendActionFailed();
            return;
        }
        final Alliance alliance = leaderClan.getAlliance();
        if (alliance == null) {
            activeChar.sendPacket(Msg.YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS);
            return;
        }
        if (!activeChar.isAllyLeader()) {
            activeChar.sendPacket(Msg.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY);
            return;
        }
        if (_clanName == null) {
            return;
        }
        final Clan clan = ClanTable.getInstance().getClanByName(_clanName);
        if (clan != null) {
            if (!alliance.isMember(clan.getClanId())) {
                activeChar.sendActionFailed();
                return;
            }
            if (alliance.getLeader().equals(clan)) {
                activeChar.sendPacket(Msg.YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_ALLIANCE);
                return;
            }
            clan.broadcastToOnlineMembers(new SystemMessage("Your clan has been expelled from " + alliance.getAllyName() + " alliance."), new SystemMessage(468));
            clan.setAllyId(0);
            clan.setLeavedAlly();
            alliance.broadcastAllyStatus();
            alliance.removeAllyMember(clan.getClanId());
            alliance.setExpelledMember();
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.ClanDismissed", activeChar).addString(clan.getName()).addString(alliance.getAllyName()));
        }
    }
}
