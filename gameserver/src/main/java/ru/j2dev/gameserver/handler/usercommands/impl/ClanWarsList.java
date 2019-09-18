package ru.j2dev.gameserver.handler.usercommands.impl;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.usercommands.IUserCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Alliance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class ClanWarsList implements IUserCommandHandler {
    private static final int[] COMMAND_IDS = {88, 89, 90};

    @Override
    public boolean useUserCommand(final int id, final Player activeChar) {
        if (id != COMMAND_IDS[0] && id != COMMAND_IDS[1] && id != COMMAND_IDS[2]) {
            return false;
        }
        final Clan clan = activeChar.getClan();
        if (clan == null) {
            activeChar.sendPacket(Msg.NOT_JOINED_IN_ANY_CLAN);
            return false;
        }
        List<Clan> data = new ArrayList<>();
        switch (id) {
            case 88:
                activeChar.sendPacket(Msg._ATTACK_LIST_);
                data = clan.getEnemyClans();
                break;
            case 89:
                activeChar.sendPacket(Msg._UNDER_ATTACK_LIST_);
                data = clan.getAttackerClans();
                break;
            default:
                activeChar.sendPacket(Msg._WAR_LIST_);
                for (final Clan c : clan.getEnemyClans()) {
                    if (clan.getAttackerClans().contains(c)) {
                        data.add(c);
                    }
                }
                break;
        }
        for (final Clan c : data) {
            final String clanName = c.getName();
            final Alliance alliance = c.getAlliance();
            SystemMessage sm;
            if (alliance != null) {
                sm = new SystemMessage(1200).addString(clanName).addString(alliance.getAllyName());
            } else {
                sm = new SystemMessage(1202).addString(clanName);
            }
            activeChar.sendPacket(sm);
        }
        activeChar.sendPacket(Msg.__EQUALS__);
        return true;
    }

    @Override
    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
