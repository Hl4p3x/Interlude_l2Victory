package ru.j2dev.gameserver.model.reward;

public enum RewardType {
    RATED_GROUPED,
    NOT_RATED_NOT_GROUPED,
    NOT_RATED_GROUPED,
    EVENT,
    SWEEP;

    public static final RewardType[] VALUES = values();

}
