package npc.model;

import bosses.AntharasManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public final class HeartOfWardingInstance extends NpcInstance {
    public HeartOfWardingInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if ("enter_lair".equalsIgnoreCase(command)) {
            AntharasManager.enterTheLair(player);
            return;
        }
        super.onBypassFeedback(player, command);
    }
}
