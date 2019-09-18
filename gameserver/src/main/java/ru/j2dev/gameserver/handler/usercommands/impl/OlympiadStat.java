package ru.j2dev.gameserver.handler.usercommands.impl;

import ru.j2dev.gameserver.handler.usercommands.IUserCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager.NobleRecord;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;

public class OlympiadStat implements IUserCommandHandler {
    private static final int[] COMMAND_IDS = {109};

    @Override
    public boolean useUserCommand(final int id, final Player activeChar) {
        if (id != COMMAND_IDS[0]) {
            return false;
        }
        if (!activeChar.isNoble()) {
            activeChar.sendPacket(SystemMsg.THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE);
            return true;
        }
        final NobleRecord nr = NoblessManager.getInstance().getNobleRecord(activeChar.getObjectId());
        CustomMessage sm = new CustomMessage("Olympiad.stat", activeChar);
        sm = sm.addNumber(Math.max(0, nr.comp_done));
        sm = sm.addNumber(Math.max(0, nr.comp_win));
        sm = sm.addNumber(Math.max(0, nr.comp_loose));
        sm = sm.addNumber(Math.max(0, nr.points_current));
        activeChar.sendMessage(sm);
        return true;
    }

    @Override
    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
