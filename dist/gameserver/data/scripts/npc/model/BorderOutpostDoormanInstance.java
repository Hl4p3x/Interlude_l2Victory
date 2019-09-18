package npc.model;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.GuardInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ReflectionUtils;

public class BorderOutpostDoormanInstance extends GuardInstance {
    public BorderOutpostDoormanInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        switch (command) {
            case "openDoor": {
                final DoorInstance door = ReflectionUtils.getDoor(24170001);
                door.openMe();
                break;
            }
            case "closeDoor": {
                final DoorInstance door = ReflectionUtils.getDoor(24170001);
                door.closeMe();
                break;
            }
            default:
                super.onBypassFeedback(player, command);
                break;
        }
    }
}
