package ru.j2dev.gameserver.network.lineage2.cgm;

import ru.akumu.smartguard.core.SmartCore;
import ru.akumu.smartguard.core.manager.LicenseManager;
import ru.akumu.smartguard.core.manager.bans.Ban;
import ru.akumu.smartguard.core.manager.bans.BanManager;
import ru.akumu.smartguard.core.manager.session.HWID;
import ru.akumu.smartguard.core.network.BlowFishKeygen;
import ru.j2dev.gameserver.handler.admincommands.AdminCommandHandler;
import ru.j2dev.gameserver.network.lineage2.CGMHelper;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.GameClient.GameClientState;
import ru.j2dev.gameserver.network.lineage2.GameCrypt;
import ru.j2dev.gameserver.network.lineage2.clientpackets.L2GameClientPacket;
import ru.j2dev.gameserver.network.lineage2.cgm.sg.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

public class SmartGuardHelperImpl extends CGMHelper {
    private static Map<LicenseManager.KeyType, byte[]> SG_KEYS;

    public SmartGuardHelperImpl() {
        init();
    }

    private static void init() {
        if (!SmartCore.init(ServerInterface.getInstance())) {
            throw new RuntimeException("Can't init SmartGuard.");
        }
        SG_KEYS = getLicenseMangerKeys();
        AdminCommandHandler.getInstance().registerAdminCommandHandler(AdminMenuWrapper.getInstance());
    }

    public static byte[] getSGKey(final LicenseManager.KeyType keyType) {
        final byte[] key = SG_KEYS.get(keyType);
        if (key != null) {
            return Arrays.copyOf(key, key.length);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<LicenseManager.KeyType, byte[]> getLicenseMangerKeys() {
        final LicenseManager lmInstance = LicenseManager.getInstance();
        try {
            final Class<LicenseManager> lmClazz = LicenseManager.class;
            final Field keysField = lmClazz.getDeclaredField("_keys");
            keysField.setAccessible(true);
            return (Map<LicenseManager.KeyType, byte[]>) keysField.get(lmInstance);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public L2GameClientPacket handle(final GameClient client, final int opcode) {
        if (client.getState() == GameClientState.CONNECTED) {
            if (opcode == 0x0) {
                return new SGProtocolVersionPacket();
            }
            if (opcode == 0x8) {
                return new SGAuthLoginPacket();
            }
        }
        return null;
    }

    @Override
    public GameCrypt createCrypt() {
        return new SGGameCryptRC4();
    }

    @Override
    public byte[] getRandomKey() {
        return BlowFishKeygen.getRandomKey();
    }

    @Override
    public void addHWIDBan(final String hwidStr, final String ip, final String account, final String comment) {
        final HWID hwid = HWID.fromString(hwidStr);
        if (hwid != null) {
            final Ban ban = new Ban(hwid, String.format("HWID ban ip:%s account:\"%s\" : \"%s\"", ip, account, comment));
            BanManager.addBan(ban);
        }
    }
}