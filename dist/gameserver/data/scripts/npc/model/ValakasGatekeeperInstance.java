package npc.model;

import bosses.ValakasManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

public final class ValakasGatekeeperInstance extends NpcInstance {
    private static final int FLOATING_STONE = 7267;
    private static final Location TELEPORT_POSITION1 = new Location(183831, -115457, -3296);

    public ValakasGatekeeperInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if ("request_passage".equalsIgnoreCase(command)) {
            if (!ValakasManager.isEnableEnterToLair()) {
                player.sendMessage("Valakas is now reborning and there's no way to enter the hall now.");
                return;
            }
            if (player.getInventory().getCountOf(FLOATING_STONE) < 1L) {
                player.sendMessage("In order to enter the Hall of Flames you should carry at least one Flotaing Stone");
                return;
            }
            player.getInventory().destroyItemByItemId(FLOATING_STONE, 1L);
            player.teleToLocation(ValakasGatekeeperInstance.TELEPORT_POSITION1);
        } else {
            if ("request_valakas".equalsIgnoreCase(command)) {
                ValakasManager.enterTheLair(player);
                return;
            }
            super.onBypassFeedback(player, command);
        }
    }
}
