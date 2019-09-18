package ru.j2dev.gameserver.network.telnet.commands;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.telnet.TelnetCommand;
import ru.j2dev.gameserver.network.telnet.TelnetCommandHolder;
import ru.j2dev.gameserver.utils.AdminFunctions;
import ru.j2dev.gameserver.utils.AutoBan;

import java.util.LinkedHashSet;
import java.util.Set;

public class TelnetBan implements TelnetCommandHolder {
    private final Set<TelnetCommand> _commands;

    public TelnetBan() {
        (_commands = new LinkedHashSet<>()).add(new TelnetCommand("kick") {
            @Override
            public String getUsage() {
                return "kick <name>";
            }

            @Override
            public String handle(final String[] args) {
                if (args.length == 0 || args[0].isEmpty()) {
                    return null;
                }
                if (AdminFunctions.kick(args[0], "telnet")) {
                    return "Player kicked.\n";
                }
                return "Player not found.\n";
            }
        });
        _commands.add(new TelnetCommand("chat_ban") {
            @Override
            public String getUsage() {
                return "chat_ban <name> <period>";
            }

            @Override
            public String handle(final String[] args) {
                if (args.length == 0 || args[0].isEmpty()) {
                    return null;
                }
                final int period = (args.length > 1 && !args[1].isEmpty()) ? Integer.parseInt(args[1]) : -1;
                return AdminFunctions.banChat(null, "GMTelnet", args[0], period, "telnet banned") + "\n";
            }
        });
        _commands.add(new TelnetCommand("char_ban") {
            @Override
            public String getUsage() {
                return "char_ban <name> <days>";
            }

            @Override
            public String handle(final String[] args) {
                if (args.length == 0 || args[0].isEmpty()) {
                    return null;
                }
                final String playerName = args[0];
                final int period = (args.length > 1 && !args[1].isEmpty()) ? Integer.parseInt(args[1]) : -1;
                if (period == 0) {
                    if (!AutoBan.banOfflinePlayer(playerName, 0, 0, "unban", "telnet")) {
                        return "Can't unban \"" + playerName + "\".\n";
                    }
                    return "\"" + playerName + "\" unbanned.\n";
                } else {
                    if (!AutoBan.banOfflinePlayer(playerName, -100, period, "unban", "telnet")) {
                        return "Can't ban \"" + playerName + "\".\n";
                    }
                    final Player player = World.getPlayer(playerName);
                    if (player != null) {
                        player.kick();
                    }
                    return "\"" + playerName + "\" banned.\n";
                }
            }
        });
    }

    @Override
    public Set<TelnetCommand> getCommands() {
        return _commands;
    }
}
