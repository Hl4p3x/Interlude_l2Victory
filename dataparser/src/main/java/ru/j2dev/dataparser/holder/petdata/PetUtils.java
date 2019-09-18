package ru.j2dev.dataparser.holder.petdata;

/**
 * @author KilRoy
 */
public class PetUtils {
    public static int getMinLevel(final int npcTemplate) {
        switch (npcTemplate) {
            case PetId.FOX_SHAMAN_ID:
            case PetId.WILD_BEAST_FIGHTER_ID:
            case PetId.WHITE_WEASEL_ID:
            case PetId.FAIRY_PRINCESS_ID:
            case PetId.OWL_MONK_ID:
            case PetId.SPIRIT_SHAMAN_ID:
            case PetId.TOY_KNIGHT_ID:
            case PetId.TURTLE_ASCETIC_ID:
                return 25;
            case PetId.GREAT_WOLF_ID:
            case PetId.GREAT_WOLF_EVENT_ID:
            case PetId.IMPROVED_BABY_BUFFALO_ID:
            case PetId.IMPROVED_BABY_KOOKABURRA_ID:
            case PetId.IMPROVED_BABY_COUGAR_ID:
            case PetId.WGREAT_WOLF_ID:
            case PetId.DEINONYCHUS_ID:
            case PetId.GUARDIANS_STRIDER_ID:
                return 55;
            case PetId.FENRIR_WOLF_ID:
            case PetId.WFENRIR_WOLF_ID:
                return 70;
            default:
                return 1;
        }
    }

    public static boolean isMountable(final int npcTemplateId) {
        switch (npcTemplateId) {
            case PetId.WGREAT_WOLF_ID:
            case PetId.FENRIR_WOLF_ID:
            case PetId.WFENRIR_WOLF_ID:
            case PetId.STRIDER_WIND_ID:
            case PetId.STRIDER_STAR_ID:
            case PetId.STRIDER_TWILIGHT_ID:
            case PetId.RED_STRIDER_WIND_ID:
            case PetId.RED_STRIDER_STAR_ID:
            case PetId.RED_STRIDER_TWILIGHT_ID:
            case PetId.GUARDIANS_STRIDER_ID:
            case PetId.WYVERN_ID:
                return true;
            default:
                return false;
        }
    }

    public static boolean isWoldForPacket(final int id) {
        switch (id) {
            case PetId.PET_WOLF_ID:
            case PetId.GREAT_WOLF_ID:
            case PetId.GREAT_WOLF_EVENT_ID:
            case PetId.WGREAT_WOLF_ID:
            case PetId.FENRIR_WOLF_ID:
            case PetId.WFENRIR_WOLF_ID:
                return true;
            default:
                return false;
        }
    }

    public static boolean isWolf(final int id) {
        return id == PetId.PET_WOLF_ID;
    }

    public static boolean isGWolf(final int id) {
        switch (id) {
            case PetId.GREAT_WOLF_ID:
            case PetId.GREAT_WOLF_EVENT_ID:
            case PetId.WGREAT_WOLF_ID:
            case PetId.FENRIR_WOLF_ID:
            case PetId.WFENRIR_WOLF_ID:
                return true;
            default:
                return false;
        }
    }

    public static boolean isHatchling(final int id) {
        switch (id) {
            case PetId.HATCHLING_WIND_ID:
            case PetId.HATCHLING_STAR_ID:
            case PetId.HATCHLING_TWILIGHT_ID:
                return true;
            default:
                return false;
        }
    }

    public static boolean isStrider(final int id) {
        switch (id) {
            case PetId.STRIDER_WIND_ID:
            case PetId.STRIDER_STAR_ID:
            case PetId.STRIDER_TWILIGHT_ID:
            case PetId.RED_STRIDER_WIND_ID:
            case PetId.RED_STRIDER_STAR_ID:
            case PetId.RED_STRIDER_TWILIGHT_ID:
            case PetId.GUARDIANS_STRIDER_ID:
                return true;
            default:
                return false;
        }
    }

    public static boolean isBabyPet(final int id) {
        switch (id) {
            case PetId.BABY_BUFFALO_ID:
            case PetId.BABY_KOOKABURRA_ID:
            case PetId.BABY_COUGAR_ID:
                return true;
            default:
                return false;
        }
    }

    public static boolean isImprovedBabyPet(final int id) {
        switch (id) {
            case PetId.IMPROVED_BABY_BUFFALO_ID:
            case PetId.IMPROVED_BABY_KOOKABURRA_ID:
            case PetId.IMPROVED_BABY_COUGAR_ID:
            case PetId.FAIRY_PRINCESS_ID:
                return true;
            default:
                return false;
        }
    }

    public static boolean isVitaminPet(final int id) {
        switch (id) {
            case PetId.FOX_SHAMAN_ID:
            case PetId.WILD_BEAST_FIGHTER_ID:
            case PetId.WHITE_WEASEL_ID:
            case PetId.FAIRY_PRINCESS_ID:
            case PetId.OWL_MONK_ID:
            case PetId.SPIRIT_SHAMAN_ID:
            case PetId.TOY_KNIGHT_ID:
            case PetId.TURTLE_ASCETIC_ID:
                return true;
            default:
                return false;
        }
    }

    public static boolean isEventPet(final int id) {
        switch (id) {
            case PetId.BR_BABY_RUDOLF_ID:
            case PetId.BR_BABY_RUDOLF_P_ID:
            case PetId.BR_ROSE_DZELLOF_A:
            case PetId.BR_ROSE_HUME_A:
            case PetId.BR_ROSE_LEKAN_A:
            case PetId.BR_ROSE_LILIATH_A:
            case PetId.BR_ROSE_LAFFAME_A:
            case PetId.BR_ROSE_MAFFUME_A:
            case PetId.BR_ROSE_DZELLOF_B:
            case PetId.BR_ROSE_HUME_B:
            case PetId.BR_ROSE_LEKAN_B:
            case PetId.BR_ROSE_LILIATH_B:
            case PetId.BR_ROSE_LAFFAME_B:
            case PetId.BR_ROSE_MAFFUME_B:
                return true;
            default:
                return false;
        }
    }

    public static class PetId {
        public static final int PET_WOLF_ID = 12077;

        public static final int HATCHLING_WIND_ID = 12311;
        public static final int HATCHLING_STAR_ID = 12312;
        public static final int HATCHLING_TWILIGHT_ID = 12313;

        public static final int STRIDER_WIND_ID = 12526;
        public static final int STRIDER_STAR_ID = 12527;
        public static final int STRIDER_TWILIGHT_ID = 12528;

        public static final int SIN_EATER_ID = 12564;

        public static final int BABY_BUFFALO_ID = 12780;
        public static final int BABY_KOOKABURRA_ID = 12781;
        public static final int BABY_COUGAR_ID = 12782;

        public static final int GREAT_WOLF_ID = 16025;
        public static final int GREAT_WOLF_EVENT_ID = 16030;
        public static final int WGREAT_WOLF_ID = 16037;

        public static final int RED_STRIDER_WIND_ID = 16038;
        public static final int RED_STRIDER_STAR_ID = 16039;
        public static final int RED_STRIDER_TWILIGHT_ID = 16040;

        public static final int IMPROVED_BABY_BUFFALO_ID = 16034;
        public static final int IMPROVED_BABY_KOOKABURRA_ID = 16035;
        public static final int IMPROVED_BABY_COUGAR_ID = 16036;

        public static final int FENRIR_WOLF_ID = 16041;
        public static final int WFENRIR_WOLF_ID = 16042;

        public static final int FOX_SHAMAN_ID = 16043;
        public static final int WILD_BEAST_FIGHTER_ID = 16044;
        public static final int WHITE_WEASEL_ID = 16045;
        public static final int FAIRY_PRINCESS_ID = 16046;
        public static final int OWL_MONK_ID = 16050;
        public static final int SPIRIT_SHAMAN_ID = 16051;
        public static final int TOY_KNIGHT_ID = 16052;
        public static final int TURTLE_ASCETIC_ID = 16053;
        public static final int DEINONYCHUS_ID = 16067;
        public static final int GUARDIANS_STRIDER_ID = 16068;

        public static final int MAGUEN_ID = 16071;
        public static final int MAGUEN_ELITE_ID = 16072;

        public static final int WYVERN_ID = 12621;

        public static final int BR_BABY_RUDOLF_ID = 1538;
        public static final int BR_BABY_RUDOLF_P_ID = 1561;

        public static final int BR_ROSE_DZELLOF_A = 1562;
        public static final int BR_ROSE_HUME_A = 1563;
        public static final int BR_ROSE_LEKAN_A = 1564;
        public static final int BR_ROSE_LILIATH_A = 1565;
        public static final int BR_ROSE_LAFFAME_A = 1566;
        public static final int BR_ROSE_MAFFUME_A = 1567;
        public static final int BR_ROSE_DZELLOF_B = 1568;
        public static final int BR_ROSE_HUME_B = 1569;
        public static final int BR_ROSE_LEKAN_B = 1570;
        public static final int BR_ROSE_LILIATH_B = 1571;
        public static final int BR_ROSE_LAFFAME_B = 1572;
        public static final int BR_ROSE_MAFFUME_B = 1573;
    }
}