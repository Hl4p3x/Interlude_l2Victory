package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Manor;
import ru.j2dev.gameserver.templates.manor.SeedProduction;

import java.util.List;

public class ExShowSeedInfo extends L2GameServerPacket {
    private final List<SeedProduction> _seeds;
    private final int _manorId;

    public ExShowSeedInfo(final int manorId, final List<SeedProduction> seeds) {
        _manorId = manorId;
        _seeds = seeds;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x1c);
        writeC(0);
        writeD(_manorId);
        writeD(0);
        writeD(_seeds.size());
        _seeds.forEach(seed -> {
            writeD(seed.getId());
            writeD((int) seed.getCanProduce());
            writeD((int) seed.getStartProduce());
            writeD((int) seed.getPrice());
            writeD(Manor.getInstance().getSeedLevel(seed.getId()));
            writeC(1);
            writeD(Manor.getInstance().getRewardItemBySeed(seed.getId(), 1));
            writeC(1);
            writeD(Manor.getInstance().getRewardItemBySeed(seed.getId(), 2));
        });
    }
}
