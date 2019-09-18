package npc.model.residences.castle;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

public class VenomTeleportCubicInstance extends NpcInstance {
    public static final Location[] LOCS = {new Location(11913, -48851, -1088), new Location(11918, -49447, -1088)};

    public VenomTeleportCubicInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        player.teleToLocation(VenomTeleportCubicInstance.LOCS[Rnd.get(VenomTeleportCubicInstance.LOCS.length)]);
    }

    @Override
    public boolean canInteractWithKarmaPlayer() {
        return true;
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        showChatWindow(player, "residence2/castle/teleport_cube_benom001.htm");
    }
}
