package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class LoginFail extends L2GameServerPacket {
    public static int NO_TEXT;
    public static int SYSTEM_ERROR_LOGIN_LATER = 1;
    public static int PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT = 2;
    public static int PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT2 = 3;
    public static int ACCESS_FAILED_TRY_LATER = 4;
    public static int INCORRECT_ACCOUNT_INFO_CONTACT_CUSTOMER_SUPPORT = 5;
    public static int ACCESS_FAILED_TRY_LATER2 = 6;
    public static int ACOUNT_ALREADY_IN_USE = 7;
    public static int ACCESS_FAILED_TRY_LATER3 = 8;
    public static int ACCESS_FAILED_TRY_LATER4 = 9;
    public static int ACCESS_FAILED_TRY_LATER5 = 10;

    private int _reason;

    public LoginFail(final int reason) {
        _reason = reason;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x14);
        writeD(_reason);
    }
}
