package ru.j2dev.gameserver.model.base;

import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;

public enum SkillTrait {
    NONE,
    BLEED {
        @Override
        public final double calcVuln(final Env env) {
            return env.target.calcStat(Stats.BLEED_RESIST, env.character, env.skill);
        }

        @Override
        public final double calcProf(final Env env) {
            return env.character.calcStat(Stats.BLEED_POWER, env.target, env.skill);
        }
    },
    BOSS,
    DEATH,
    DERANGEMENT {
        @Override
        public final double calcVuln(final Env env) {
            return env.target.calcStat(Stats.MENTAL_RESIST, env.character, env.skill);
        }

        @Override
        public final double calcProf(final Env env) {
            return Math.min(40.0, env.character.calcStat(Stats.MENTAL_POWER, env.target, env.skill) + SkillTrait.calcEnchantMod(env));
        }
    },
    ETC,
    GUST,
    HOLD {
        @Override
        public final double calcVuln(final Env env) {
            return env.target.calcStat(Stats.ROOT_RESIST, env.character, env.skill);
        }

        @Override
        public final double calcProf(final Env env) {
            return env.character.calcStat(Stats.ROOT_POWER, env.target, env.skill);
        }
    },
    PARALYZE {
        @Override
        public final double calcVuln(final Env env) {
            return env.target.calcStat(Stats.PARALYZE_RESIST, env.character, env.skill);
        }

        @Override
        public final double calcProf(final Env env) {
            return env.character.calcStat(Stats.PARALYZE_POWER, env.target, env.skill);
        }
    },
    PHYSICAL_BLOCKADE,
    POISON {
        @Override
        public final double calcVuln(final Env env) {
            return env.target.calcStat(Stats.POISON_RESIST, env.character, env.skill);
        }

        @Override
        public final double calcProf(final Env env) {
            return env.character.calcStat(Stats.POISON_POWER, env.target, env.skill);
        }
    },
    SHOCK {
        @Override
        public final double calcVuln(final Env env) {
            return env.target.calcStat(Stats.STUN_RESIST, env.character, env.skill);
        }

        @Override
        public final double calcProf(final Env env) {
            return Math.min(40.0, env.character.calcStat(Stats.STUN_POWER, env.target, env.skill) + SkillTrait.calcEnchantMod(env));
        }
    },
    SLEEP {
        @Override
        public final double calcVuln(final Env env) {
            return env.target.calcStat(Stats.SLEEP_RESIST, env.character, env.skill);
        }

        @Override
        public final double calcProf(final Env env) {
            return env.character.calcStat(Stats.SLEEP_POWER, env.target, env.skill);
        }
    },
    VALAKAS;

    public static double calcEnchantMod(final Env env) {
        int enchantLevel = env.skill.getDisplayLevel();
        if (enchantLevel <= 100) {
            return 0.0;
        }
        enchantLevel %= 100;
        return (env.skill.getEnchantLevelCount() == 15) ? (enchantLevel * 2) : ((double) enchantLevel);
    }

    public double calcVuln(final Env env) {
        return 0.0;
    }

    public double calcProf(final Env env) {
        return 0.0;
    }
}
