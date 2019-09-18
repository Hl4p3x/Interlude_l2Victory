package ru.j2dev.gameserver.model.npcmaker;

import ru.j2dev.gameserver.utils.Location;

public class RespawnData {
    public final String dbname;
    public final long respawnTime;
    public final int currentHp;
    public final int currentMp;
    public final Location position;

    public RespawnData(String name, long respawn, int hp, int mp, int x, int y, int z) {
        dbname = name;
        respawnTime = respawn;
        currentHp = hp;
        currentMp = mp;
        position = new Location(x, y, z);
    }
}
