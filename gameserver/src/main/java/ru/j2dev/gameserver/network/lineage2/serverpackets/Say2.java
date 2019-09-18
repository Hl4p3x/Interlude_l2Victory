package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.SysString;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;

public class Say2 extends L2GameServerPacket {

    private final ChatType _type;
    private final int _objectId;
    private SysString _sysString;
    private SystemMsg _systemMsg;
    private String _charName;
    private String _text;

    public Say2(final int objectId, final ChatType type, final SysString st, final SystemMsg sm) {
        _objectId = objectId;
        _type = type;
        _sysString = st;
        _systemMsg = sm;
    }

    public Say2(final int objectId, final ChatType type, final String charName, final String text) {
        _objectId = objectId;
        _type = type;
        _charName = charName;
        _text = text;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x4a);
        writeD(_objectId);
        writeD(_type.ordinal());
        switch (_type) {
            case SYSTEM_MESSAGE:
                writeD(_sysString.getId());
                writeD(_systemMsg.getId());
                break;
            default:
                writeS(_charName);
                writeS(_text);
                break;
        }
    }
}
