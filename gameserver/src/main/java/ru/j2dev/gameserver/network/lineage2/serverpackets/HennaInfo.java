package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

import java.util.stream.IntStream;

public class HennaInfo extends L2GameServerPacket {
    private final Henna[] _hennas;
    private final int _str;
    private final int _con;
    private final int _dex;
    private final int _int;
    private final int _wit;
    private final int _men;
    private final int slots;
    private int _count;

    public HennaInfo(final Player player) {
        _hennas = new Henna[3];
        _count = 0;
        for (int i = 0; i < 3; ++i) {
            final ru.j2dev.gameserver.templates.Henna h;
            if ((h = player.getHenna(i + 1)) != null) {
                _hennas[_count++] = new Henna(h.getSymbolId(), h.isForThisClass(player));
            }
        }
        _str = player.getHennaStatSTR();
        _con = player.getHennaStatCON();
        _dex = player.getHennaStatDEX();
        _int = player.getHennaStatINT();
        _wit = player.getHennaStatWIT();
        _men = player.getHennaStatMEN();
        slots = ((player.getLevel() < 40) ? 2 : 3);
    }

    @Override
    protected final void writeImpl() {
        writeC(0xe4);
        writeC(_int);
        writeC(_str);
        writeC(_con);
        writeC(_men);
        writeC(_dex);
        writeC(_wit);
        writeD(slots);
        writeD(_count);
        IntStream.range(0, _count).forEach(i -> {
            writeD(_hennas[i]._symbolId);
            writeD(_hennas[i]._valid ? _hennas[i]._symbolId : 0);
        });
    }

    private static class Henna {
        private final int _symbolId;
        private final boolean _valid;

        public Henna(final int sy, final boolean valid) {
            _symbolId = sy;
            _valid = valid;
        }
    }
}
