package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.actor.instances.player.Friend;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

import java.util.Map;
import java.util.Map.Entry;

public class RequestFriendInfoList extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        activeChar.sendPacket(Msg._FRIENDS_LIST_);
        final Map<Integer, Friend> _list = activeChar.getFriendList().getList();
        for (final Entry<Integer, Friend> entry : _list.entrySet()) {
            final Player friend = World.getPlayer(entry.getKey());
            if (friend != null) {
                activeChar.sendPacket(new SystemMessage(488).addName(friend));
            } else {
                activeChar.sendPacket(new SystemMessage(489).addString(entry.getValue().getName()));
            }
        }
        activeChar.sendPacket(Msg.__EQUALS__);
    }
}
