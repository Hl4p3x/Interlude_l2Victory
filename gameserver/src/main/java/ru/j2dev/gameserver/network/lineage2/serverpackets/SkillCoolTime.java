package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.skills.TimeStamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SkillCoolTime extends L2GameServerPacket {
    private List<Skill> _list;

    public SkillCoolTime(final Player player) {
        _list = Collections.emptyList();
        final Collection<TimeStamp> list = player.getSkillReuses();
        _list = new ArrayList<>(list.size());
        list.stream().filter(TimeStamp::hasNotPassed).forEach(stamp -> {
            final ru.j2dev.gameserver.model.Skill skill = player.getKnownSkill(stamp.getId());
            if (skill == null) {
                return;
            }
            final Skill sk = new Skill();
            sk.skillId = skill.getId();
            sk.level = skill.getLevel();
            sk.reuseBase = (int) Math.floor(stamp.getReuseBasic() / 1000.0);
            sk.reuseCurrent = (int) Math.floor(stamp.getReuseCurrent() / 1000.0);
            _list.add(sk);
        });
    }

    @Override
    protected final void writeImpl() {
        writeC(0xc1);
        writeD(_list.size());
        _list.forEach(sk -> {
            writeD(sk.skillId);
            writeD(sk.level);
            writeD(sk.reuseBase);
            writeD(sk.reuseCurrent);
        });
    }

    private static class Skill {
        public int skillId;
        public int level;
        public int reuseBase;
        public int reuseCurrent;
    }
}
