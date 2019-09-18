package ru.j2dev.gameserver.handler.admincommands.impl;

import org.apache.commons.lang3.math.NumberUtils;
import ru.j2dev.commons.lang.StatsUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.GameTimeController;
import ru.j2dev.gameserver.Shutdown;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AdminShutdown implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanRestart) {
            return false;
        }
        try {
            switch (command) {
                case admin_server_shutdown: {
                    Shutdown.getInstance().schedule(NumberUtils.toInt(wordList[1], -1), 0);
                    break;
                }
                case admin_server_restart: {
                    Shutdown.getInstance().schedule(NumberUtils.toInt(wordList[1], -1), 2);
                    break;
                }
                case admin_server_abort: {
                    Shutdown.getInstance().cancel();
                    break;
                }
            }
        } catch (Exception e) {
            sendHtmlForm(activeChar);
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void sendHtmlForm(final Player activeChar) {
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

        final int t = GameTimeController.getInstance().getGameTime();
        final int h = t / 60;
        final int m = t % 60;
        final SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, m);

        final StringBuilder replyMSG = new StringBuilder("<html><body>");
        replyMSG.append("<table width=260><tr>");
        if (activeChar.isLangRus()) {
            replyMSG.append("<td width=40><button value=\"Главная\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
            replyMSG.append("<td width=180><center>Настройки сервера</center></td>");
            replyMSG.append("<td width=40><button value=\"Назад\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        } else {
            replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
            replyMSG.append("<td width=180><center>Server Management Menu</center></td>");
            replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");

        }
        replyMSG.append("</tr></table>");
        replyMSG.append("<br><br>");
        replyMSG.append("<table>");
        replyMSG.append(activeChar.isLangRus() ? "<tr><td>Игроков онлайн: " : "<tr><td>Players Online: ").append(GameObjectsStorage.getAllOnlinePlayerCount()).append("</td></tr>");
        replyMSG.append(activeChar.isLangRus() ? "<tr><td>Фейк игроки: " : "<tr><td>Phantoms: ").append(GameObjectsStorage.getAllOnlinePhantomsCount()).append("</td></tr>");
        replyMSG.append(activeChar.isLangRus() ? "<tr><td>Офф.трейд: " : "<tr><td>Off-Traders: ").append(GameObjectsStorage.getAllOfflineCount()).append("</td></tr>");
        replyMSG.append(activeChar.isLangRus() ? "<tr><td>Использование памяти: " : "<tr><td>Used Memory: ").append(StatsUtils.getUsedMem()).append(" Mb.").append("/").append(StatsUtils.getTotalMem()).append(" Mb.").append("</td></tr>");
        replyMSG.append(activeChar.isLangRus() ? "<tr><td>Рейты сервера: " : "<tr><td>Server Rates: ").append(Config.RATE_XP).append("x, ").append(Config.RATE_SP).append("x, ").append(Config.RATE_DROP_ADENA).append("x, ").append(Config.RATE_DROP_ITEMS).append("x</td></tr>");
        replyMSG.append(activeChar.isLangRus() ? "<tr><td>Игровое время: " : "<tr><td>Game Time: ").append(sdf.format(cal.getTime())).append("</td></tr>");
        replyMSG.append(activeChar.isLangRus() ? "<tr><td>Время сервера: " : "<tr><td>Server Time: ").append(sdf.format(new Date(System.currentTimeMillis()))).append("</td></tr>");
        replyMSG.append("</table><br>");
        replyMSG.append("<table width=270>");
        replyMSG.append(activeChar.isLangRus() ? "<tr><td>Введите время в секундах до отключения :</td></tr>" : "<tr><td>Enter in seconds the time till the server shutdowns :</td></tr>");
        replyMSG.append("<br>");
        replyMSG.append(activeChar.isLangRus() ? "<tr><td><center>Секунды до: <edit var=\"shutdown_time\" width=60></center></td></tr>" : "<tr><td><center>Seconds till: <edit var=\"shutdown_time\" width=60></center></td></tr>");
        replyMSG.append("</table><br>");
        replyMSG.append("<center><table><tr><td>");
        if (activeChar.isLangRus()) {
            replyMSG.append("<button value=\"Выключить\" action=\"bypass -h admin_server_shutdown $shutdown_time\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
            replyMSG.append("<button value=\"Перезагрузить\" action=\"bypass -h admin_server_restart $shutdown_time\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
            replyMSG.append("<button value=\"Отмена\" action=\"bypass -h admin_server_abort\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\">");
        } else {
            replyMSG.append("<button value=\"Shutdown\" action=\"bypass -h admin_server_shutdown $shutdown_time\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
            replyMSG.append("<button value=\"Restart\" action=\"bypass -h admin_server_restart $shutdown_time\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
            replyMSG.append("<button value=\"Abort\" action=\"bypass -h admin_server_abort\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\">");
        }
        replyMSG.append("</td></tr></table></center>");
        replyMSG.append("</body></html>");

        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);
    }

    private enum Commands {
        admin_server_shutdown,
        admin_server_restart,
        admin_server_abort
    }
}
