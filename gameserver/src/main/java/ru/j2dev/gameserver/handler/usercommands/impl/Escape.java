package ru.j2dev.gameserver.handler.usercommands.impl;

import ru.j2dev.gameserver.handler.usercommands.IUserCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.scripts.Scripts;
import ru.j2dev.gameserver.tables.SkillTable;

public class Escape implements IUserCommandHandler {
    private static final int[] COMMAND_IDS = {52};

    @Override
    public boolean useUserCommand(final int id, final Player activeChar) {
        if (id != COMMAND_IDS[0]) {
            return false;
        }
        if (activeChar.isMovementDisabled() || activeChar.isOlyParticipant()) {
            return false;
        }
        if (activeChar.getTeleMode() != 0 || !activeChar.getPlayerAccess().UseTeleport || isEventParticipant(activeChar)) {
            activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar));
            return false;
        }
        if (activeChar.isInDuel() || activeChar.getTeam() != TeamType.NONE) {
            activeChar.sendMessage(new CustomMessage("common.RecallInDuel", activeChar));
            return false;
        }
        activeChar.abortAttack(true, true);
        activeChar.abortCast(true, true);
        activeChar.stopMove();
        Skill skill;
        if (activeChar.getPlayerAccess().FastUnstuck) {
            skill = SkillTable.getInstance().getInfo(1050, 2);
        } else {
            skill = SkillTable.getInstance().getInfo(2099, 1);
        }
        if (skill != null && skill.checkCondition(activeChar, activeChar, false, false, true)) {
            activeChar.getAI().Cast(skill, activeChar, false, true);
        }
        return true;
    }

    private boolean isEventParticipant(final Player player) {
        return (boolean) Scripts.getInstance().callScripts(player, "events.TvT2.PvPEvent", "isEventPartisipant");
    }

    @Override
    public final int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
