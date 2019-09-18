package ru.j2dev.gameserver.enums;

/**
 * Created by JunkyFunky
 * on 11.07.2018 22:57
 * group j2dev
 */
public enum ClanPrivs {

    CP_NOTHING(0x0),
    CP_CL_INVITE_CLAN(0x2),
    CP_CL_MANAGE_TITLES(0x4),
    CP_CL_WAREHOUSE_SEARCH(0x8),
    CP_CL_MANAGE_RANKS(0x10),
    CP_CL_CLAN_WAR(0x20),
    CP_CL_DISMISS(0x40),
    CP_CL_EDIT_CREST(0x80),
    CP_CL_APPRENTICE(0x100),
    CP_CL_TROOPS_FAME(0x200),
    CP_CH_ENTRY_EXIT(0x400),
    CP_CH_USE_FUNCTIONS(0x800),
    CP_CH_AUCTION(0x1000),
    CP_CH_DISMISS(0x2000),
    CP_CH_SET_FUNCTIONS(0x4000),
    CP_CS_ENTRY_EXIT(0x8000),
    CP_CS_MANOR_ADMIN(0x10000),
    CP_CS_MANAGE_SIEGE(0x20000),
    CP_CS_USE_FUNCTIONS(0x40000),
    CP_CS_DISMISS(0x80000),
    CP_CS_TAXES(0x100000),
    CP_CS_MERCENARIES(0x200000),
    CP_CS_SET_FUNCTIONS(0x400000),
    CP_ALL(0x7ffffe);


    public int getValue() {
        return value;
    }

    final int value;

    ClanPrivs(final int value) {
        this.value = value;
    }

    public static ClanPrivs getByValue(final int value) {
        for(ClanPrivs privs : values()) {
            if(privs.getValue() == value) {
                return privs;
            }
        }
        return CP_NOTHING;
    }
}
