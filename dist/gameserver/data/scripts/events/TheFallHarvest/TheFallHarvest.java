package events.TheFallHarvest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.MultiSellHolder;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerEnterListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;

import java.io.File;

public class TheFallHarvest extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger((Class) TheFallHarvest.class);
    private static final String FALL_HARVEST_SPAWN = "[the_fall_harvest_spawn]";
    private static final File multiSellFile = new File(Config.DATAPACK_ROOT, "data/html/en/scripts/events/TheFallHarvest/31255.xml");
    private static boolean _active;
    private static boolean MultiSellLoaded;

    private static boolean isActive() {
        return IsActive("TheFallHarvest");
    }

    private static void loadMultiSell() {
        if (MultiSellLoaded) {
            return;
        }
        MultiSellHolder.getInstance().parseFile(multiSellFile);
        MultiSellLoaded = true;
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal(new ListenersImpl());
        if (isActive()) {
            _active = true;
            loadMultiSell();
            spawnEventManagers();
            LOGGER.info("Loaded Event: The Fall Harvest [state: activated]");
        } else {
            LOGGER.info("Loaded Event: The Fall Harvest [state: deactivated]");
        }
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("TheFallHarvest", true)) {
            loadMultiSell();
            spawnEventManagers();
            System.out.println("Event 'The Fall Harvest' started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.TheFallHarvest.AnnounceEventStarted", null);
        } else {
            player.sendMessage("Event 'The Fall Harvest' already started.");
        }
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("TheFallHarvest", false)) {
            unSpawnEventManagers();
            System.out.println("Event 'The Fall Harvest' stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.TheFallHarvest.AnnounceEventStoped", null);
        } else {
            player.sendMessage("Event 'The Fall Harvest' not started.");
        }
        _active = false;
        show("admin/events/events.htm", player);
    }

    private void spawnEventManagers() {
        SpawnManager.getInstance().spawn(FALL_HARVEST_SPAWN);
    }

    private void unSpawnEventManagers() {
        SpawnManager.getInstance().despawn(FALL_HARVEST_SPAWN);
    }

    private class ListenersImpl implements OnDeathListener, OnPlayerEnterListener {

        @Override
        public void onDeath(final Creature cha, final Creature killer) {
            if (_active && SimpleCheckDrop(cha, killer) && Rnd.chance(Config.EVENT_TFH_POLLEN_CHANCE * killer.getPlayer().getRateItems() * ((NpcInstance) cha).getTemplate().rateHp)) {
                ((NpcInstance) cha).dropItem(killer.getPlayer(), 6391, 1L);
            }
        }

        @Override
        public void onPlayerEnter(final Player player) {
            if (_active) {
                Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.TheFallHarvest.AnnounceEventStarted", null);
            }
        }
    }

}
