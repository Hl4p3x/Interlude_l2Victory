package ru.j2dev.gameserver.network.telnet;

import java.util.Set;

public interface TelnetCommandHolder {
    Set<TelnetCommand> getCommands();
}
