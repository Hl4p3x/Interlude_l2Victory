package ru.j2dev.gameserver.templates.npc;

public class MinionData {
    private final int _minionId;
    private final int _minionAmount;

    public MinionData(final int minionId, final int minionAmount) {
        _minionId = minionId;
        _minionAmount = minionAmount;
    }
    public MinionData(final String... minionString) {
        this(Integer.parseInt(minionString[0]), Integer.parseInt(minionString[1]));
    }

    public int getMinionId() {
        return _minionId;
    }

    public int getAmount() {
        return _minionAmount;
    }

    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == getClass() && ((MinionData) o).getMinionId() == getMinionId());
    }
}
