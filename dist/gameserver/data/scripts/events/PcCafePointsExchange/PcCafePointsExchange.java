package events.PcCafePointsExchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.scripts.Functions;

public class PcCafePointsExchange extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PcCafePointsExchange.class);
    private static final String EVENT_NAME = "PcCafePointsExchange";
    private static final String EVENT_PC_CAFE_EXCHANGE_SPAWN = "[pc_cafe_exchange_spawn]";

    private static boolean isActive() {
        return IsActive("PcCafePointsExchange");
    }

    private void spawnEventManagers() {
        SpawnManager.getInstance().spawn("[pc_cafe_exchange_spawn]");
    }

    private void unSpawnEventManagers() {
        SpawnManager.getInstance().despawn("[pc_cafe_exchange_spawn]");
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("PcCafePointsExchange", true)) {
            spawnEventManagers();
            System.out.println("Event: 'PcCafePointsExchange' started.");
        } else {
            player.sendMessage("Event 'PcCafePointsExchange' already started.");
        }
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("PcCafePointsExchange", false)) {
            unSpawnEventManagers();
            System.out.println("Event: 'PcCafePointsExchange' stopped.");
        } else {
            player.sendMessage("Event: 'PcCafePointsExchange' not started.");
        }
        show("admin/events/events.htm", player);
    }

    @Override
    public void onInit() {
        if (isActive()) {
            spawnEventManagers();
            LOGGER.info("Loaded Event: PcCafePointsExchange [state: activated]");
        } else {
            LOGGER.info("Loaded Event: PcCafePointsExchange [state: deactivated]");
        }
    }

}
