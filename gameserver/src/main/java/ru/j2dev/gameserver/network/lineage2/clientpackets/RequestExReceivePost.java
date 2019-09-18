package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.dao.MailDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;
import ru.j2dev.gameserver.model.mail.Mail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowReceivedPostList;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

import java.util.Set;

public class RequestExReceivePost extends L2GameClientPacket {
    private int postId;

    @Override
    protected void readImpl() {
        postId = readD();
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
        if (activeChar.isInStoreMode()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS);
            return;
        }
        if (activeChar.isInTrade()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_DURING_AN_EXCHANGE);
            return;
        }
        if (activeChar.isFishing()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
            return;
        }
        if (activeChar.getEnchantScroll() != null) {
            activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT);
            return;
        }
        final Mail mail = MailDAO.getInstance().getReceivedMailByMailId(activeChar.getObjectId(), postId);
        if (mail != null) {
            activeChar.getInventory().writeLock();
            try {
                final Set<ItemInstance> attachments = mail.getAttachments();
                if (attachments.size() > 0 && !activeChar.isInPeaceZone()) {
                    activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_IN_A_NON_PEACE_ZONE_LOCATION);
                    return;
                }
                final ItemInstance[] items;
                synchronized (attachments) {
                    if (mail.getAttachments().isEmpty()) {
                        return;
                    }
                    items = mail.getAttachments().toArray(new ItemInstance[attachments.size()]);
                    int slots = 0;
                    long weight = 0L;
                    for (final ItemInstance item : items) {
                        weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getCount(), item.getTemplate().getWeight()));
                        if (!item.getTemplate().isStackable() || activeChar.getInventory().getItemByItemId(item.getItemId()) == null) {
                            ++slots;
                        }
                    }
                    if (!activeChar.getInventory().validateWeight(weight)) {
                        sendPacket(Msg.YOU_COULD_NOT_RECEIVE_BECAUSE_YOUR_INVENTORY_IS_FULL);
                        return;
                    }
                    if (!activeChar.getInventory().validateCapacity(slots)) {
                        sendPacket(Msg.YOU_COULD_NOT_RECEIVE_BECAUSE_YOUR_INVENTORY_IS_FULL);
                        return;
                    }
                    if (mail.getPrice() > 0L) {
                        if (!activeChar.reduceAdena(mail.getPrice(), true)) {
                            activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_BECAUSE_YOU_DON_T_HAVE_ENOUGH_ADENA);
                            return;
                        }
                        final Player sender = World.getPlayer(mail.getSenderId());
                        if (sender != null) {
                            sender.addAdena(mail.getPrice(), true);
                            sender.sendPacket(new SystemMessage(3072).addName(activeChar));
                        } else {
                            final int expireTime = 1296000 + (int) (System.currentTimeMillis() / 1000L);
                            final Mail reply = mail.reply();
                            reply.setExpireTime(expireTime);
                            final ItemInstance item = ItemFunctions.createItem(57);
                            item.setOwnerId(reply.getReceiverId());
                            item.setCount(mail.getPrice());
                            item.setLocation(ItemLocation.MAIL);
                            item.save();
                            Log.LogItem(activeChar, ItemLog.PostSend, item);
                            reply.addAttachment(item);
                            reply.save();
                        }
                    }
                    attachments.clear();
                }
                mail.setJdbcState(JdbcEntityState.UPDATED);
                mail.update();
                for (final ItemInstance item2 : items) {
                    activeChar.sendPacket(new SystemMessage(3073).addItemName(item2.getItemId()).addNumber(item2.getCount()));
                    Log.LogItem(activeChar, ItemLog.PostRecieve, item2);
                    activeChar.getInventory().addItem(item2);
                }
                activeChar.sendPacket(Msg.MAIL_SUCCESSFULLY_RECEIVED);
            } catch (ArithmeticException ignored) {
            } finally {
                activeChar.getInventory().writeUnlock();
            }
        }
        activeChar.sendPacket(new ExShowReceivedPostList(activeChar));
    }
}
