package ru.j2dev.gameserver.model.base;

public enum RestartType {
    TO_VILLAGE,
    TO_CLANHALL,
    TO_CASTLE,
    TO_FLAG,
    FIXED;

    public static final RestartType[] VALUES = values();

}
