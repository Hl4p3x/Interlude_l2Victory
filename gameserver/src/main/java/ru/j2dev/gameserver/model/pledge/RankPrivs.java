package ru.j2dev.gameserver.model.pledge;

public class RankPrivs {
    private final int _rank;
    private int _party;
    private int _privs;

    public RankPrivs(final int rank, final int party, final int privs) {
        _rank = rank;
        _party = party;
        _privs = privs;
    }

    public int getRank() {
        return _rank;
    }

    public int getParty() {
        return _party;
    }

    public void setParty(final int party) {
        _party = party;
    }

    public int getPrivs() {
        return _privs;
    }

    public void setPrivs(final int privs) {
        _privs = privs;
    }
}
