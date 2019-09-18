package ru.j2dev.gameserver.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.GameTimeController;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.SpawnHolder;
import ru.j2dev.gameserver.listener.game.OnDayNightChangeListener;
import ru.j2dev.gameserver.listener.game.OnSSPeriodListener;
import ru.j2dev.gameserver.model.HardSpawner;
import ru.j2dev.gameserver.model.Spawner;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.templates.spawn.PeriodOfDay;
import ru.j2dev.gameserver.templates.spawn.SpawnTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnManager {
    public static final String SPAWN_EVENT_NAME_AVARICE_DUSK = "[ssq_seal1_twilight]";
    public static final String SPAWN_EVENT_NAME_AVARICE_DAWN = "[ssq_seal1_dawn]";
    public static final String SPAWN_EVENT_NAME_GNOSIS_NONE = "[ssq_seal2_none]";
    public static final String SPAWN_EVENT_NAME_GNOSIS_DUSK = "[ssq_seal2_twilight]";
    public static final String SPAWN_EVENT_NAME_GNOSIS_DAWN = "[ssq_seal2_dawn]";
    public static final String DAWN_GROUP = "dawn_spawn";
    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnManager.class);
    private static final String SPAWN_EVENT_NAME_SSQ_EVENT = "[ssq_event]";
    private static final String SPAWN_EVENT_NAME_AVARICE_NONE = "[ssq_seal1_none]";
    private static final String DUSK_GROUP = "dusk_spawn";

    private final Map<String, List<Spawner>> _spawns = new ConcurrentHashMap<>();
    private final Listeners _listeners = new Listeners();

    private SpawnManager() {
        SpawnHolder.getInstance().getSpawns().forEach(this::fillSpawn);
        GameTimeController.getInstance().addListener(_listeners);
        SevenSigns.getInstance().addListener(_listeners);
    }

    public static SpawnManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public List<Spawner> fillSpawn(final String group, final List<SpawnTemplate> templateList) {
        if (Config.DONTLOADSPAWN) {
            return Collections.emptyList();
        }
        List<Spawner> spawnerList = _spawns.computeIfAbsent(group, k -> new ArrayList<>(templateList.size()));
        for (final SpawnTemplate template : templateList) {
            try {
                final HardSpawner spawner = new HardSpawner(template);
                spawnerList.add(spawner);
                final NpcTemplate npcTemplate = NpcTemplateHolder.getInstance().getTemplate(spawner.getCurrentNpcId());
                if (Config.RATE_MOB_SPAWN > 1 && Objects.requireNonNull(npcTemplate).getInstanceClass() == MonsterInstance.class && npcTemplate.level >= Config.RATE_MOB_SPAWN_MIN_LEVEL && npcTemplate.level <= Config.RATE_MOB_SPAWN_MAX_LEVEL) {
                    spawner.setAmount(template.getCount() * Config.RATE_MOB_SPAWN);
                } else {
                    spawner.setAmount(template.getCount());
                }
                spawner.setRespawnDelay(template.getRespawn(), template.getRespawnRandom());
                spawner.setRespawnCron(template.getRespawnCron());
                spawner.setReflection(ReflectionManager.DEFAULT);
                spawner.setRespawnTime(0);
                if (!npcTemplate.isRaid || !group.equals(PeriodOfDay.ALL.name())) {
                    continue;
                }
                RaidBossSpawnManager.getInstance().addNewSpawn(npcTemplate.getNpcId(), spawner);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return spawnerList;
    }

    public void spawnAll() {
        spawn(PeriodOfDay.ALL.name());
        if (Config.ALLOW_EVENT_GATEKEEPER) {
            spawn("[event_gate]");
        }
        if (Config.ALLOW_GLOBAL_GK) {
            spawn("[global_gatekeeper]");
        }
        if (Config.ALLOW_BUFFER) {
            spawn("[npc_buffer]");
        }
        if (Config.ALLOW_GMSHOP) {
            spawn("[gm_shop]");
        }
        if (Config.SAVE_ADMIN_SPAWN) {
            spawn("[custom_spawn]");
        }
        if (Config.ALLOW_AUCTIONER) {
            spawn("[auctioner]");
        }
        if (Config.ALLOW_GLOBAL_SERVICES) {
            spawn("[global_services]");
        }
        if (Config.ALLOW_PVP_EVENT_MANAGER) {
            spawn("[pvp_event_manager]");
        }
        if (Config.ALLOW_TREASURE_BOX) {
            spawn("[treasure_box]");
        }
        if (Config.SERVICES_ALLOW_LOTTERY) {
            spawn("[lotto_manager]");
        }
        if (!Config.ALLOW_CLASS_MASTERS_LIST.isEmpty() && Config.ALLOW_CLASS_MASTERS_SPAWN) {
            spawn("class_master");
        }
        spawn("[custom_npc_spawn]");
    }

    public void spawn(final String group) {
        final List<Spawner> spawnerList = getSpawners(group);
        if (spawnerList == null) {
            return;
        }
        int npcSpawnCount = 0;
        for (final Spawner spawner : spawnerList) {
            npcSpawnCount += spawner.init();
            if (npcSpawnCount % 1000 == 0 && npcSpawnCount != 0) {
                LOGGER.info("SpawnManager: spawned " + npcSpawnCount + " npc for group: " + group);
            }
        }
        LOGGER.info("SpawnManager: spawned " + npcSpawnCount + " npc; spawns: " + spawnerList.size() + "; group: " + group);
    }

    public void despawn(final String group) {
        final List<Spawner> spawnerList = _spawns.get(group);
        if (spawnerList == null) {
            return;
        }
        for (final Spawner spawner : spawnerList) {
            spawner.deleteAll();
        }
    }

    public List<Spawner> getSpawners(final String group) {
        final List<Spawner> list = _spawns.get(group);
        return (list == null) ? Collections.emptyList() : list;
    }

    public void reloadAll() {
        RaidBossSpawnManager.getInstance().cleanUp();
        _spawns.values().stream().flatMap(Collection::stream).forEach(Spawner::deleteAll);
        _spawns.clear();
        SpawnHolder.getInstance().getSpawns().forEach(this::fillSpawn);
        RaidBossSpawnManager.getInstance().reloadBosses();
        spawnAll();
        if (SevenSigns.getInstance().getCurrentPeriod() == 3) {
            SevenSigns.getInstance().getCabalHighestScore();
        }
        _listeners.onPeriodChange(SevenSigns.getInstance().getCurrentPeriod());
        if (GameTimeController.getInstance().isNowNight()) {
            _listeners.onNight();
        } else {
            _listeners.onDay();
        }
    }

    private static class LazyHolder {
        private static final SpawnManager INSTANCE = new SpawnManager();
    }

    private class Listeners implements OnDayNightChangeListener, OnSSPeriodListener {
        @Override
        public void onDay() {
            despawn(PeriodOfDay.NIGHT.name());
            spawn(PeriodOfDay.DAY.name());
        }

        @Override
        public void onNight() {
            despawn(PeriodOfDay.DAY.name());
            spawn(PeriodOfDay.NIGHT.name());
        }

        @Override
        public void onPeriodChange(final int mode) {
            despawn(SPAWN_EVENT_NAME_SSQ_EVENT);
            despawn(SPAWN_EVENT_NAME_AVARICE_NONE);
            despawn(SPAWN_EVENT_NAME_AVARICE_DUSK);
            despawn(SPAWN_EVENT_NAME_AVARICE_DAWN);
            despawn(SPAWN_EVENT_NAME_GNOSIS_NONE);
            despawn(SPAWN_EVENT_NAME_GNOSIS_DUSK);
            despawn(SPAWN_EVENT_NAME_GNOSIS_DAWN);
            switch (SevenSigns.getInstance().getCurrentPeriod()) {
                case 1: {
                    spawn(SPAWN_EVENT_NAME_SSQ_EVENT);
                    break;
                }
                case 3: {
                    switch (SevenSigns.getInstance().getSealOwner(1)) {
                        case 0: {
                            spawn(SPAWN_EVENT_NAME_AVARICE_NONE);
                            break;
                        }
                        case 1: {
                            spawn(SPAWN_EVENT_NAME_AVARICE_DUSK);
                            break;
                        }
                        case 2: {
                            spawn(SPAWN_EVENT_NAME_AVARICE_DAWN);
                            break;
                        }
                    }
                    switch (SevenSigns.getInstance().getSealOwner(2)) {
                        case 0: {
                            spawn(SPAWN_EVENT_NAME_GNOSIS_NONE);
                            break;
                        }
                        case 1: {
                            spawn(SPAWN_EVENT_NAME_GNOSIS_DUSK);
                            break;
                        }
                        case 2: {
                            spawn(SPAWN_EVENT_NAME_GNOSIS_DAWN);
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }
}
