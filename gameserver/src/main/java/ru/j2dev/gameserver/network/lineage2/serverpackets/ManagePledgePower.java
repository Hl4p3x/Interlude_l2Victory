package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.RankPrivs;

public class ManagePledgePower extends L2GameServerPacket {
    private final int _action;
    private final int _clanId;
    private final int privs;

    public ManagePledgePower(final Player player, final int action, final int rank) {
        _clanId = player.getClanId();
        _action = action;
        final RankPrivs temp = player.getClan().getRankPrivs(rank);
        privs = ((temp == null) ? 0 : temp.getPrivs());
        player.sendPacket(new PledgeReceiveUpdatePower(privs));
    }

    @Override
    protected final void writeImpl() {
        writeC(0x30);
        writeD(_clanId);
        writeD(_action);
        writeD(privs);
    }
}
