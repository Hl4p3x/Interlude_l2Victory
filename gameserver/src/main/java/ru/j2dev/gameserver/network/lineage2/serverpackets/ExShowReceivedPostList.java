package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.dao.MailDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.mail.Mail;
import ru.j2dev.gameserver.model.mail.Mail.SenderType;

import java.util.List;

public class ExShowReceivedPostList extends L2GameServerPacket {
    private final List<Mail> mails;

    public ExShowReceivedPostList(final Player cha) {
        mails = MailDAO.getInstance().getReceivedMailByOwnerId(cha.getObjectId());
    }

    @Override
    protected void writeImpl() {
        writeEx(0xaa);
        writeD((int) (System.currentTimeMillis() / 1000L));
        writeD(mails.size());
        mails.stream().sorted().forEach(mail -> {
            writeD(mail.getMessageId());
            writeS(mail.getTopic());
            writeS(mail.getSenderName());
            writeD(mail.isPayOnDelivery() ? 1 : 0);
            writeD(mail.getExpireTime());
            writeD(mail.isUnread() ? 1 : 0);
            writeD((mail.getType() != SenderType.NORMAL) ? 1 : 0);
            writeD(mail.getAttachments().isEmpty() ? 0 : 1);
            writeD(0);
            writeD(mail.getType().ordinal());
            writeD(0);
        });
    }
}
