package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.SevenSigns;

public class AdminSS implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().Menu) {
            return false;
        }
        switch (command) {
            case admin_ssq_change: {
                if (wordList.length > 2) {
                    final int period = Integer.parseInt(wordList[1]);
                    final int minutes = Integer.parseInt(wordList[2]);
                    SevenSigns.getInstance().changePeriod(period, minutes * 60);
                    break;
                }
                if (wordList.length > 1) {
                    final int period = Integer.parseInt(wordList[1]);
                    SevenSigns.getInstance().changePeriod(period);
                    break;
                }
                SevenSigns.getInstance().changePeriod();
                break;
            }
            case admin_ssq_time: {
                if (wordList.length > 1) {
                    final int time = Integer.parseInt(wordList[1]);
                    SevenSigns.getInstance().setTimeToNextPeriodChange(time);
                    break;
                }
                break;
            }
            case admin_ssq_cabal: {
                if (wordList.length > 3) {
                    final int player = Integer.parseInt(wordList[1]);
                    final int cabal = Integer.parseInt(wordList[2]);
                    final int seal = Integer.parseInt(wordList[3]);
                    SevenSigns.getInstance().setPlayerInfo(player, cabal, seal);
                    break;
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
        admin_ssq_change,
        admin_ssq_time,
        admin_ssq_cabal
    }
}
