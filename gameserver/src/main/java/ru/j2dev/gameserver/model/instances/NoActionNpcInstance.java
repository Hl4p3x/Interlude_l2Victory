package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

@Deprecated
public class NoActionNpcInstance extends NpcInstance {
    public NoActionNpcInstance(final int objectID, final NpcTemplate template) {
        super(objectID, template);
    }

    @Override
    public void onAction(final Player player, final boolean dontMove) {
        player.sendActionFailed();
    }
}
