package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.entity.olympiad.OlympiadGameType;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadStadium;

import java.util.ArrayList;

public class ExReceiveOlympiadGameList extends L2GameServerPacket {
    private final ArrayList<GameRec> _games;

    public ExReceiveOlympiadGameList() {
        _games = new ArrayList<>();
    }

    public void add(final OlympiadStadium sid, final OlympiadGameType _type, final int _state, final String p0, final String p1) {
        _games.add(new GameRec(sid, _type, _state, p0, p1));
    }

    @Override
    protected void writeImpl() {
        writeEx(0xd4);
        writeD(0);
        writeD(_games.size());
        writeD(0);
        _games.forEach(gr -> {
            writeD(gr.stadium_id);
            writeD(gr.type);
            writeD(gr.state);
            writeS(gr.player0name);
            writeS(gr.player1name);
        });
    }

    private class GameRec {
        final int stadium_id;
        final int type;
        final int state;
        final String player0name;
        final String player1name;

        public GameRec(final OlympiadStadium sid, final OlympiadGameType _type, final int _state, final String p0, final String p1) {
            stadium_id = sid.getStadiumId();
            type = _type.getTypeIdx();
            state = _state;
            player0name = p0;
            player1name = p1;
        }
    }
}
