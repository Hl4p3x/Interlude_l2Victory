package ru.j2dev.gameserver.skills;

/**
 * @author VISTALL
 * @date 0:15/03.06.2011
 */
enum SkillEntryType {

    NONE, // 0
    EQUIP, // 1
    CLAN, // 2
    RESERVE_3, RESERVE_4, CERTIFICATION, // 5
    RESERVE_6, TRANSFER_CARDINAL, // 7
    TRANSFER_EVA_SAINTS, // 8
    TRANSFER_SHILLIEN_SAINTS; // 9

    public static final SkillEntryType[] VALUES = values();
}
