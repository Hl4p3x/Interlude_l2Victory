package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.dao.CharacterDAO;
import ru.j2dev.gameserver.database.mysql;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;
import ru.j2dev.gameserver.model.mail.Mail;
import ru.j2dev.gameserver.model.mail.Mail.SenderType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExNoticePostArrived;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExReplyWritePost;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;
import ru.j2dev.gameserver.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestExSendPost extends L2GameClientPacket {
    private int _messageType;
    private String _recieverName;
    private String _topic;
    private String _body;
    private int _count;
    private int[] _items;
    private long[] _itemQ;
    private long _price;

    @Override
    protected void readImpl() {
        _recieverName = readS(35);
        _messageType = readD();
        _topic = readS(127);
        _body = readS(32767);
        _count = readD();
        if (_count * 8 + 4 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new int[_count];
        _itemQ = new long[_count];
        for (int i = 0; i < _count; ++i) {
            _items[i] = readD();
            _itemQ[i] = readD();
            if (_itemQ[i] < 1L || ArrayUtils.indexOf(_items, _items[i]) < i) {
                _count = 0;
                return;
            }
        }
        _price = readQ();
        if (_price < 0L) {
            _count = 0;
            _price = 0L;
        }
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isGM() && "ONLINE_ALL".equalsIgnoreCase(_recieverName)) {
            final Map<Integer, Long> map = new HashMap<>();
            if (_items != null && _items.length > 0) {
                for (int i = 0; i < _items.length; ++i) {
                    final ItemInstance item = activeChar.getInventory().getItemByObjectId(_items[i]);
                    map.put(item.getItemId(), _itemQ[i]);
                }
            }
            GameObjectsStorage.getPlayers().stream().filter(p -> p != null && p.isOnline()).forEach(p -> Functions.sendSystemMail(p, _topic, _body, map));
            activeChar.sendPacket(ExReplyWritePost.STATIC_TRUE);
            activeChar.sendPacket(Msg.MAIL_SUCCESSFULLY_SENT);
            return;
        }
        if (!Config.ALLOW_MAIL) {
            activeChar.sendMessage(new CustomMessage("mail.Disabled", activeChar));
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isInStoreMode()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS);
            return;
        }
        if (activeChar.isInTrade()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_DURING_AN_EXCHANGE);
            return;
        }
        if (activeChar.getEnchantScroll() != null) {
            activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT);
            return;
        }
        if (activeChar.getName().equalsIgnoreCase(_recieverName)) {
            activeChar.sendPacket(Msg.YOU_CANNOT_SEND_A_MAIL_TO_YOURSELF);
            return;
        }
        if (_count > 0 && !activeChar.isInPeaceZone()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_IN_A_NON_PEACE_ZONE_LOCATION);
            return;
        }
        if (activeChar.isFishing()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
            return;
        }
        if (!activeChar.getAntiFlood().canMail()) {
            activeChar.sendPacket(Msg.THE_PREVIOUS_MAIL_WAS_FORWARDED_LESS_THAN_1_MINUTE_AGO_AND_THIS_CANNOT_BE_FORWARDED);
            return;
        }
        if (_price > 0L) {
            if (!activeChar.getPlayerAccess().UseTrade) {
                activeChar.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
                activeChar.sendActionFailed();
                return;
            }
            final String tradeBan = activeChar.getVar("tradeBan");
            if (tradeBan != null && ("-1".equals(tradeBan) || Long.parseLong(tradeBan) >= System.currentTimeMillis())) {
                if ("-1".equals(tradeBan)) {
                    activeChar.sendMessage(new CustomMessage("common.TradeBannedPermanently", activeChar));
                } else {
                    activeChar.sendMessage(new CustomMessage("common.TradeBanned", activeChar).addString(Util.formatTime((int) (Long.parseLong(tradeBan) / 1000L - System.currentTimeMillis() / 1000L))));
                }
                return;
            }
        }
        if (activeChar.isInBlockList(_recieverName)) {
            activeChar.sendPacket(new SystemMessage(2057).addString(_recieverName));
            return;
        }
        final Player target = World.getPlayer(_recieverName);
        int recieverId;
        if (target != null) {
            recieverId = target.getObjectId();
            _recieverName = target.getName();
            if (target.isInBlockList(activeChar)) {
                activeChar.sendPacket(new SystemMessage(1228).addString(_recieverName));
                return;
            }
        } else {
            recieverId = CharacterDAO.getInstance().getObjectIdByName(_recieverName);
            if (recieverId > 0 && mysql.simple_get_int("target_Id", "character_blocklist", "obj_Id=" + recieverId + " AND target_Id=" + activeChar.getObjectId()) > 0) {
                activeChar.sendPacket(new SystemMessage(1228).addString(_recieverName));
                return;
            }
        }
        if (recieverId == 0) {
            activeChar.sendPacket(Msg.WHEN_THE_RECIPIENT_DOESN_T_EXIST_OR_THE_CHARACTER_HAS_BEEN_DELETED_SENDING_MAIL_IS_NOT_POSSIBLE);
            return;
        }
        final int expireTime = ((_messageType == 1) ? 12 : 360) * 3600 + (int) (System.currentTimeMillis() / 1000L);
        if (_count > 8) {
            activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            return;
        }
        final long serviceCost = 100 + _count * 1000;
        final List<ItemInstance> attachments = new ArrayList<>();
        activeChar.getInventory().writeLock();
        try {
            if (activeChar.getAdena() < serviceCost) {
                activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_BECAUSE_YOU_DON_T_HAVE_ENOUGH_ADENA);
                return;
            }
            if (_count > 0) {
                for (int j = 0; j < _count; ++j) {
                    final ItemInstance item2 = activeChar.getInventory().getItemByObjectId(_items[j]);
                    if (item2 == null || item2.getCount() < _itemQ[j] || (item2.getItemId() == 57 && item2.getCount() < _itemQ[j] + serviceCost) || !item2.canBeTraded(activeChar)) {
                        activeChar.sendPacket(Msg.THE_ITEM_THAT_YOU_RE_TRYING_TO_SEND_CANNOT_BE_FORWARDED_BECAUSE_IT_ISN_T_PROPER);
                        return;
                    }
                }
            }
            if (!activeChar.reduceAdena(serviceCost, true)) {
                activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_BECAUSE_YOU_DON_T_HAVE_ENOUGH_ADENA);
                return;
            }
            if (_count > 0) {
                for (int j = 0; j < _count; ++j) {
                    final ItemInstance item2 = activeChar.getInventory().removeItemByObjectId(_items[j], _itemQ[j]);
                    Log.LogItem(activeChar, ItemLog.PostSend, item2);
                    item2.setOwnerId(activeChar.getObjectId());
                    item2.setLocation(ItemLocation.MAIL);
                    item2.save();
                    attachments.add(item2);
                }
            }
        } finally {
            activeChar.getInventory().writeUnlock();
        }
        final Mail mail = new Mail();
        mail.setSenderId(activeChar.getObjectId());
        mail.setSenderName(activeChar.getName());
        mail.setReceiverId(recieverId);
        mail.setReceiverName(_recieverName);
        mail.setTopic(_topic);
        mail.setBody(_body);
        mail.setPrice((_messageType > 0) ? _price : 0L);
        mail.setUnread(true);
        mail.setType(SenderType.NORMAL);
        mail.setExpireTime(expireTime);
        for (final ItemInstance item3 : attachments) {
            mail.addAttachment(item3);
        }
        mail.save();
        activeChar.sendPacket(ExReplyWritePost.STATIC_TRUE);
        activeChar.sendPacket(Msg.MAIL_SUCCESSFULLY_SENT);
        if (target != null) {
            target.sendPacket(ExNoticePostArrived.STATIC_TRUE);
            target.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
        }
    }
}
