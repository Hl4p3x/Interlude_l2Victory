package ru.j2dev.gameserver.network.telnet.commands;

import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.telnet.TelnetCommand;
import ru.j2dev.gameserver.network.telnet.TelnetCommandHolder;
import ru.j2dev.gameserver.tables.GmListTable;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class TelnetWorld implements TelnetCommandHolder {
    private final Set<TelnetCommand> _commands;

    public TelnetWorld() {
        (_commands = new LinkedHashSet<>()).add(new TelnetCommand("find") {
            @Override
            public String getUsage() {
                return "find <name>";
            }

            @Override
            public String handle(final String[] args) {
                if (args.length == 0) {
                    return null;
                }
                final Iterable<Player> players = GameObjectsStorage.getPlayers();
                final Iterator<Player> itr = players.iterator();
                final StringBuilder sb = new StringBuilder();
                int count = 0;
                final Pattern pattern = Pattern.compile(args[0] + "\\S+", Pattern.CASE_INSENSITIVE);
                while (itr.hasNext()) {
                    final Player player = itr.next();
                    if (pattern.matcher(player.getName()).matches()) {
                        ++count;
                        sb.append(player).append("\n");
                    }
                }
                if (count == 0) {
                    sb.append("Player not found.").append("\n");
                } else {
                    sb.append("=================================================\n");
                    sb.append("Found: ").append(count).append(" players.").append("\n");
                }
                return sb.toString();
            }
        });
        _commands.add(new TelnetCommand("whois", "who") {
            @Override
            public String getUsage() {
                return "whois <name>";
            }

            @Override
            public String handle(final String[] args) {
                if (args.length == 0) {
                    return null;
                }
                final Player player = GameObjectsStorage.getPlayer(args[0]);
                if (player == null) {
                    return "Player not found.\n";
                }
                final StringBuilder sb = new StringBuilder();
                sb.append("Name: .................... ").append(player.getName()).append("\n");
                sb.append("ID: ...................... ").append(player.getObjectId()).append("\n");
                sb.append("Account Name: ............ ").append(player.getAccountName()).append("\n");
                sb.append("IP: ...................... ").append(player.getIP()).append("\n");
                sb.append("Level: ................... ").append(player.getLevel()).append("\n");
                sb.append("Location: ................ ").append(player.getLoc()).append("\n");
                if (player.getClan() != null) {
                    sb.append("Clan: .................... ").append(player.getClan().getName()).append("\n");
                    if (player.getAlliance() != null) {
                        sb.append("Ally: .................... ").append(player.getAlliance().getAllyName()).append("\n");
                    }
                }
                sb.append("Offline: ................. ").append(player.isInOfflineMode()).append("\n");
                sb.append(player).append("\n");
                return sb.toString();
            }
        });
        _commands.add(new TelnetCommand("gmlist", "gms") {
            @Override
            public String getUsage() {
                return "gmlist";
            }

            @Override
            public String handle(final String[] args) {
                final List<Player> gms = GmListTable.getAllGMs();
                final int count = gms.size();
                if (count == 0) {
                    return "GMs not found.\n";
                }
                final StringBuilder sb = new StringBuilder();
                for (Player gm : gms) {
                    sb.append(gm).append("\n");
                }
                sb.append("Found: ").append(count).append(" GMs.").append("\n");
                return sb.toString();
            }
        });
    }

    @Override
    public Set<TelnetCommand> getCommands() {
        return _commands;
    }
}
