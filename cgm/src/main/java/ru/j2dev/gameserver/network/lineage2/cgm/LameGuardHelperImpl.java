package ru.j2dev.gameserver.network.lineage2.cgm;

import com.lameguard.BanList;
import com.lameguard.Config;
import com.lameguard.LameGuard;
import com.lameguard.crypt.BlowFishKeygen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.network.lineage2.CGMHelper;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.GameClient.GameClientState;
import ru.j2dev.gameserver.network.lineage2.GameCrypt;
import ru.j2dev.gameserver.network.lineage2.cgm.lg.LGGameCryptWrapper;
import ru.j2dev.gameserver.network.lineage2.cgm.lg.LGProtocolVersionPacket;
import ru.j2dev.gameserver.network.lineage2.clientpackets.L2GameClientPacket;

public class LameGuardHelperImpl extends CGMHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(LameGuardHelperImpl.class);

    public LameGuardHelperImpl() {
        LOGGER.info("LameGuardHelper: Init ...");
        Config.load();
        LameGuard.getInstance();
        BanList.getInstance();
    }

    @Override
    public L2GameClientPacket handle(final GameClient client, final int opcode) {
        if (client.getState() == GameClientState.CONNECTED && opcode == 0) {
            return new LGProtocolVersionPacket();
        }
        return null;
    }

    @Override
    public GameCrypt createCrypt() {
        return new LGGameCryptWrapper();
    }

    @Override
    public byte[] getRandomKey() {
        return BlowFishKeygen.getRandomKey();
    }

    @Override
    public void addHWIDBan(final String hwid, final String ip, final String account, final String comment) {
        if (hwid != null && !hwid.isEmpty()) {
            BanList.getInstance().addHWID(hwid, ip, account, comment);
        }
    }
}