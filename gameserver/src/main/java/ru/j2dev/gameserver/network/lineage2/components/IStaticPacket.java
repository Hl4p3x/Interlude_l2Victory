package ru.j2dev.gameserver.network.lineage2.components;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;

public interface IStaticPacket {
    L2GameServerPacket packet(final Player p0);
}
