package ru.j2dev.gameserver.network.telnet.commands;

import org.apache.commons.lang3.time.DurationFormatUtils;
import ru.j2dev.commons.lang.StatsUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.GameTimeController;
import ru.j2dev.gameserver.Shutdown;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.telnet.TelnetCommand;
import ru.j2dev.gameserver.network.telnet.TelnetCommandHolder;
import ru.j2dev.gameserver.tables.GmListTable;
import ru.j2dev.gameserver.utils.Util;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class TelnetStatus implements TelnetCommandHolder {
    private final Set<TelnetCommand> _commands;

    public TelnetStatus() {
        (_commands = new LinkedHashSet<>()).add(new TelnetCommand("status", "s") {
            @Override
            public String getUsage() {
                return "status";
            }

            @Override
            public String handle(final String[] args) {
                final StringBuilder sb = new StringBuilder();
                final int[] stats = World.getStats();
                sb.append("Server Status: ").append("\n");
                sb.append("Players: ................. ").append(stats[12]).append("/").append(Config.MAXIMUM_ONLINE_USERS).append("\n");
                sb.append("     Online: ............. ").append(stats[12] - stats[13]).append("\n");
                sb.append("     Offline: ............ ").append(stats[13]).append("\n");
                sb.append("     GM: ................. ").append(GmListTable.getAllGMs().size()).append("\n");
                sb.append("Objects: ................. ").append(stats[10]).append("\n");
                sb.append("Characters: .............. ").append(stats[11]).append("\n");
                sb.append("Summons: ................. ").append(stats[18]).append("\n");
                sb.append("Npcs: .................... ").append(stats[15]).append("/").append(stats[14]).append("\n");
                sb.append("Monsters: ................ ").append(stats[16]).append("\n");
                sb.append("Minions: ................. ").append(stats[17]).append("\n");
                sb.append("Doors: ................... ").append(stats[19]).append("\n");
                sb.append("Items: ................... ").append(stats[20]).append("\n");
                sb.append("Reflections: ............. ").append(ReflectionManager.getInstance().getAll().length).append("\n");
                sb.append("Regions: ................. ").append(stats[0]).append("\n");
                sb.append("     Active: ............. ").append(stats[1]).append("\n");
                sb.append("     Inactive: ........... ").append(stats[2]).append("\n");
                sb.append("     Null: ............... ").append(stats[3]).append("\n");
                sb.append("Game Time: ............... ").append(getGameTime()).append("\n");
                sb.append("Real Time: ............... ").append(getCurrentTime()).append("\n");
                sb.append("Start Time: .............. ").append(getStartTime()).append("\n");
                sb.append("Uptime: .................. ").append(getUptime()).append("\n");
                sb.append("Shutdown: ................ ").append(Util.formatTime(Shutdown.getInstance().getSeconds())).append("/").append(Shutdown.getInstance().getMode()).append("\n");
                sb.append("Threads: ................. ").append(Thread.activeCount()).append("\n");
                sb.append("RAM Used: ................ ").append(StatsUtils.getMemUsedMb()).append("\n");
                return sb.toString();
            }
        });
    }

    public static String getGameTime() {
        final int t = GameTimeController.getInstance().getGameTime();
        final int h = t / 60;
        final int m = t % 60;
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, m);
        return format.format(cal.getTime());
    }

    public static String getUptime() {
        return DurationFormatUtils.formatDurationHMS(ManagementFactory.getRuntimeMXBean().getUptime());
    }

    public static String getStartTime() {
        return new Date(ManagementFactory.getRuntimeMXBean().getStartTime()).toString();
    }

    public static String getCurrentTime() {
        return new Date().toString();
    }

    @Override
    public Set<TelnetCommand> getCommands() {
        return _commands;
    }
}
