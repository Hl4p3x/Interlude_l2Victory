package npc.model;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.manager.DimensionalRiftManager;
import ru.j2dev.gameserver.manager.DimensionalRiftManager.DimensionalRiftRoom;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.DelusionChamber;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.Map;

public final class DelustionGatekeeperInstance extends NpcInstance {
    public DelustionGatekeeperInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if (command.startsWith("enterDC")) {
            final int izId = Integer.parseInt(command.substring(8));
            final int type = izId - 120;
            final Map<Integer, DimensionalRiftRoom> rooms = DimensionalRiftManager.getInstance().getRooms(type);
            if (rooms == null) {
                player.sendPacket(Msg.SYSTEM_ERROR);
                return;
            }
            final Reflection r = player.getActiveReflection();
            if (r != null) {
                if (player.canReenterInstance(izId)) {
                    player.teleToLocation(r.getTeleportLoc(), r);
                }
            } else if (player.canEnterInstance(izId)) {
                final Party party = player.getParty();
                if (party != null) {
                    new DelusionChamber(party, type, Rnd.get(1, rooms.size() - 1));
                }
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
