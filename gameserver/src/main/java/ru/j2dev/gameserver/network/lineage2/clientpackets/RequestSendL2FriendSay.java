package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.chat.ChatFilters;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatFilter;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2FriendSay;
import ru.j2dev.gameserver.utils.Log;

public class RequestSendL2FriendSay extends L2GameClientPacket {
    private String _message;
    private String _reciever;

    @Override
    protected void readImpl() {
        _message = readS(2048);
        _reciever = readS(16);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Player targetPlayer = World.getPlayer(_reciever);
        if (targetPlayer == null) {
            activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
            return;
        }
        if (targetPlayer.isBlockAll()) {
            activeChar.sendPacket(SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
            return;
        }
        Label_0196:
        {
            if (!activeChar.getPlayerAccess().CanAnnounce) {
                for (final ChatFilter f : ChatFilters.getInstance().getFilters()) {
                    if (f.isMatch(activeChar, ChatType.L2FRIEND, _message, targetPlayer)) {
                        switch (f.getAction()) {
                            case 1: {
                                activeChar.updateNoChannel(Integer.parseInt(f.getValue()) * 1000L);
                                break Label_0196;
                            }
                            case 2: {
                                activeChar.sendMessage(new CustomMessage(f.getValue(), activeChar));
                                return;
                            }
                            case 3: {
                                _message = f.getValue();
                                break Label_0196;
                            }
                        }
                    }
                }
            }
        }
        if (activeChar.getNoChannel() > 0L && ArrayUtils.contains(Config.BAN_CHANNEL_LIST, ChatType.L2FRIEND)) {
            if (activeChar.getNoChannelRemained() > 0L) {
                final long timeRemained = activeChar.getNoChannelRemained() / 60000L + 1L;
                activeChar.sendMessage(new CustomMessage("common.ChatBanned", activeChar).addNumber(timeRemained));
                return;
            }
            activeChar.updateNoChannel(0L);
        }
        if (!activeChar.getFriendList().getList().containsKey(targetPlayer.getObjectId())) {
            return;
        }
        if (activeChar.canTalkWith(targetPlayer)) {
            final L2FriendSay frm = new L2FriendSay(activeChar.getName(), _reciever, _message);
            targetPlayer.sendPacket(frm);
            Log.LogChat("FRIENDTELL", activeChar.getName(), _reciever, _message, 0);
        }
    }
}
