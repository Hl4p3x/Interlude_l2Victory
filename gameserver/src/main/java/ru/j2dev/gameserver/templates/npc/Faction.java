package ru.j2dev.gameserver.templates.npc;

import gnu.trove.list.array.TIntArrayList;

import java.util.stream.IntStream;

public class Faction {
    public static final String none = "none";
    public static final Faction NONE = new Faction("none");

    public final String factionId;
    public int factionRange;
    public TIntArrayList ignoreId = new TIntArrayList();

    public Faction(final String factionId) {
        this.factionId = factionId;
    }

    public String getName() {
        return factionId;
    }

    public int getRange() {
        return factionRange;
    }

    public void setRange(final int factionRange) {
        this.factionRange = factionRange;
    }

    public void addIgnoreNpcId(final int npcId) {
        ignoreId.add(npcId);
    }

    public void addIgnoreNpcId(final int... npcId) {
       IntStream.of(npcId).forEach(this::addIgnoreNpcId);
    }

    public boolean isIgnoreNpcId(final int npcId) {
        return ignoreId.contains(npcId);
    }

    public boolean isNone() {
        return factionId.isEmpty() || "none".equals(factionId);
    }

    public boolean equals(final Faction faction) {
        return !isNone() && faction.getName().equalsIgnoreCase(factionId);
    }

    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == getClass() && equals((Faction) o));
    }

    @Override
    public String toString() {
        return isNone() ? "none" : factionId;
    }
}
