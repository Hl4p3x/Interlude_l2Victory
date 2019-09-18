package npc.model.residences.clanhall;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CastleSiegeInfo;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class MessengerInstance extends NpcInstance {
    private final String _siegeDialog;
    private final String _ownerDialog;

    public MessengerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _siegeDialog = template.getAIParams().getString("siege_dialog");
        _ownerDialog = template.getAIParams().getString("owner_dialog");
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        final ClanHall clanHall = getClanHall();
        final ClanHallSiegeEvent siegeEvent = clanHall.getSiegeEvent();
        if (clanHall.getOwner() != null && clanHall.getOwner() == player.getClan()) {
            showChatWindow(player, _ownerDialog);
        } else if (siegeEvent.isInProgress()) {
            showChatWindow(player, _siegeDialog);
        } else {
            player.sendPacket(new CastleSiegeInfo(clanHall, player));
        }
    }
}
