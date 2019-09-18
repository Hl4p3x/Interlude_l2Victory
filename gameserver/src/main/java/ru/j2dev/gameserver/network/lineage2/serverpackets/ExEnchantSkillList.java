package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.data.xml.holder.EnchantSkillHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.SkillEnchant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ExEnchantSkillList extends L2GameServerPacket {
    private final List<SkillEnchantEntry> _skills;

    public ExEnchantSkillList() {
        _skills = new ArrayList<>();
    }

    public static ExEnchantSkillList packetFor(final Player player) {
        final Collection<Skill> playerSkills = player.getAllSkills();
        final ExEnchantSkillList esl = new ExEnchantSkillList();
        playerSkills.forEach(currSkill -> {
            final int skillId = currSkill.getId();
            final int currSkillLevel = currSkill.getLevel();
            final int baseSkillLevel = currSkill.getBaseLevel();
            if (currSkillLevel < baseSkillLevel) {
                return;
            }
            final Map<Integer, Map<Integer, SkillEnchant>> skillEnchRoutes = EnchantSkillHolder.getInstance().getRoutesOf(skillId);
            if (skillEnchRoutes == null || skillEnchRoutes.isEmpty()) {
                return;
            }
            final SkillEnchant currSkillEnch = EnchantSkillHolder.getInstance().getSkillEnchant(skillId, currSkillLevel);
            if (currSkillLevel == baseSkillLevel) {
                skillEnchRoutes.values()
                        .stream().
                        flatMap(skillEnchLevels -> skillEnchLevels.values().
                                stream()).
                        filter(newSkillEnch -> newSkillEnch.getEnchantLevel() == 1).
                        forEach(newSkillEnch -> esl.addSkill(newSkillEnch.getSkillId(), newSkillEnch.getSkillLevel(), newSkillEnch.getSp(), newSkillEnch.getExp()));
            } else {
                if (currSkillEnch == null) {
                    return;
                }
                final Map<Integer, SkillEnchant> skillEnchLevels2 = skillEnchRoutes.get(currSkillEnch.getRouteId());
                final int newSkillLevel = currSkillLevel + 1;
                final SkillEnchant newSkillEnch2 = skillEnchLevels2.get(newSkillLevel);
                if (newSkillEnch2 == null) {
                    return;
                }
                esl.addSkill(newSkillEnch2.getSkillId(), newSkillEnch2.getSkillLevel(), newSkillEnch2.getSp(), newSkillEnch2.getExp());
            }
        });
        return esl;
    }

    public void addSkill(final int id, final int level, final int sp, final long exp) {
        _skills.add(new SkillEnchantEntry(id, level, sp, exp));
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x17);
        writeD(_skills.size());
        _skills.forEach(SkillEnchantEntry::write);
    }

    class SkillEnchantEntry {
        private final int _skillId;
        private final int _skillLevel;
        private final int _neededSp;
        private final long _neededExp;

        SkillEnchantEntry(final int skillId, final int skillLevel, final int neededSp, final long neededExp) {
            _skillId = skillId;
            _skillLevel = skillLevel;
            _neededSp = neededSp;
            _neededExp = neededExp;
        }

        private void write() {
            writeD(_skillId);
            writeD(_skillLevel);
            writeD(_neededSp);
            writeQ(_neededExp);
        }
    }
}
