package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadPlayer;

import java.util.ArrayList;
import java.util.List;

public class ExReceiveOlympiadResult extends L2GameServerPacket {
    private final String _winner;
    private final int _winner_side;
    private final ArrayList<ExReceiveOlympiadResultRecord> _Red;
    private final ArrayList<ExReceiveOlympiadResultRecord> _Blue;

    public ExReceiveOlympiadResult(final int winner_side, final String winner) {
        _winner = winner;
        _winner_side = winner_side;
        _Red = new ArrayList<>();
        _Blue = new ArrayList<>();
    }

    public void add(final int side, final Player player, final int dmg, final int points, final int delta) {
        if (side == OlympiadPlayer.SIDE_RED) {
            _Red.add(new ExReceiveOlympiadResultRecord(player, dmg, points, delta));
        }
        if (side == OlympiadPlayer.SIDE_BLUE) {
            _Blue.add(new ExReceiveOlympiadResultRecord(player, dmg, points, delta));
        }
    }

    @Override
    protected void writeImpl() {
        writeEx(0xd4);
        writeD(1);
        if (_winner_side != 0) {
            writeD(0);
            writeS(_winner);
        } else {
            writeD(1);
            writeS("");
        }
        if (_winner_side == OlympiadPlayer.SIDE_RED) {
            writeD(1);
            recordResult(_Red);
            writeD(0);
            recordResult(_Blue);
        } else {
            writeD(0);
            recordResult(_Blue);
            writeD(1);
            recordResult(_Red);
        }
    }

    private void recordResult(List<ExReceiveOlympiadResultRecord> resultRecords) {
        writeD(resultRecords.size());
        for (final ExReceiveOlympiadResultRecord orr : resultRecords) {
            writeS(orr.name);
            writeS(orr.clan);
            writeD(orr.crest_id);
            writeD(orr.class_id);
            writeD(orr.dmg);
            writeD(orr.points);
            writeD(orr.delta);
        }
    }

    private class ExReceiveOlympiadResultRecord {
        final String name;
        final String clan;
        final int class_id;
        final int crest_id;
        final int dmg;
        final int points;
        final int delta;

        public ExReceiveOlympiadResultRecord(final Player player, final int _dmg, final int _points, final int _delta) {
            name = player.getName();
            class_id = player.getClassId().getId();
            clan = ((player.getClan() != null) ? player.getClan().getName() : "");
            crest_id = player.getClanId();
            dmg = _dmg;
            points = _points;
            delta = _delta;
        }
    }
}
