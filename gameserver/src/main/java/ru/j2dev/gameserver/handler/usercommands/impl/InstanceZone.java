package ru.j2dev.gameserver.handler.usercommands.impl;

import ru.j2dev.gameserver.data.xml.holder.InstantZoneHolder;
import ru.j2dev.gameserver.handler.usercommands.IUserCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;

public class InstanceZone implements IUserCommandHandler {
    private static final int[] COMMAND_IDS = {114};

    @Override
    public boolean useUserCommand(final int id, final Player activeChar) {
        if (COMMAND_IDS[0] != id) {
            return false;
        }
        if (activeChar.getActiveReflection() != null) {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.INSTANT_ZONE_CURRENTLY_IN_USE_S1).addInstanceName(activeChar.getActiveReflection().getInstancedZoneId()));
        }
        boolean noLimit = true;
        boolean showMsg = false;
        for (final int i : activeChar.getInstanceReuses().keySet()) {
            final int limit = InstantZoneHolder.getInstance().getMinutesToNextEntrance(i, activeChar);
            if (limit > 0) {
                noLimit = false;
                if (!showMsg) {
                    activeChar.sendPacket(SystemMsg.INSTANCE_ZONE_TIME_LIMIT);
                    showMsg = true;
                }
                activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_WILL_BE_AVAILABLE_FOR_REUSE_AFTER_S2_HOURS_S3_MINUTES).addInstanceName(i).addInteger(limit / 60).addInteger(limit % 60));
            }
        }
        if (noLimit) {
            activeChar.sendPacket(SystemMsg.THERE_IS_NO_INSTANCE_ZONE_UNDER_A_TIME_LIMIT);
        }
        return true;
    }

    @Override
    public final int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
