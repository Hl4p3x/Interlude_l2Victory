package events.Kamaloka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.scripts.Functions;

public class kamaloka extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger((Class) kamaloka.class);
    private static final String EVENT_KAMALOKA_SPAWN = "[event_kamaloka_spawn_list]";
    private static boolean _active;

    private static boolean isActive() {
        return IsActive("kamaloka");
    }

    private void spawnEventManagers() {
        SpawnManager.getInstance().spawn(EVENT_KAMALOKA_SPAWN);
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("kamaloka", true)) {
            spawnEventManagers();
            System.out.println("Event 'Kamaloka' started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.Kamaloka.AnnounceEventStarted", null);
        } else {
            player.sendMessage("Event 'Kamaloka' already started.");
        }
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("kamaloka", false)) {
            unSpawnEventManagers();
            System.out.println("Event 'Kamaloka' stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.Kamaloka.AnnounceEventStoped", null);
        } else {
            player.sendMessage("Event 'Kamaloka' not started.");
        }
        _active = false;
        show("admin/events/events.htm", player);
    }

    private void unSpawnEventManagers() {
        SpawnManager.getInstance().despawn(EVENT_KAMALOKA_SPAWN);
    }

    @Override
    public void onInit() {
        if (isActive()) {
            _active = true;
            spawnEventManagers();
            LOGGER.info("Loaded Event: Kamaloka [state: activated]");
        } else {
            LOGGER.info("Loaded Event: Kamaloka [state: deactivated]");
        }
    }
}
