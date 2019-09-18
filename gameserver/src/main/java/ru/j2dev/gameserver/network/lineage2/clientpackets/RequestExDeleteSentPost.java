package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.dao.MailDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.mail.Mail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowSentPostList;

import java.util.Collection;
import java.util.stream.IntStream;

public class RequestExDeleteSentPost extends L2GameClientPacket {
    private int _count;
    private int[] _list;

    @Override
    protected void readImpl() {
        _count = readD();
        if (_count * 4 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _list = new int[_count];
        IntStream.range(0, _count).forEach(i -> _list[i] = readD());
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || _count == 0) {
            return;
        }
        final Collection<Mail> mails = MailDAO.getInstance().getSentMailByOwnerId(activeChar.getObjectId());
        if (!mails.isEmpty()) {
            mails.stream().filter(mail -> ArrayUtils.contains(_list, mail.getMessageId()) && mail.getAttachments().isEmpty()).forEach(mail -> MailDAO.getInstance().deleteSentMailByMailId(activeChar.getObjectId(), mail.getMessageId()));
        }
        activeChar.sendPacket(new ExShowSentPostList(activeChar));
    }
}
