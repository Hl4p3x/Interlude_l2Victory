package ru.j2dev.gameserver.network.lineage2;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.network.lineage2.clientpackets.L2GameClientPacket;

public abstract class CGMHelper {
    private static final CGMHelper INSTANCE = init();

    private static CGMHelper init() {
        CGMHelper inst = null;
        try {
            String instClassName = Config.ALT_CG_MODULE;
            for (final CGMType cgmType : CGMType.values()) {
                if (cgmType.name().equals(Config.ALT_CG_MODULE)) {
                    instClassName = cgmType.getImplClassName();
                }
            }
            if (instClassName != null) {
                inst = (CGMHelper) Class.forName(instClassName).newInstance();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return inst;
    }

    public static CGMHelper getInstance() {
        return INSTANCE;
    }

    public static boolean isActive() {
        return getInstance() != null;
    }

    public abstract L2GameClientPacket handle(final GameClient p0, final int p1);

    public abstract GameCrypt createCrypt();

    public abstract byte[] getRandomKey();

    public abstract void addHWIDBan(final String p0, final String p1, final String p2, final String p3);

    public enum CGMType {
        NONE(null),
        LAMEGUARD("ru.j2dev.gameserver.network.lineage2.cgm.LameGuardHelperImpl"),
        SMARTGUARD("ru.j2dev.gameserver.network.lineage2.cgm.SmartGuardHelperImpl"),
        STRIXGUARD("ru.j2dev.gameserver.network.lineage2.cgm.StrixGuardHelperImpl");

        private final String _implClassName;

        CGMType(final String implClassName) {
            _implClassName = implClassName;
        }

        public String getImplClassName() {
            return _implClassName;
        }

        public boolean isActive() {
            return _implClassName != null;
        }
    }
}
