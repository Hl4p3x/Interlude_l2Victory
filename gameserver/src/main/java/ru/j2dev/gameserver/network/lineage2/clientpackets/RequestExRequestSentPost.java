package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.dao.MailDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.mail.Mail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExReplySentPost;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowSentPostList;

public class RequestExRequestSentPost extends L2GameClientPacket {
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
        final Mail mail = MailDAO.getInstance().getSentMailByMailId(activeChar.getObjectId(), postId);
        if (mail != null) {
            activeChar.sendPacket(new ExReplySentPost(mail));
            return;
        }
        activeChar.sendPacket(new ExShowSentPostList(activeChar));
    }
}
