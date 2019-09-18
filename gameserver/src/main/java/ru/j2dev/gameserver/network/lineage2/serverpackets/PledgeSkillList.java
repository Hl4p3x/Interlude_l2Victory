package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.pledge.Clan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PledgeSkillList extends L2GameServerPacket {
    private final List<UnitSkillInfo> _unitSkills;
    private List<SkillInfo> _allSkills;

    public PledgeSkillList(final Clan clan) {
        _allSkills = Collections.emptyList();
        _unitSkills = new ArrayList<>();
        final Collection<Skill> skills = clan.getSkills();
        _allSkills = new ArrayList<>(skills.size());
        skills.forEach(sk -> _allSkills.add(new SkillInfo(sk.getId(), sk.getLevel())));
        clan.getAllSubUnits().forEach(subUnit -> subUnit.getSkills().stream().map(sk2 -> new UnitSkillInfo(subUnit.getType(), sk2.getId(), sk2.getLevel())).forEach(_unitSkills::add));
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x39);
        writeD(_allSkills.size() + _unitSkills.size());
        _allSkills.forEach(info -> {
            writeD(info._id);
            writeD(info._level);
        });
        _unitSkills.forEach(info2 -> {
            writeD(info2._id);
            writeD(info2._level);
        });
    }

    static class SkillInfo {
        public final int _id;
        public final int _level;

        public SkillInfo(final int id, final int level) {
            _id = id;
            _level = level;
        }
    }

    static class UnitSkillInfo extends SkillInfo {
        private final int _type;

        public UnitSkillInfo(final int type, final int id, final int level) {
            super(id, level);
            _type = type;
        }
    }
}
