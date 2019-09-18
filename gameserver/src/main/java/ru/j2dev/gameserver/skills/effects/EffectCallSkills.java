package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.tables.SkillTable;

public class EffectCallSkills extends Effect {
    public EffectCallSkills(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        final int[] skillIds = getTemplate().getParam().getIntegerArray("skillIds");
        final int[] skillLevels = getTemplate().getParam().getIntegerArray("skillLevels");
        for (int i = 0; i < skillIds.length; ++i) {
            final Skill skill = SkillTable.getInstance().getInfo(skillIds[i], skillLevels[i]);
            for (final Creature cha : skill.getTargets(getEffector(), getEffected(), false)) {
                getEffector().broadcastPacket(new MagicSkillUse(getEffector(), cha, skillIds[i], skillLevels[i], 0, 0L));
            }
            getEffector().callSkill(skill, skill.getTargets(getEffector(), getEffected(), false), false);
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
