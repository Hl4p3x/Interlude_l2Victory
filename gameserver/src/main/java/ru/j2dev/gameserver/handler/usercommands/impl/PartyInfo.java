package ru.j2dev.gameserver.handler.usercommands.impl;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.usercommands.IUserCommandHandler;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

public class PartyInfo implements IUserCommandHandler {
    private static final int[] COMMAND_IDS = {81};

    @Override
    public boolean useUserCommand(final int id, final Player activeChar) {
        if (id != COMMAND_IDS[0]) {
            return false;
        }
        final Party playerParty = activeChar.getParty();
        if (!activeChar.isInParty()) {
            return false;
        }
        final Player partyLeader = playerParty.getPartyLeader();
        if (partyLeader == null) {
            return false;
        }
        final int memberCount = playerParty.getMemberCount();
        final int lootDistribution = playerParty.getLootDistribution();
        activeChar.sendPacket(Msg._PARTY_INFORMATION_);
        switch (lootDistribution) {
            case 0: {
                activeChar.sendPacket(Msg.LOOTING_METHOD_FINDERS_KEEPERS);
                break;
            }
            case 3: {
                activeChar.sendPacket(Msg.LOOTING_METHOD_BY_TURN);
                break;
            }
            case 4: {
                activeChar.sendPacket(Msg.LOOTING_METHOD_BY_TURN_INCLUDING_SPOIL);
                break;
            }
            case 1: {
                activeChar.sendPacket(Msg.LOOTING_METHOD_RANDOM);
                break;
            }
            case 2: {
                activeChar.sendPacket(Msg.LOOTING_METHOD_RANDOM_INCLUDING_SPOIL);
                break;
            }
        }
        activeChar.sendPacket(new SystemMessage(1611).addString(partyLeader.getName()));
        activeChar.sendMessage(new CustomMessage("scripts.commands.user.PartyInfo.Members", activeChar).addNumber(memberCount));
        activeChar.sendPacket(Msg.__DASHES__);
        return true;
    }

    @Override
    public final int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
