package ru.j2dev.gameserver.stats.funcs;

public interface FuncOwner {
    boolean isFuncEnabled();

    boolean overrideLimits();
}
