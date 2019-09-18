package ru.j2dev.gameserver.handler.usercommands.impl;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.GameTimeController;
import ru.j2dev.gameserver.handler.usercommands.IUserCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Time implements IUserCommandHandler {
    private static final int[] COMMAND_IDS = {77};
    private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
    private static final SimpleDateFormat sf = new SimpleDateFormat("H:mm");

    static {
        df.setMinimumIntegerDigits(2);
    }

    @Override
    public boolean useUserCommand(final int id, final Player activeChar) {
        if (COMMAND_IDS[0] != id) {
            return false;
        }
        final int h = GameTimeController.getInstance().getGameHour();
        final int m = GameTimeController.getInstance().getGameMin();
        SystemMessage sm;
        if (GameTimeController.getInstance().isNowNight()) {
            sm = new SystemMessage(928);
        } else {
            sm = new SystemMessage(927);
        }
        sm.addString(df.format(h)).addString(df.format(m));
        activeChar.sendPacket(sm);
        if (Config.ALT_SHOW_SERVER_TIME) {
            activeChar.sendMessage(new CustomMessage("usercommandhandlers.Time.ServerTime", activeChar, sf.format(new Date(System.currentTimeMillis()))));
        }
        return true;
    }

    @Override
    public final int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
