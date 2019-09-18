package npc.model;

import bosses.BaiumManager;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;

public final class BaiumGatekeeperInstance extends NpcInstance {
    private static final int Baium = 29020;
    private static final int BaiumNpc = 29025;
    private static final int BloodedFabric = 4295;
    private static final Location TELEPORT_POSITION = new Location(113100, 14500, 10077);

    public BaiumGatekeeperInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if (command.startsWith("request_entrance")) {
            if (ItemFunctions.getItemCount(player, BloodedFabric) > 0L) {
                final NpcInstance baiumBoss = GameObjectsStorage.getByNpcId(Baium);
                if (baiumBoss != null) {
                    showChatWindow(player, "default/31862-1.htm");
                    return;
                }
                final NpcInstance baiumNpc = GameObjectsStorage.getByNpcId(BaiumNpc);
                if (baiumNpc == null) {
                    showChatWindow(player, "default/31862-2.htm");
                    return;
                }
                ItemFunctions.removeItem(player, BloodedFabric, 1L, true);
                player.setVar("baiumPermission", "granted", -1L);
                player.teleToLocation(TELEPORT_POSITION);
            } else {
                showChatWindow(player, "default/31862-3.htm");
            }
        } else if (command.startsWith("request_wakeup")) {
            if(!BaiumManager.getZone().checkIfInZone(player)) {
                return;
            }
            if (player.getVar("baiumPermission") == null || !"granted".equalsIgnoreCase(player.getVar("baiumPermission"))) {
                showChatWindow(player, "default/29025-1.htm");
                return;
            }
            if (isBusy()) {
                showChatWindow(player, "default/29025-2.htm");
            }
            setBusy(true);
            //Functions.npcSay(this, "You called my name! Now you gonna die!");
            MakeFString(1000521, player.getName(), "", "", "", "");
            BaiumManager.spawnBaium(this, player);
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
