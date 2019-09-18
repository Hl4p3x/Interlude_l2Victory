package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;

import java.util.ArrayList;
import java.util.List;

public class StatusUpdate extends L2GameServerPacket {
    public static final int CUR_HP = 9;
    public static final int MAX_HP = 10;
    public static final int CUR_MP = 11;
    public static final int MAX_MP = 12;
    public static final int CUR_LOAD = 14;
    public static final int MAX_LOAD = 15;
    public static final int PVP_FLAG = 26;
    public static final int KARMA = 27;
    public static final int CUR_CP = 33;
    public static final int MAX_CP = 34;
    private final int _objectId;
    private final List<Attribute> _attributes;

    public StatusUpdate(final Creature creature) {
        _attributes = new ArrayList<>();
        _objectId = creature.getObjectId();
    }

    public StatusUpdate(final int objectId) {
        _attributes = new ArrayList<>();
        _objectId = objectId;
    }

    public StatusUpdate addAttribute(final int id, final int level) {
        _attributes.add(new Attribute(id, level));
        return this;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xe);
        writeD(_objectId);
        writeD(_attributes.size());
        _attributes.forEach(temp -> {
            writeD(temp.id);
            writeD(temp.value);
        });
    }

    public boolean hasAttributes() {
        return !_attributes.isEmpty();
    }

    class Attribute {
        public final int id;
        public final int value;

        Attribute(final int id, final int value) {
            this.id = id;
            this.value = value;
        }
    }
}
