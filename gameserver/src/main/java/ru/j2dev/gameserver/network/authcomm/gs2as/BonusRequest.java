package ru.j2dev.gameserver.network.authcomm.gs2as;

import ru.j2dev.gameserver.network.authcomm.SendablePacket;

public class BonusRequest extends SendablePacket {
    private final String account;
    private final double bonus;
    private final int bonusExpire;

    public BonusRequest(final String account, final double bonus, final int bonusExpire) {
        this.account = account;
        this.bonus = bonus;
        this.bonusExpire = bonusExpire;
    }

    @Override
    protected void writeImpl() {
        writeC(0x10);
        writeS(account);
        writeF(bonus);
        writeD(bonusExpire);
    }
}
