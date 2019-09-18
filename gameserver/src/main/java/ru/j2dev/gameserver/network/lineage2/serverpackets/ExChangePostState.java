package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.mail.Mail;

import java.util.Arrays;

public class ExChangePostState extends L2GameServerPacket {
    private final boolean _receivedBoard;
    private final Mail[] _mails;
    private final int _changeId;

    public ExChangePostState(final boolean receivedBoard, final int type, final Mail... n) {
        _receivedBoard = receivedBoard;
        _mails = n;
        _changeId = type;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xb3);
        writeD(_receivedBoard ? 1 : 0);
        writeD(_mails.length);
        Arrays.stream(_mails).forEach(mail -> {
            writeD(mail.getMessageId());
            writeD(_changeId);
        });
    }
}
