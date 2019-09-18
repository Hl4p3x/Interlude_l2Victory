package ru.j2dev.gameserver.model.event;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;

import java.util.concurrent.atomic.AtomicInteger;

@HideAccess
@StringEncryption
public class PvpEventPlayerInfo {
    private final int storeId;
    private final AtomicInteger _kills;
    private final AtomicInteger _deaths;

    public PvpEventPlayerInfo(final int storedId) {
        storeId = storedId;
        _kills = new AtomicInteger(0);
        _deaths = new AtomicInteger(0);
    }

    public PvpEventPlayerInfo(final Player player) {
        this(player.getObjectId());
    }

    public int incrementKills() {
        return _kills.incrementAndGet();
    }

    public int incrementDeaths() {
        return _deaths.incrementAndGet();
    }

    public int getKillsCount() {
        return _kills.get();
    }

    public int getDeathsCount() {
        return _deaths.get();
    }

    public final Player getPlayer() {
        return GameObjectsStorage.getPlayer(storeId);
    }
}
