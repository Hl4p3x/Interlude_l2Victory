package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;
import ru.j2dev.gameserver.tables.ClanTable;

public class RequestStopPledgeWar extends L2GameClientPacket {
    private String _pledgeName;

    @Override
    protected void readImpl() {
        _pledgeName = readS(32);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Clan playerClan = activeChar.getClan();
        if (playerClan == null) {
            return;
        }
        if ((activeChar.getClanPrivileges() & 0x20) != 0x20) {
            activeChar.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT, ActionFail.STATIC);
            return;
        }
        final Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
        if (clan == null) {
            activeChar.sendPacket(SystemMsg.CLAN_NAME_IS_INVALID, ActionFail.STATIC);
            return;
        }
        if (!playerClan.isAtWarWith(clan.getClanId())) {
            activeChar.sendPacket(Msg.YOU_HAVE_NOT_DECLARED_A_CLAN_WAR_TO_S1_CLAN, ActionFail.STATIC);
            return;
        }
        for (final UnitMember mbr : playerClan) {
            if (mbr.isOnline() && mbr.getPlayer().isInCombat()) {
                activeChar.sendPacket(Msg.A_CEASE_FIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE, ActionFail.STATIC);
                return;
            }
        }
        ClanTable.getInstance().stopClanWar(playerClan, clan);
    }
}
