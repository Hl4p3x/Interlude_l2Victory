package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.base.AcquireType;

import java.util.ArrayList;
import java.util.List;

public class AcquireSkillList extends L2GameServerPacket {
    private final List<Skill> _skills;
    private final AcquireType _type;

    public AcquireSkillList(final AcquireType type, final int size) {
        _skills = new ArrayList<>(size);
        _type = type;
    }

    public void addSkill(final int id, final int nextLevel, final int maxLevel, final int Cost, final int requirements, final int subUnit) {
        _skills.add(new Skill(id, nextLevel, maxLevel, Cost, requirements, subUnit));
    }

    public void addSkill(final int id, final int nextLevel, final int maxLevel, final int Cost, final int requirements) {
        _skills.add(new Skill(id, nextLevel, maxLevel, Cost, requirements, 0));
    }

    @Override
    protected final void writeImpl() {
        writeC(0x8a);
        writeD(_type.ordinal());
        writeD(_skills.size());
        _skills.forEach(temp -> {
            writeD(temp.id);
            writeD(temp.nextLevel);
            writeD(temp.maxLevel);
            writeD(temp.cost);
            writeD(temp.requirements);
        });
    }

    class Skill {
        public final int id;
        public final int nextLevel;
        public final int maxLevel;
        public final int cost;
        public final int requirements;
        public final int subUnit;

        Skill(final int id, final int nextLevel, final int maxLevel, final int cost, final int requirements, final int subUnit) {
            this.id = id;
            this.nextLevel = nextLevel;
            this.maxLevel = maxLevel;
            this.cost = cost;
            this.requirements = requirements;
            this.subUnit = subUnit;
        }
    }
}
