package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.dao.MailDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.mail.Mail;
import ru.j2dev.gameserver.model.mail.Mail.SenderType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExNoticePostArrived;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowReceivedPostList;

public class RequestExRejectPost extends L2GameClientPacket {
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
            activeChar.sendPacket(Msg.YOU_CANNOT_CANCEL_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS);
            return;
        }
        if (activeChar.isInTrade()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_CANCEL_DURING_AN_EXCHANGE);
            return;
        }
        if (activeChar.getEnchantScroll() != null) {
            activeChar.sendPacket(Msg.YOU_CANNOT_CANCEL_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT);
            return;
        }
        if (!activeChar.isInPeaceZone()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_CANCEL_IN_A_NON_PEACE_ZONE_LOCATION);
            return;
        }
        if (activeChar.isFishing()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
            return;
        }
        final Mail mail = MailDAO.getInstance().getReceivedMailByMailId(activeChar.getObjectId(), postId);
        if (mail != null) {
            if (mail.getType() != SenderType.NORMAL || mail.getAttachments().isEmpty()) {
                activeChar.sendActionFailed();
                return;
            }
            final int expireTime = 1296000 + (int) (System.currentTimeMillis() / 1000L);
            final Mail reject = mail.reject();
            MailDAO.getInstance().deleteReceivedMailByMailId(mail.getReceiverId(), mail.getMessageId());
            MailDAO.getInstance().deleteSentMailByMailId(mail.getReceiverId(), mail.getMessageId());
            reject.setExpireTime(expireTime);
            reject.save();
            final Player sender = World.getPlayer(reject.getReceiverId());
            if (sender != null) {
                sender.sendPacket(ExNoticePostArrived.STATIC_TRUE);
            }
        }
        activeChar.sendPacket(new ExShowReceivedPostList(activeChar));
    }
}
