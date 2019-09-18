package npc.model;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.NpcUtils;

public final class DragonVortexInstance extends NpcInstance {
    private final int[] bosses = {25718, 25719, 25720, 25721, 25722, 25723, 25724};
    private NpcInstance boss;

    public DragonVortexInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if (command.startsWith("request_boss")) {
            if (boss != null && !boss.isDead()) {
                showChatWindow(player, "default/32871-3.htm");
                return;
            }
            if (ItemFunctions.getItemCount(player, 17248) > 0L) {
                ItemFunctions.removeItem(player, 17248, 1L, true);
                boss = NpcUtils.spawnSingle(bosses[Rnd.get(bosses.length)], Location.coordsRandomize(getLoc(), 300, 600), getReflection());
                showChatWindow(player, "default/32871-1.htm");
            } else {
                showChatWindow(player, "default/32871-2.htm");
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
