package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.NpcString;

public class ExSendUIEvent extends NpcStringContainer {
    private final int _objectId;
    private final boolean _isHide;
    private final boolean _isIncrease;
    private final int _startTime;
    private final int _endTime;

    public ExSendUIEvent(final Player player, final boolean isHide, final boolean isIncrease, final int startTime, final int endTime, final String... params) {
        this(player, isHide, isIncrease, startTime, endTime, NpcString.NONE, params);
    }

    public ExSendUIEvent(final Player player, final boolean isHide, final boolean isIncrease, final int startTime, final int endTime, final NpcString npcString, final String... params) {
        super(npcString, params);
        _objectId = player.getObjectId();
        _isHide = isHide;
        _isIncrease = isIncrease;
        _startTime = startTime;
        _endTime = endTime;
    }

    @Override
    protected void writeImpl() {
        writeC(0xfe);
        writeH(142);
        writeD(_objectId);
        writeD(_isHide ? 1 : 0);
        writeD(0);
        writeD(0);
        writeS(_isIncrease ? "1" : "0");
        writeS(String.valueOf(_startTime / 60));
        writeS(String.valueOf(_startTime % 60));
        writeS(String.valueOf(_endTime / 60));
        writeS(String.valueOf(_endTime % 60));
        writeElements();
    }
}
