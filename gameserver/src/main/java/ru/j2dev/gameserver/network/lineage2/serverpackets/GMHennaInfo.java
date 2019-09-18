package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.templates.Henna;

import java.util.stream.IntStream;

public class GMHennaInfo extends L2GameServerPacket {
    private final Henna[] _hennas;
    private final int _count;
    private final int _str;
    private final int _con;
    private final int _dex;
    private final int _int;
    private final int _wit;
    private final int _men;

    public GMHennaInfo(final Player cha) {
        _hennas = new Henna[3];
        _str = cha.getHennaStatSTR();
        _con = cha.getHennaStatCON();
        _dex = cha.getHennaStatDEX();
        _int = cha.getHennaStatINT();
        _wit = cha.getHennaStatWIT();
        _men = cha.getHennaStatMEN();
        int j = 0;
        for (int i = 0; i < 3; ++i) {
            final Henna h = cha.getHenna(i + 1);
            if (h != null) {
                _hennas[j++] = h;
            }
        }
        _count = j;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xea);
        writeC(_int);
        writeC(_str);
        writeC(_con);
        writeC(_men);
        writeC(_dex);
        writeC(_wit);
        writeD(3);
        writeD(_count);
        IntStream.range(0, _count).forEach(i -> {
            writeD(_hennas[i].getSymbolId());
            writeD(_hennas[i].getSymbolId());
        });
    }
}
