package ru.j2dev.gameserver.model.event;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;

/**
 * Список с типами ивентов
 */
@HideAccess
@StringEncryption
public enum PvpEventType {
    LastHero(1, "Last Hero", "Последний Герой", -10),
    TeamVsTeam(2, "Team vs Team", "Команда против Команды", -11),
    CaptureTheFlag(3, "Capture The Flag", "Захват флага", -12),
    KoreanStyle(4, "Korean Style", "Корейский стиль", -13);
    //TownSiege(5, "Town Siege", "Осада Города", -14);

    private final int _id;
    private final int _refId;
    private final String _nameEng;
    private final String _nameRus;

    PvpEventType(final int id, final String nameEn, final String nameRus, int reflectionId) {
        _id = id;
        _nameEng = nameEn;
        _nameRus = nameRus;
        _refId = reflectionId;
    }

    public int getRefId() {
        return _refId;
    }

    /**
     * @return _id
     */
    public int getId() {
        return _id;
    }

    /**
     * @return _nameEn
     */
    public String getName(final boolean rus) {
        return rus ? _nameRus : _nameEng;
    }

    @Override
    public String toString() {
        return _nameEng;
    }
}
