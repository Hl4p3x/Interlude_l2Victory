package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.mail.Mail;

public class ExReplySentPost extends L2GameServerPacket {
    private final Mail mail;

    public ExReplySentPost(final Mail mail) {
        this.mail = mail;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xad);
        writeD(mail.getMessageId());
        writeD(mail.isPayOnDelivery() ? 1 : 0);
        writeS(mail.getReceiverName());
        writeS(mail.getTopic());
        writeS(mail.getBody());
        writeD(mail.getAttachments().size());
        mail.getAttachments().forEach(item -> {
            writeItemInfo(item);
            writeD(item.getObjectId());
        });
        writeQ(mail.getPrice());
        writeD(0);
    }
}
