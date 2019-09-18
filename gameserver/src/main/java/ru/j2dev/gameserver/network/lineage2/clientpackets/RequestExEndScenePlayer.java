package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;

public class RequestExEndScenePlayer extends L2GameClientPacket {
    private int _movieId;

    @Override
    protected void readImpl() {
        _movieId = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (!activeChar.isInMovie() || activeChar.getMovieId() != _movieId) {
            activeChar.sendActionFailed();
            return;
        }
        activeChar.setIsInMovie(false);
        activeChar.setMovieId(0);
        activeChar.decayMe();
        activeChar.spawnMe();
    }
}
