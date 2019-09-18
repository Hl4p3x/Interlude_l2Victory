package ru.j2dev.gameserver.network.lineage2.cgm.sxg;

import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.GameCrypt;
import ru.j2dev.gameserver.network.lineage2.clientpackets.L2GameClientPacket;

public interface ISXGWrapper {
    boolean isEnabled();

    void init();

    byte[] getRandomKey();

    void addHWIDBan(final String p0, final String p1, final String p2, final String p3);

    L2GameClientPacket handle(final GameClient p0, final int p1);

    GameCrypt createCrypt();
}
