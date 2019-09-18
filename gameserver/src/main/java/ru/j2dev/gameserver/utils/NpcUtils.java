package ru.j2dev.gameserver.utils;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.taskmanager.tasks.objecttasks.DeleteTask;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class NpcUtils {

    public static NpcInstance spawnSingle(final int npcId, final int x, final int y, final int z, final Creature killer, final int i0, final int i1, final int i2) {
        return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, 0L, null, killer, i0, i1, i2);
    }

    public static NpcInstance spawnSingle(final int npcId, final int x, final int y, final int z) {
        return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, 0L, null, null, 0, 0, 0);
    }

    public static NpcInstance spawnSingle(final int npcId, final int x, final int y, final int z, final long despawnTime) {
        return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, despawnTime, null, null, 0, 0, 0);
    }

    public static NpcInstance spawnSingle(final int npcId, final int x, final int y, final int z, final int h, final long despawnTime) {
        return spawnSingle(npcId, new Location(x, y, z, h), ReflectionManager.DEFAULT, despawnTime, null, null, 0, 0, 0);
    }

    public static NpcInstance spawnSingle(final int npcId, final Location loc) {
        return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, 0L, null, null, 0, 0, 0);
    }

    public static NpcInstance spawnSingle(final int npcId, final Location loc, final long despawnTime) {
        return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, despawnTime, null, null, 0, 0, 0);
    }

    public static NpcInstance spawnSingle(final int npcId, final Location loc, final Reflection reflection) {
        return spawnSingle(npcId, loc, reflection, 0L, null, null, 0, 0, 0);
    }

    public static NpcInstance spawnSingle(final int npcId, final Location loc, final Reflection reflection, final long despawnTime) {
        return spawnSingle(npcId, loc, reflection, despawnTime, null, null, 0, 0, 0);
    }

    public static NpcInstance spawnSingle(final int npcId, final Location loc, final Reflection reflection, final long despawnTime, final String title, final Creature killer, int i0, int i1, int i2) {
        final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(npcId);
        if (template == null) {
            throw new NullPointerException("Npc template id : " + npcId + " not found!");
        }
        final NpcInstance npc = template.getNewInstance();
        npc.setHeading((loc.h < 0) ? Rnd.get(65535) : loc.h);
        npc.setSpawnedLoc(loc);
        npc.setReflection(reflection);
        npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
        if (title != null) {
            npc.setTitle(title);
        }
        npc.setParam1(i0);
        npc.setParam2(i1);
        npc.setParam3(i2);
        if (killer != null) {
            npc.setParam4(killer);
        }
        npc.spawnMe(npc.getSpawnedLoc());
        if (despawnTime > 0L) {
            ThreadPoolManager.getInstance().schedule(new DeleteTask(npc), despawnTime);
        }
        return npc;
    }

    public static NpcInstance createOnePrivateEx(final int npcId, final int x, final int y, final int z, final int h, final int i0, final int i1, final int i2) {
        return createOnePrivateEx(npcId, new Location(x, y, z, h), ReflectionManager.DEFAULT, 0, null, null, i0, i1, i2);
    }

    public static NpcInstance createOnePrivateEx(final int npcId, final int x, final int y, final int z, final int i0, final int i1) {
        return createOnePrivateEx(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, 0, null, null, 0, i0, i1);
    }

    public static NpcInstance createOnePrivateEx(final int npcId, final Location loc, final Reflection reflection, final Creature arg, final int i0, final int i1) {
        return createOnePrivateEx(npcId, loc, reflection, 0, null, arg, 0, i0, i1);
    }

    public static NpcInstance createOnePrivateEx(final int npcId, final int x, final int y, final int z, final Creature arg, final int i0, final int i1) {
        return createOnePrivateEx(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, 0, null, arg, 0, i0, i1);
    }

    public static NpcInstance createOnePrivateEx(final int npcId, final Location loc, final Reflection reflection, final long despawnTime, final String title, final Creature arg, final int i0, final int i1, final int i2) {
        final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(npcId);
        if (template == null) {
            throw new NullPointerException("Npc template id : " + npcId + " not found!");
        }
        final NpcInstance npc = template.getNewInstance();
        npc.setHeading(loc.h < 0 ? Rnd.get(0xFFFF) : loc.h);
        npc.setSpawnedLoc(loc);
        npc.setReflection(reflection);
        npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
        if (title != null) {
            npc.setTitle(title);
        }
        npc.spawnMe(npc.getSpawnedLoc());
        npc.setParam1(i0);
        npc.setParam2(i1);
        npc.setParam3(i2);
        if (arg != null) {
            npc.setParam4(arg);
        }
        if (despawnTime > 0) {
            ThreadPoolManager.getInstance().schedule(new DeleteTask(npc), despawnTime);
        }
        return npc;
    }
}
