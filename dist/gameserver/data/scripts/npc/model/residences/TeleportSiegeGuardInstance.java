package npc.model.residences;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class TeleportSiegeGuardInstance extends NpcInstance {
    protected static final int COND_ALL_FALSE = 0;
    protected static final int BUSY_BECAUSE_SIEGE_NOT_INPROGRESS = 1;
    protected static final int COND_OWNER = 2;

    public TeleportSiegeGuardInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        player.sendActionFailed();
        String filename;
        final int condition = validateCondition(player);
        if (condition == 2) {
            filename = "castle/teleporter/" + getNpcId() + ".htm";
        } else {
            filename = "castle/teleporter/castleteleporter-no.htm";
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile(filename);
        player.sendPacket(html);
    }

    @Override
    public boolean isAttackable(final Creature attacker) {
        final Player player = attacker.getPlayer();
        if (player == null) {
            return false;
        }
        final SiegeEvent<?, ?> siegeEvent = (SiegeEvent<?, ?>) getEvent(SiegeEvent.class);
        final SiegeEvent<?, ?> siegeEvent2 = (SiegeEvent<?, ?>) attacker.getEvent(SiegeEvent.class);
        final Clan clan = player.getClan();
        return siegeEvent != null && (clan == null || siegeEvent != siegeEvent2 || siegeEvent.getSiegeClan("defenders", clan) == null);
    }

    protected int validateCondition(final Player player) {
        if (getCastle() != null && getCastle().getId() > 0 && player.getClan() != null) {
            if (getCastle().getSiegeEvent().isInProgress()) {
                return 1;
            }
            if (getCastle().getOwnerId() == player.getClanId()) {
                return 2;
            }
        }
        return 0;
    }

    @Override
    public boolean isFearImmune() {
        return true;
    }

    @Override
    public boolean isParalyzeImmune() {
        return true;
    }
}
