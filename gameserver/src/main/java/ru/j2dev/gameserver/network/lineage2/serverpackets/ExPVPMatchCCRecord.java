package ru.j2dev.gameserver.network.lineage2.serverpackets;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.gameserver.model.Player;

import java.util.LinkedList;
import java.util.List;

public class ExPVPMatchCCRecord extends L2GameServerPacket {
    private final List<Pair<String, Integer>> _result;
    private final PVPMatchCCAction _action;
    private int _len;

    public ExPVPMatchCCRecord(final PVPMatchCCAction action) {
        _action = action;
        _len = 0;
        _result = new LinkedList<>();
    }

    public void addPlayer(final Player player, final int points) {
        ++_len;
        _result.add(new ImmutablePair<>(player.getName(), points));
    }

    @Override
    public void writeImpl() {
        writeEx(0x89);
        writeD(_action.getVal());
        writeD(_len);
        _result.forEach(p -> {
            writeS(p.getLeft());
            writeD(p.getRight());
        });
    }

    public enum PVPMatchCCAction {
        INIT(0),
        UPDATE(1),
        DONE(2);

        private final int _val;

        PVPMatchCCAction(final int val) {
            _val = val;
        }

        public int getVal() {
            return _val;
        }
    }
}
