package ru.j2dev.gameserver.utils;


import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.skills.EffectType;
import ru.j2dev.gameserver.skills.effects.EffectTemplate;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.tables.SkillTable;

import java.util.Objects;

public class SkillUtils {

    public static String getSkillName(final int id) {
        return getSkillName(id, SkillTable.getInstance().getBaseLevel(id));
    }

    public static String getSkillName(final int id, final int level) {
        final Skill skill = SkillTable.getInstance().getInfo(id, level);
        return skill == null ? "NoNameSkill" : skill.getName();
    }

    public static String getSkillIcon(final int id) {
        return getSkillIcon(id, SkillTable.getInstance().getBaseLevel(id));
    }

    public static String getSkillIcon(final int id, final int level) {
        final Skill skill = SkillTable.getInstance().getInfo(id, level);
        return skill == null ? "icon.skill0000" : skill.getIcon();
    }

    public static void applySkillEffect(final int id, final int level, final long time, final Creature actor) {
        applySkillEffect(id, level, time, actor, false);
    }

    public static void applySkillEffect(final int id, final int level, final long time, final Creature actor, final boolean animation) {
        final Skill skill = SkillTable.getInstance().getInfo(id, level);
        applySkillEffect(skill, time, actor, animation);
    }

    public static void applySkillEffect(final Skill skill, final long time, final Creature actor, final boolean animation) {
        if (actor == null) {
            return;
        }
        if (animation) {
            actor.broadcastPacket(new MagicSkillUse(actor, actor, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
        }
        if (skill != null) {
            skill.getEffects(actor, actor, false, false, time, 1.0, false);
        }
    }

    public static void imposeSkillEffect(final int id, final int level, final long time, final Playable actor) {
        if (actor == null) {
            return;
        }
        final Skill skill = SkillTable.getInstance().getInfo(id, level);
        imposeSkillEffect(skill, time, actor);
    }

    public static void imposeSkillEffect(final Skill skill, final long time, final Playable actor) {
        if (actor == null) {
            return;
        }
        addEffect(actor, skill, time);
    }

    private static void addEffect(Playable actor, Skill skill, long time) {
        for (final EffectTemplate et : skill.getEffectTemplates()) {
            final Env env = new Env(actor, actor, skill);
            final Effect effect = et.getEffect(env);
            if (Objects.requireNonNull(effect).getEffectType() == EffectType.HealPercent || effect.getEffectType() == EffectType.HealCPPercent) {
                continue;
            }
            effect.setPeriod(time);
            actor.getEffectList().addEffect(effect);
            actor.sendChanges();
            actor.updateEffectIcons();
        }
    }
}
