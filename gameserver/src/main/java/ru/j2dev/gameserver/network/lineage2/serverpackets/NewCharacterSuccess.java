package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.templates.PlayerTemplate;

import java.util.ArrayList;
import java.util.List;

public class NewCharacterSuccess extends L2GameServerPacket {
    private final List<PlayerTemplate> _chars;

    public NewCharacterSuccess() {
        _chars = new ArrayList<>();
    }

    public void addChar(final PlayerTemplate template) {
        _chars.add(template);
    }

    @Override
    protected final void writeImpl() {
        writeC(0x17);
        writeD(_chars.size());
        _chars.forEach(temp -> {
            writeD(temp.race.ordinal());
            writeD(temp.classId.getId());
            writeD(70);
            writeD(temp.getBaseSTR());
            writeD(10);
            writeD(70);
            writeD(temp.getBaseDEX());
            writeD(10);
            writeD(70);
            writeD(temp.getBaseCON());
            writeD(10);
            writeD(70);
            writeD(temp.getBaseINT());
            writeD(10);
            writeD(70);
            writeD(temp.getBaseWIT());
            writeD(10);
            writeD(70);
            writeD(temp.getBaseMEN());
            writeD(10);
        });
    }
}
