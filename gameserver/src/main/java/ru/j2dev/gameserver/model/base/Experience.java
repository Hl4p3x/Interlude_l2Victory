package ru.j2dev.gameserver.model.base;

import ru.j2dev.gameserver.Config;

public class Experience {
    public static final long[] LEVEL = {
            -1L,
            0L,
            68L,
            363L,
            1168L,
            2884L,
            6038L,
            11287L,
            19423L,
            31378L,
            48229L,
            71201L,
            101676L,
            141192L,
            191452L,
            254327L,
            331864L,
            426284L,
            539995L,
            675590L,
            835854L,
            1023775L,
            1242536L,
            1495531L,
            1786365L,
            2118860L,
            2497059L,
            2925229L,
            3407873L,
            3949727L,
            4555766L,
            5231213L,
            5981539L,
            6812472L,
            7729999L,
            8740372L,
            9850111L,
            11066012L,
            12395149L,
            13844879L,
            15422851L,
            17137002L,
            18995573L,
            21007103L,
            23180442L,
            25524751L,
            28049509L,
            30764519L,
            33679907L,
            36806133L,
            40153995L,
            45524865L,
            51262204L,
            57383682L,
            63907585L,
            70852742L,
            80700339L,
            91162131L,
            102265326L,
            114038008L,
            126509030L,
            146307211L,
            167243291L,
            189363788L,
            212716741L,
            237351413L,
            271973532L,
            308441375L,
            346825235L,
            387197529L,
            429632402L,
            474205751L,
            532692055L,
            606319094L,
            696376867L,
            804219972L,
            931275828L,
            1151275834L,
            1511275834L,
            2099275834L,
            4200000000L,
            6300000000L};

    public static double penaltyModifier(final long count, final double percents) {
        return Math.max(1.0 - count * percents / 100.0, 0.0);
    }

    public static int getMaxLevel() {
        return Config.ALT_MAX_LEVEL;
    }

    public static int getMaxSubLevel() {
        return Config.ALT_MAX_SUB_LEVEL;
    }

    public static int getLevel(final long thisExp) {
        int level = 0;
        for (int i = 0; i < LEVEL.length; ++i) {
            final long exp = LEVEL[i];
            if (thisExp >= exp) {
                level = i;
            }
        }
        return level;
    }

    public static long getExpForLevel(final int lvl) {
        if (lvl >= LEVEL.length) {
            return 0L;
        }
        return LEVEL[lvl];
    }

    public static double getExpPercent(final int level, final long exp) {
        return (exp - getExpForLevel(level)) / ((getExpForLevel(level + 1) - getExpForLevel(level)) / 100.0) * 0.01;
    }
}
