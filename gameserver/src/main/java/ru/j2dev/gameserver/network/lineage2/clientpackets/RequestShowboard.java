package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.handler.bbs.CommunityBoardManager;
import ru.j2dev.gameserver.handler.bbs.ICommunityBoardHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

public class RequestShowboard extends L2GameClientPacket {
    private int _unknown;

    @Override
    public void readImpl() {
        _unknown = readD();
    }

    @Override
    public void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (Config.COMMUNITYBOARD_ENABLED) {
            final ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(Config.BBS_DEFAULT);
            if (handler != null) {
                handler.onBypassCommand(activeChar, Config.BBS_DEFAULT);
            }
        } else {
            activeChar.sendPacket(new SystemMessage(938));
        }
    }
}
