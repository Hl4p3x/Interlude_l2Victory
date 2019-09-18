package services;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;

public class TeleToMDT extends Functions {
    public void toMDT() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (!NpcInstance.canBypassCheck(player, npc)) {
            return;
        }
        player.setVar("backCoords", player.getLoc().toXYZString(), -1L);
        player.teleToLocation(12661, 181687, -3560);
    }

    public void fromMDT() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (!NpcInstance.canBypassCheck(player, npc)) {
            return;
        }
        final String var = player.getVar("backCoords");
        if (var == null || "".equals(var)) {
            teleOut();
            return;
        }
        player.teleToLocation(Location.parseLoc(var));
    }

    public void teleOut() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        player.teleToLocation(12902, 181011, -3563);
        show(player.isLangRus() ? "\u042f \u043d\u0435 \u0437\u043d\u0430\u044e, \u043a\u0430\u043a \u0412\u044b \u043f\u043e\u043f\u0430\u043b\u0438 \u0441\u044e\u0434\u0430, \u043d\u043e \u044f \u043c\u043e\u0433\u0443 \u0412\u0430\u0441 \u043e\u0442\u043f\u0440\u0430\u0432\u0438\u0442\u044c \u0437\u0430 \u043e\u0433\u0440\u0430\u0436\u0434\u0435\u043d\u0438\u0435." : "I don't know from where you came here, but I can teleport you the another border side.", player, npc);
    }
}
