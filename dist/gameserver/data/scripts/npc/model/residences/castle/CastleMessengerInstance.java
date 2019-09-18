package npc.model.residences.castle;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CastleSiegeInfo;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class CastleMessengerInstance extends NpcInstance {
    public CastleMessengerInstance(final int objectID, final NpcTemplate template) {
        super(objectID, template);
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        final Castle castle = getCastle();
        if (player.isCastleLord(castle.getId())) {
            if (castle.getSiegeEvent().isInProgress()) {
                showChatWindow(player, "residence2/castle/sir_tyron021.htm");
            } else {
                showChatWindow(player, "residence2/castle/sir_tyron007.htm");
            }
        } else if (castle.getSiegeEvent().isInProgress()) {
            showChatWindow(player, "residence2/castle/sir_tyron021.htm");
        } else {
            player.sendPacket(new CastleSiegeInfo(castle, player));
        }
    }

    @Override
    public boolean canInteractWithKarmaPlayer() {
        return true;
    }
}
