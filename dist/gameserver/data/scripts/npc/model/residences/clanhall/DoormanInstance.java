package npc.model.residences.clanhall;

import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class DoormanInstance extends npc.model.residences.DoormanInstance {
    public DoormanInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public int getOpenPriv() {
        return 1024;
    }

    @Override
    public Residence getResidence() {
        return getClanHall();
    }
}
