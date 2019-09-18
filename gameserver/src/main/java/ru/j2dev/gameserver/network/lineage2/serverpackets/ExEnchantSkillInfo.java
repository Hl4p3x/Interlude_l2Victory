package ru.j2dev.gameserver.network.lineage2.serverpackets;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;

public class ExEnchantSkillInfo extends L2GameServerPacket {
    private final int _skillId;
    private final int _skillLevel;
    private final int _sp;
    private final long _exp;
    private final int _chance;
    private final List<Pair<Integer, Long>> _itemsNeeded;

    public ExEnchantSkillInfo(final int skillId, final int skillLvl, final int sp, final long exp, final int chance) {
        _skillId = skillId;
        _skillLevel = skillLvl;
        _sp = sp;
        _exp = exp;
        _chance = chance;
        _itemsNeeded = new LinkedList<>();
    }

    public void addNeededItem(final int itemId, final long itemCount) {
        _itemsNeeded.add(ImmutablePair.of(itemId, itemCount));
    }

    @Override
    protected void writeImpl() {
        writeEx(0x18);
        writeD(_skillId);
        writeD(_skillLevel);
        writeD(_sp);
        writeQ(_exp);
        writeD(_chance);
        if (_itemsNeeded.isEmpty()) {
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
        } else {
            writeD(_itemsNeeded.size());
            _itemsNeeded.forEach(itemNeeded -> {
                writeD(4);
                writeD(itemNeeded.getKey());
                writeD(Math.toIntExact(itemNeeded.getValue()));
                writeD(0);
            });
        }
    }
}
