package events.cubics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.handler.bypass.Bypass;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;

/**
 * Created by JunkyFunky
 * on 12.06.2018 15:15
 * group j2dev
 */
public class Cubics implements OnInitScriptListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cubics.class);
    private static boolean state;

    @Bypass("cubics:teleportAround")
    public void teleportAround(Player player, NpcInstance lastNpc, String[] args) {
        teleportCreaturesToAroundCoords(player.getLoc(), player.getAroundCharacters(2000, 1000));
    }


    private void teleportCreaturesToAroundCoords(final Location coreLoc, final List<Creature> creatureList) {
        final int size = creatureList.size();
        final int[] x = new int[size];
        final int[] y = new int[size];
        final int radius = 500;
        for(int i = 0; i < size; i++) {
            x[i] = (int) (coreLoc.getX() + radius * Math.cos(2 * Math.PI * i / size));
            y[i] = (int) (coreLoc.getY() + radius * Math.sin(2 * Math.PI * i / size));
        }
        int j = 0;
        for(Creature creature : creatureList) {
            creature.teleToLocation(x[j], y[j], coreLoc.getZ());
            j++;
        }
    }

    @Override
    public void onInit() {
        LOGGER.info("Loading Cubics Event : state ["+state+"]");
    }
}
