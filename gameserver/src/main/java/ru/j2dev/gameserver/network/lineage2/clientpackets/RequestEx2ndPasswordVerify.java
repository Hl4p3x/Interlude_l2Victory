package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.SecondPasswordAuth;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Ex2ndPasswordCheck;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Ex2ndPasswordCheck.Ex2ndPasswordCheckResult;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Ex2ndPasswordVerify;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Ex2ndPasswordVerify.Ex2ndPasswordVerifyResult;

public class RequestEx2ndPasswordVerify extends L2GameClientPacket {
    private String _password;

    @Override
    protected void readImpl() {
        _password = readS(8);
    }

    @Override
    protected void runImpl() {
        final GameClient client = getClient();
        final SecondPasswordAuth spa = client.getSecondPasswordAuth();
        if (spa == null) {
            client.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerifyResult.ERROR));
            return;
        }
        if (!spa.isSecondPasswordSet()) {
            client.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheckResult.CREATE));
            return;
        }
        if (spa.isValidSecondPassword(_password)) {
            client.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerifyResult.SUCCESS));
            client.setSecondPasswordAuthed(true);
        } else if (spa.isBlocked()) {
            client.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerifyResult.BLOCK_HOMEPAGE));
        } else {
            client.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerifyResult.FAILED, spa.getTrysCount()));
        }
    }
}
