package ru.j2dev.gameserver.model.entity.olympiad;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;

@HideAccess
@StringEncryption
public enum OlympiadGameState {
    INIT(0),
    STAND_BY(1),
    PLAYING(2),
    FINISH(0);

    private final int _state_id;

    OlympiadGameState(final int state_id) {
        _state_id = state_id;
    }

    public int getStateId() {
        return _state_id;
    }
}
