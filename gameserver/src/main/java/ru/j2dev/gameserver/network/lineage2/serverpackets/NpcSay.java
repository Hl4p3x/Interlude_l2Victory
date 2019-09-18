package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.NpcString;

public class NpcSay extends NpcStringContainer {
    private final int _objId;
    private final int _type;
    private final int _id;

    public NpcSay(final NpcInstance npc, final ChatType chatType, final String text) {
        this(npc, chatType, NpcString.NONE, text);
    }

    public NpcSay(final NpcInstance npc, final ChatType chatType, final NpcString npcString, final String... params) {
        super(npcString, params);
        _objId = npc.getObjectId();
        _id = npc.getNpcId();
        _type = chatType.ordinal();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x2);
        writeD(_objId);
        writeD(_type);
        writeD(1000000 + _id);
        writeElements();
    }
}
