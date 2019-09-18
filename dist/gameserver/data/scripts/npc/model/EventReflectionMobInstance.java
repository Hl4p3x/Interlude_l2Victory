package npc.model;

import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class EventReflectionMobInstance extends MonsterInstance {
    public EventReflectionMobInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    protected void onDeath(final Creature killer) {
        super.onDeath(killer);
        if (getReflection() == killer.getReflection() && getReflection() != ReflectionManager.DEFAULT) {
            switch (getNpcId()) {
                case 25657: {
                    final DoorInstance door = getReflection().getDoor(25150002);
                    if (door != null) {
                        door.openMe();
                    }
                    break;
                }
                case 25658: {
                    final DoorInstance door = getReflection().getDoor(25150003);
                    if (door != null) {
                        door.openMe();
                        break;
                    }
                    break;
                }
            }
        }
    }
}
