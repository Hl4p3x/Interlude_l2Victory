package ru.j2dev.gameserver.model.npcmaker;

import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.instances.NpcInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class RespawnManager {
    private static final Map<Integer, Long> respawn_list = new ConcurrentHashMap<>();
    private static ScheduledFuture<?> mainTask;

    public static void addRespawnNpc(NpcInstance npc, long delay) {
        if (mainTask == null) {
            mainTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new RespawnTask(), 1000, 1000);
        }

        respawn_list.put(npc.getObjectId(), System.currentTimeMillis() + delay);
    }

    public static void cancelRespawn(NpcInstance npc) {
        respawn_list.remove(npc.getObjectId());
    }

    public static boolean contains(NpcInstance npc) {
        return respawn_list.containsKey(npc.getObjectId());
    }

    public static class RespawnTask implements Runnable {
        public void run() {
            try {
                long current = System.currentTimeMillis();
                respawn_list.forEach((key, dl) -> {
                    NpcInstance npc = GameObjectsStorage.getNpc(key);
                    if (npc != null) {
                        SpawnDefine sd = npc.getSpawnDefine();
                        if (dl != null && sd != null) {
                            if (current > dl) {
                                npc.refreshID();
                                sd.spawnNpc(npc, null);
                                respawn_list.remove(key);
                            }
                        } else
                            respawn_list.remove(key);
                    } else {
                        respawn_list.remove(key);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
