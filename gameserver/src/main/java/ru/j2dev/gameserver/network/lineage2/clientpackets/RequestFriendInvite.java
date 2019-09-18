package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.StringUtils;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.FriendAddRequest;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;

public class RequestFriendInvite extends L2GameClientPacket {
    private String _name;

    @Override
    protected void readImpl() {
        _name = readS(16);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || StringUtils.isEmpty(_name)) {
            return;
        }
        if (activeChar.isOutOfControl()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isProcessingRequest()) {
            activeChar.sendPacket(SystemMsg.WAITING_FOR_ANOTHER_REPLY);
            return;
        }
        final Player target = World.getPlayer(_name);
        if (target == null) {
            activeChar.sendPacket(SystemMsg.THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME);
            return;
        }
        if (target == activeChar) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST);
            return;
        }
        if (target.isBlockAll() || target.isInBlockList(activeChar) || target.getMessageRefusal()) {
            activeChar.sendPacket(SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
            return;
        }
        if (activeChar.getFriendList().getList().containsKey(target.getObjectId())) {
            activeChar.sendPacket((new SystemMessage2(SystemMsg.C1_IS_ALREADY_ON_YOUR_FRIEND_LIST)).addName(target));
            return;
        }
        if (activeChar.getFriendList().getList().size() >= 128) {
            activeChar.sendPacket(SystemMsg.YOU_CAN_ONLY_ENTER_UP_128_NAMES_IN_YOUR_FRIENDS_LIST);
            return;
        }
        if (target.getFriendList().getList().size() >= 128) {
            activeChar.sendPacket(SystemMsg.THE_FRIENDS_LIST_OF_THE_PERSON_YOU_ARE_TRYING_TO_ADD_IS_FULL_SO_REGISTRATION_IS_NOT_POSSIBLE);
            return;
        }
        if (target.isOlyParticipant()) {
            activeChar.sendPacket(SystemMsg.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS);
            return;
        }
        new Request(L2RequestType.FRIEND, activeChar, target).setTimeout(10000L);
        activeChar.sendPacket((new SystemMessage2(SystemMsg.C1_HAS_SENT_A_FRIEND_REQUEST)).addName(target));
        target.sendPacket((new SystemMessage2(SystemMsg.C1_HAS_SENT_A_FRIEND_REQUEST)).addName(activeChar), new FriendAddRequest(activeChar.getName()));
    }
}
