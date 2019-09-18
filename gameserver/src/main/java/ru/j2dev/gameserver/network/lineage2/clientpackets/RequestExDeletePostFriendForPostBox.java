package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.StringUtils;
import ru.j2dev.gameserver.dao.CharacterPostFriendDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;

import java.util.Map;

public class RequestExDeletePostFriendForPostBox extends L2GameClientPacket {
    private String _name;

    @Override
    protected void readImpl() {
        _name = readS();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        if (StringUtils.isEmpty(_name)) {
            return;
        }
        int key = 0;
        final Map<Integer, String> postFriends = player.getPostFriends();
        for (final Map.Entry<Integer, String> entry : postFriends.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(_name)) {
                key = entry.getKey();
            }
        }
        if (key == 0) {
            player.sendPacket(SystemMsg.THE_NAME_IS_NOT_CURRENTLY_REGISTERED);
            return;
        }
        player.getPostFriends().remove(key);
        CharacterPostFriendDAO.getInstance().delete(player, key);
        player.sendPacket((new SystemMessage2(SystemMsg.S1_WAS_SUCCESSFULLY_DELETED_FROM_YOUR_CONTACT_LIST)).addString(_name));
    }
}
