package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SkillList extends L2GameServerPacket {
    private final List<SkillListRecord> _skillRecords;

    public SkillList(final Player player) {
        final Collection<Skill> playerSkills = player.getAllSkills();
        _skillRecords = new ArrayList<>(playerSkills.size());
        playerSkills.stream().map(skill -> new SkillListRecord(player, skill)).forEach(_skillRecords::add);
    }

    @Override
    protected final void writeImpl() {
        writeC(0x58);
        writeD(_skillRecords.size());
        _skillRecords.forEach(SkillListRecord::writeRecord);
    }

    private class SkillListRecord implements Comparable<SkillListRecord> {
        private final int _id;
        private final int _lvl;
        private final boolean _disabled;
        private final int _order;

        public SkillListRecord(final Player player, final Skill skill) {
            _id = skill.getDisplayId();
            _lvl = skill.getDisplayLevel();
            _disabled = player.isUnActiveSkill(skill.getId());
            _order = ((!skill.isActive() && !skill.isToggle()) ? 1 : 0);
        }

        public void writeRecord() {
            writeD(_order);
            writeD(_lvl);
            writeD(_id);
            writeC(_disabled ? 1 : 0);
        }

        @Override
        public int compareTo(final SkillListRecord o) {
            return _id - o._id;
        }
    }
}
