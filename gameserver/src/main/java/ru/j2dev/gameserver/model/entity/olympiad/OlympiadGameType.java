package ru.j2dev.gameserver.model.entity.olympiad;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;

import java.util.Arrays;

@HideAccess
@StringEncryption
public enum OlympiadGameType {
    TEAM_CLASS_FREE(0),
    CLASS_FREE(1),
    CLASS_INDIVIDUAL(2);

    private final int _type_idx;

    OlympiadGameType(final int type_idx) {
        _type_idx = type_idx;
    }

    public static OlympiadGameType getTypeOf(final int idx) {
        return Arrays.stream(values()).filter(type -> type.getTypeIdx() == idx).findFirst().orElse(null);
    }

    public int getTypeIdx() {
        return _type_idx;
    }
}
