package services;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.ReflectionUtils;

public class PaganDoormans extends Functions {
    private static final int MainDoorId = 19160001;
    private static final int SecondDoor1Id = 19160011;
    private static final int SecondDoor2Id = 19160010;
    private static final int q_mark_of_sacrifice = 8064;
    private static final int q_faded_mark_of_sac = 8065;
    private static final int q_mark_of_heresy = 8067;

    private static void openDoor(final int doorId) {
        final DoorInstance door = ReflectionUtils.getDoor(doorId);
        door.openMe();
    }

    public void openMainDoor() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (!NpcInstance.canBypassCheck(player, npc)) {
            return;
        }
        final long items = getItemCount(player, 8064);
        if (items == 0L && getItemCount(player, 8067) == 0L) {
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            return;
        }
        if (items > 0L) {
            removeItem(player, 8064, items);
            addItem(player, 8065, 1L);
        }
        openDoor(19160001);
        show("default/32034-1.htm", player, npc);
    }

    public void openSecondDoor() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (!NpcInstance.canBypassCheck(player, npc)) {
            return;
        }
        if (getItemCount(player, 8067) == 0L) {
            show("default/32036-2.htm", player, npc);
            return;
        }
        openDoor(19160011);
        openDoor(19160010);
        show("default/32036-1.htm", player, npc);
    }

    public void pressSkull() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (!NpcInstance.canBypassCheck(player, npc)) {
            return;
        }
        openDoor(19160001);
        show("default/32035-1.htm", player, npc);
    }

    public void press2ndSkull() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (!NpcInstance.canBypassCheck(player, npc)) {
            return;
        }
        openDoor(19160011);
        openDoor(19160010);
        show("default/32037-1.htm", player, npc);
    }
}
