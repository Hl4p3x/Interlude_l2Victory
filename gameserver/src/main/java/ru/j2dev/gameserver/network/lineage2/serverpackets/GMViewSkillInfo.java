package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;

import java.util.Collection;

public class GMViewSkillInfo extends L2GameServerPacket {
    private final String _charName;
    private final Collection<Skill> _skills;
    private final Player _targetChar;

    public GMViewSkillInfo(final Player cha) {
        _charName = cha.getName();
        _skills = cha.getAllSkills();
        _targetChar = cha;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x91);
        writeS(_charName);
        writeD(_skills.size());
        _skills.forEach(skill -> {
            writeD(skill.isPassive() ? 1 : 0);
            writeD(skill.getDisplayLevel());
            writeD(skill.getId());
            writeC(_targetChar.isUnActiveSkill(skill.getId()) ? 1 : 0);
        });
    }
}
