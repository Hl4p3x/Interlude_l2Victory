package ru.j2dev.gameserver.network.lineage2.cgm.sxg;

import org.strixplatform.StrixPlatform;
import org.strixplatform.configs.MainConfig;
import org.strixplatform.managers.ClientBanManager;
import org.strixplatform.utils.BannedHWIDInfo;
import ru.j2dev.gameserver.network.lineage2.BlowFishKeygen;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.GameCrypt;
import ru.j2dev.gameserver.network.lineage2.clientpackets.L2GameClientPacket;

public final class SXGWrapper implements ISXGWrapper {
    @Override
    public boolean isEnabled() {
        return StrixPlatform.getInstance().isPlatformEnabled();
    }

    @Override
    public void init() {
        StrixPlatform.getInstance();
    }

    @Override
    public byte[] getRandomKey() {
        return BlowFishKeygen.getRandomKey();
    }

    @Override
    public void addHWIDBan(final String hwid, final String ip, final String account, final String comment) {
        final BannedHWIDInfo bannedHWIDInfo = new BannedHWIDInfo(hwid, System.currentTimeMillis() + MainConfig.STRIX_PLATFORM_AUTOMATICAL_BAN_TIME * 60L * 1000L, comment, "none");
        ClientBanManager.getInstance().tryToStoreBan(bannedHWIDInfo);
    }

    @Override
    public L2GameClientPacket handle(final GameClient client, final int opcode) {
        if (client.getState() == GameClient.GameClientState.CONNECTED && opcode == 0) {
            return new SXGProtocolVersionPacket();
        }
        return null;
    }

    @Override
    public GameCrypt createCrypt() {
        return isEnabled() ? new SXGGameCryptWrapper() : new GameCrypt();
    }
}
