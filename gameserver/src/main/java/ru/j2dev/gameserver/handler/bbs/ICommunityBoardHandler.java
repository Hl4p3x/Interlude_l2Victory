package ru.j2dev.gameserver.handler.bbs;

import ru.j2dev.gameserver.model.Player;

public interface ICommunityBoardHandler {
    String[] getBypassCommands();

    void onBypassCommand(final Player p0, final String p1);

    void onWriteCommand(final Player p0, final String p1, final String p2, final String p3, final String p4, final String p5, final String p6);
}
