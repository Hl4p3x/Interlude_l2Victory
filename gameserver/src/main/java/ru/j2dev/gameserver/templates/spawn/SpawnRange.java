package ru.j2dev.gameserver.templates.spawn;

import ru.j2dev.gameserver.utils.Location;

public interface SpawnRange {
    Location getRandomLoc(final int p0);
}
