package ru.j2dev.gameserver.handler.admincommands.impl;

import org.apache.commons.lang3.math.NumberUtils;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.manager.PetitionManager;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

public class AdminPetition implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        if (!activeChar.getPlayerAccess().CanEditChar) {
            return false;
        }
        int petitionId = NumberUtils.toInt((wordList.length > 1) ? wordList[1] : "-1", -1);
        final Commands command = (Commands) comm;
        switch (command) {
            case admin_view_petitions: {
                PetitionManager.getInstance().sendPendingPetitionList(activeChar);
                break;
            }
            case admin_view_petition: {
                PetitionManager.getInstance().viewPetition(activeChar, petitionId);
                break;
            }
            case admin_accept_petition: {
                if (petitionId < 0) {
                    activeChar.sendMessage("Usage: //accept_petition id");
                    return false;
                }
                if (PetitionManager.getInstance().isPlayerInConsultation(activeChar)) {
                    activeChar.sendPacket(new SystemMessage(390));
                    return true;
                }
                if (PetitionManager.getInstance().isPetitionInProcess(petitionId)) {
                    activeChar.sendPacket(new SystemMessage(407));
                    return true;
                }
                if (!PetitionManager.getInstance().acceptPetition(activeChar, petitionId)) {
                    activeChar.sendPacket(new SystemMessage(388));
                    break;
                }
                break;
            }
            case admin_reject_petition: {
                if (petitionId < 0) {
                    activeChar.sendMessage("Usage: //accept_petition id");
                    return false;
                }
                if (!PetitionManager.getInstance().rejectPetition(activeChar, petitionId)) {
                    activeChar.sendPacket(new SystemMessage(393));
                }
                PetitionManager.getInstance().sendPendingPetitionList(activeChar);
                break;
            }
            case admin_reset_petitions: {
                if (PetitionManager.getInstance().isPetitionInProcess()) {
                    activeChar.sendPacket(new SystemMessage(407));
                    return false;
                }
                PetitionManager.getInstance().clearPendingPetitions();
                PetitionManager.getInstance().sendPendingPetitionList(activeChar);
                break;
            }
            case admin_force_peti: {
                if (fullString.length() < 11) {
                    activeChar.sendMessage("Usage: //force_peti text");
                    return false;
                }
                try {
                    final GameObject targetChar = activeChar.getTarget();
                    if (!(targetChar instanceof Player)) {
                        activeChar.sendPacket(new SystemMessage(109));
                        return false;
                    }
                    final Player targetPlayer = (Player) targetChar;
                    petitionId = PetitionManager.getInstance().submitPetition(targetPlayer, fullString.substring(10), 9);
                    PetitionManager.getInstance().acceptPetition(activeChar, petitionId);
                } catch (StringIndexOutOfBoundsException e) {
                    activeChar.sendMessage("Usage: //force_peti text");
                    return false;
                }
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_view_petitions,
        admin_view_petition,
        admin_accept_petition,
        admin_reject_petition,
        admin_reset_petitions,
        admin_force_peti
    }
}
