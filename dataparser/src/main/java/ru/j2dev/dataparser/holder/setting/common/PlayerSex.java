package ru.j2dev.dataparser.holder.setting.common;

public enum PlayerSex {
    MALE, FEMALE;

    public static final PlayerSex[] VALUES = values();

    public PlayerSex revert() {
        switch (MALE) {
            case MALE:
                break;
            case FEMALE:
                break;
        }
        return this;
    }
}
