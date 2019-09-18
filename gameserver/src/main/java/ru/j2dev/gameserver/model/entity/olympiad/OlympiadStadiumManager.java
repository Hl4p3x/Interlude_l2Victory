package ru.j2dev.gameserver.model.entity.olympiad;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayDeque;
import java.util.stream.IntStream;

@HideAccess
@StringEncryption
public class OlympiadStadiumManager {
    private static final int REFLECTION_COUNT = 22;
    private static final Logger LOGGER = LoggerFactory.getLogger(OlympiadStadiumManager.class);
    private static final StadiumTemplate[] OLY_STADIA_TEMPLATES = {new StadiumTemplate(147, new Location(-20814, -21189, -3030))};

    private final ArrayDeque<OlympiadStadium> _freeOlympiadStadiums;
    private final OlympiadStadium[] _allOlympiadStadiums;

    private OlympiadStadiumManager() {
        _allOlympiadStadiums = new OlympiadStadium[REFLECTION_COUNT];
        _freeOlympiadStadiums = new ArrayDeque<>();
    }

    public static OlympiadStadiumManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public OlympiadStadium[] getAllStadiums() {
        return _allOlympiadStadiums;
    }

    public void AllocateStadiums() {
        int cnt = 0;
        for (int i = 0; i < REFLECTION_COUNT / OLY_STADIA_TEMPLATES.length; ++i) {
            for (final StadiumTemplate st : OLY_STADIA_TEMPLATES) {
                final OlympiadStadium olympiadStadium = new OlympiadStadium(cnt, st.zid, st.oloc);
                _allOlympiadStadiums[cnt] = olympiadStadium;
                _freeOlympiadStadiums.addLast(olympiadStadium);
                ++cnt;
            }
        }
        LOGGER.info("OlympiadStadiumManager: allocated " + cnt + " stadiums.");
    }

    public void FreeStadiums() {
        IntStream.range(0, _allOlympiadStadiums.length).filter(i -> _allOlympiadStadiums[i] != null).forEach(i -> {
            _allOlympiadStadiums[i].collapse();
            _allOlympiadStadiums[i] = null;
        });
        _freeOlympiadStadiums.clear();
        LOGGER.info("OlympiadStadiumManager: stadiums cleared.");
    }

    public boolean isStadiumAvailable() {
        return _freeOlympiadStadiums.size() > 0;
    }

    public synchronized OlympiadStadium pollStadium() {
        OlympiadStadium olympiadStadium = _freeOlympiadStadiums.pollFirst();
        if (!olympiadStadium.isFree()) {
            LOGGER.warn("OlympiadStadiumManager: Poll used stadium");
            Thread.dumpStack();
            olympiadStadium = _freeOlympiadStadiums.pollFirst();
        }
        olympiadStadium.setFree(false);
        return olympiadStadium;
    }

    public synchronized void putStadium(final OlympiadStadium olympiadStadium) {
        if (olympiadStadium.isFree()) {
            LOGGER.warn("OlympiadStadiumManager: Put free stadium");
            Thread.dumpStack();
        }
        olympiadStadium.clear();
        olympiadStadium.setFree(true);
        _freeOlympiadStadiums.addFirst(olympiadStadium);
    }

    public OlympiadStadium getStadium(final int id) {
        return _allOlympiadStadiums[id];
    }

    private static class LazyHolder {
        private static final OlympiadStadiumManager INSTANCE = new OlympiadStadiumManager();
    }

    private static class StadiumTemplate {
        public final Location oloc;
        public final int zid;
        public Location[] plocs;
        public Location[] blocs;

        public StadiumTemplate(final int _zid, final Location ol) {
            oloc = ol;
            zid = _zid;
        }
    }
}
