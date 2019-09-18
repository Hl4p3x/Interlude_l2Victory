package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.SkillLearn;
import ru.j2dev.gameserver.model.base.AcquireType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AcquireSkillInfo extends L2GameServerPacket {
    private final SkillLearn _learn;
    private final AcquireType _type;
    private List<Require> _reqs;

    public AcquireSkillInfo(final AcquireType type, final SkillLearn learn) {
        this(type, learn, learn.getItemId(), (int) learn.getItemCount());
    }

    public AcquireSkillInfo(final AcquireType type, final SkillLearn learn, final int itemId, final int itemCount) {
        _reqs = Collections.emptyList();
        _type = type;
        _learn = learn;
        if (itemId != 0) {
            (_reqs = new ArrayList<>(1)).add(new Require(99, itemId, itemCount, 50));
        }
    }

    @Override
    public void writeImpl() {
        writeC(0x8b);
        writeD(_learn.getId());
        writeD(_learn.getLevel());
        writeD(_learn.getCost());
        writeD(_type.ordinal());
        writeD(_reqs.size());
        for (final Require temp : _reqs) {
            writeD(temp.type);
            writeD(temp.itemId);
            writeD((int) temp.count);
            writeD(temp.unk);
        }
    }

    private static class Require {
        public final int itemId;
        public final long count;
        public final int type;
        public final int unk;

        public Require(final int pType, final int pItemId, final long pCount, final int pUnk) {
            itemId = pItemId;
            type = pType;
            count = pCount;
            unk = pUnk;
        }
    }
}
