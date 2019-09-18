package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.manager.RaidBossSpawnManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExGetBossRecord;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExGetBossRecord.BossRecordInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RequestGetBossRecord extends L2GameClientPacket {
    private int _bossID;

    @Override
    protected void readImpl() {
        _bossID = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        int totalPoints = 0;
        int ranking = 0;
        if (activeChar == null) {
            return;
        }
        final List<BossRecordInfo> list = new ArrayList<>();
        final Map<Integer, Integer> points = RaidBossSpawnManager.getInstance().getPointsForOwnerId(activeChar.getObjectId());
        if (points != null && !points.isEmpty()) {
            for (final Entry<Integer, Integer> e : points.entrySet()) {
                switch (e.getKey()) {
                    case -1: {
                        ranking = e.getValue();
                        continue;
                    }
                    case 0: {
                        totalPoints = e.getValue();
                        continue;
                    }
                    default: {
                        list.add(new BossRecordInfo(e.getKey(), e.getValue(), 0));
                        continue;
                    }
                }
            }
        }
        activeChar.sendPacket(new ExGetBossRecord(ranking, totalPoints, list));
    }
}
