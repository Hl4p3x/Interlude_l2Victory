package npc.model;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class TriolsMirrorInstance extends NpcInstance {
    public TriolsMirrorInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        if (getNpcId() == 32040) {
            player.teleToLocation(-12766, -35840, -10856);
        } else if (getNpcId() == 32039) {
            player.teleToLocation(35079, -49758, -760);
        }
    }
}
