package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.gameserver.dao.MailDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.mail.Mail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExChangePostState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExReplyReceivedPost;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowReceivedPostList;

public class RequestExRequestReceivedPost extends L2GameClientPacket {
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
        final Mail mail = MailDAO.getInstance().getReceivedMailByMailId(activeChar.getObjectId(), postId);
        if (mail != null) {
            if (mail.isUnread()) {
                mail.setUnread(false);
                mail.setJdbcState(JdbcEntityState.UPDATED);
                mail.update();
                activeChar.sendPacket(new ExChangePostState(true, 1, mail));
            }
            activeChar.sendPacket(new ExReplyReceivedPost(mail));
            return;
        }
        activeChar.sendPacket(new ExShowReceivedPostList(activeChar));
    }
}
