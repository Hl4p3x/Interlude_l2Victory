package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.dao.CharacterDAO;
import ru.j2dev.gameserver.dao.CharacterPostFriendDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExConfirmAddingPostFriend;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SysMsgContainer;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;

import java.util.Map;

public class RequestExAddPostFriendForPostBox extends L2GameClientPacket {
    private String _name;

    @Override
    protected void readImpl() {
        _name = readS(Config.CNAME_MAXLEN);
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final int targetObjectId = CharacterDAO.getInstance().getObjectIdByName(_name);
        if (targetObjectId == 0) {
            player.sendPacket(new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.NAME_IS_NOT_EXISTS));
            return;
        }
        if (_name.equalsIgnoreCase(player.getName())) {
            player.sendPacket(new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.NAME_IS_NOT_REGISTERED));
            return;
        }
        final Map<Integer, String> postFriend = player.getPostFriends();
        if (postFriend.size() >= 100) {
            player.sendPacket(new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.LIST_IS_FULL));
            return;
        }
        if (postFriend.containsKey(targetObjectId)) {
            player.sendPacket(new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.ALREADY_ADDED));
            return;
        }
        CharacterPostFriendDAO.getInstance().insert(player, targetObjectId);
        postFriend.put(targetObjectId, CharacterDAO.getInstance().getNameByObjectId(targetObjectId));
        player.sendPacket(((SysMsgContainer) new SystemMessage2(SystemMsg.S1_WAS_SUCCESSFULLY_ADDED_TO_YOUR_CONTACT_LIST)).addString(_name), new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.SUCCESS));
    }
}
