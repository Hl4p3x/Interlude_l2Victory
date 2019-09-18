package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.InvisibleType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CameraMode;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SpecialCamera;

public class AdminCamera implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().Menu) {
            return false;
        }
        switch (command) {
            case admin_freelook: {
                if (fullString.length() > 15) {
                    fullString = fullString.substring(15);
                    final int mode = Integer.parseInt(fullString);
                    if (mode == 1) {
                        activeChar.setInvisibleType(InvisibleType.NORMAL);
                        activeChar.setIsInvul(true);
                        activeChar.setNoChannel(-1L);
                        activeChar.setFlying(true);
                    } else {
                        activeChar.setInvisibleType(InvisibleType.NONE);
                        activeChar.setIsInvul(false);
                        activeChar.setNoChannel(0L);
                        activeChar.setFlying(false);
                    }
                    activeChar.sendPacket(new CameraMode(mode));
                    break;
                }
                activeChar.sendMessage("Usage: //freelook 1 or //freelook 0");
                return false;
            }
            case admin_cinematic: {
                final int id = Integer.parseInt(wordList[1]);
                final int dist = Integer.parseInt(wordList[2]);
                final int yaw = Integer.parseInt(wordList[3]);
                final int pitch = Integer.parseInt(wordList[4]);
                final int time = Integer.parseInt(wordList[5]);
                final int duration = Integer.parseInt(wordList[6]);
                activeChar.sendPacket(new SpecialCamera(id, dist, yaw, pitch, time, duration));
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
        admin_freelook,
        admin_cinematic
    }
}
