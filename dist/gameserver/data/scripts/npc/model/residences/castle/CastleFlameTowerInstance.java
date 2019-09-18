package npc.model.residences.castle;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.entity.events.impl.CastleSiegeEvent;
import ru.j2dev.gameserver.model.entity.events.objects.CastleDamageZoneObject;
import ru.j2dev.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.List;
import java.util.Set;

public class CastleFlameTowerInstance extends SiegeToggleNpcInstance {
    private Set<String> _zoneList;

    public CastleFlameTowerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onDeathImpl(final Creature killer) {
        final CastleSiegeEvent event = getEvent(CastleSiegeEvent.class);
        if (event == null || !event.isInProgress()) {
            return;
        }
        for (final String s : _zoneList) {
            final List<CastleDamageZoneObject> objects = event.getObjects(s);
            for (final CastleDamageZoneObject zone : objects) {
                zone.getZone().setActive(false);
            }
        }
    }

    @Override
    public boolean canInteractWithKarmaPlayer() {
        return true;
    }

    @Override
    public void setZoneList(final Set<String> set) {
        _zoneList = set;
    }
}
