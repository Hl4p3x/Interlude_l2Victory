package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.templates.Henna;

public class HennaItemInfo extends L2GameServerPacket {
    private final int _str;
    private final int _con;
    private final int _dex;
    private final int _int;
    private final int _wit;
    private final int _men;
    private final long _adena;
    private final Henna _henna;

    public HennaItemInfo(final Henna henna, final Player player) {
        _henna = henna;
        _adena = player.getAdena();
        _str = player.getSTR();
        _dex = player.getDEX();
        _con = player.getCON();
        _int = player.getINT();
        _wit = player.getWIT();
        _men = player.getMEN();
    }

    @Override
    protected final void writeImpl() {
        writeC(0xe3);
        writeD(_henna.getSymbolId());
        writeD(_henna.getDyeId());
        writeD((int) _henna.getDrawCount());
        writeD((int) _henna.getPrice());
        writeD(1);
        writeD((int) _adena);
        writeD(_int);
        writeC(_int + _henna.getStatINT());
        writeD(_str);
        writeC(_str + _henna.getStatSTR());
        writeD(_con);
        writeC(_con + _henna.getStatCON());
        writeD(_men);
        writeC(_men + _henna.getStatMEN());
        writeD(_dex);
        writeC(_dex + _henna.getStatDEX());
        writeD(_wit);
        writeC(_wit + _henna.getStatWIT());
    }
}
