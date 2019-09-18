package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.manager.PetitionManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Say2;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.tables.GmListTable;

public final class RequestPetitionCancel extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (PetitionManager.getInstance().isPlayerInConsultation(activeChar)) {
            if (activeChar.isGM()) {
                PetitionManager.getInstance().endActivePetition(activeChar);
            } else {
                activeChar.sendPacket(new SystemMessage(407));
            }
        } else if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar)) {
            if (PetitionManager.getInstance().cancelActivePetition(activeChar)) {
                final int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar);
                activeChar.sendPacket(new SystemMessage(736).addString(String.valueOf(numRemaining)));
                final String msgContent = activeChar.getName() + " has canceled a pending petition.";
                GmListTable.broadcastToGMs(new Say2(activeChar.getObjectId(), ChatType.HERO_VOICE, "Petition System", msgContent));
            } else {
                activeChar.sendPacket(new SystemMessage(393));
            }
        } else {
            activeChar.sendPacket(new SystemMessage(738));
        }
    }
}
