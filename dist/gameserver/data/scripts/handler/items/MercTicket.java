package handler.items;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ru.j2dev.gameserver.dao.CastleHiredGuardDAO;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.pledge.Privilege;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;
import ru.j2dev.gameserver.templates.item.support.MerchantGuard;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;
import ru.j2dev.gameserver.utils.PositionUtils;

import java.util.Collection;

public class MercTicket extends ScriptItemHandler {
    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        return false;
    }

    @Override
    public void dropItem(final Player player, ItemInstance item, final long count, final Location loc) {
        if (!player.hasPrivilege(Privilege.CS_FS_MERCENARIES) || player.getClan().getCastle() == 0) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_POSITION_MERCENARIES, ActionFail.STATIC);
            return;
        }
        final Castle castle = player.getCastle();
        final MerchantGuard guard = castle.getMerchantGuard(item.getItemId());
        if (guard == null || !castle.checkIfInZone(loc, ReflectionManager.DEFAULT) || player.isActionBlocked("drop_merchant_guard")) {
            player.sendPacket(SystemMsg.YOU_CANNOT_POSITION_MERCENARIES_HERE, ActionFail.STATIC);
            return;
        }
        if (castle.getSiegeEvent().isInProgress() || !guard.isValidSSQPeriod()) {
            player.sendPacket(SystemMsg.A_MERCENARY_CAN_BE_ASSIGNED_TO_A_POSITION_FROM_THE_BEGINNING_OF_THE_SEAL_VALIDATION_PERIOD_UNTIL_THE_TIME_WHEN_A_SIEGE_STARTS, ActionFail.STATIC);
            return;
        }
        int countOfGuard = 0;
        for (final ItemInstance $item : castle.getSpawnMerchantTickets()) {
            if (PositionUtils.getDistance($item.getLoc(), loc) < 200.0) {
                player.sendPacket(SystemMsg.POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT, ActionFail.STATIC);
                return;
            }
            if ($item.getItemId() != guard.getItemId()) {
                continue;
            }
            countOfGuard++;
        }
        if (countOfGuard >= guard.getMax()) {
            player.sendPacket(SystemMsg.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE, ActionFail.STATIC);
            return;
        }
        item = player.getInventory().removeItemByObjectId(item.getObjectId(), 1L);
        if (item == null) {
            player.sendActionFailed();
            return;
        }
        Log.LogItem(player, ItemLog.Drop, item);
        item.dropToTheGround(player, loc);
        player.disableDrop(1000);
        player.sendChanges();
        item.delete();
        castle.getSpawnMerchantTickets().add(item);
        CastleHiredGuardDAO.getInstance().insert(castle, item.getItemId(), item.getLoc());
    }

    @Override
    public boolean pickupItem(final Playable playable, final ItemInstance item) {
        if (!playable.isPlayer()) {
            return false;
        }
        final Player player = (Player) playable;
        if (!player.hasPrivilege(Privilege.CS_FS_MERCENARIES) || player.getClan().getCastle() == 0) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING);
            return false;
        }
        final Castle castle = player.getCastle();
        if (!castle.getSpawnMerchantTickets().contains(item)) {
            player.sendPacket(SystemMsg.THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_YOU_CANNOT_CANCEL_ITS_POSITIONING);
            return false;
        }
        if (castle.getSiegeEvent().isInProgress()) {
            player.sendPacket(SystemMsg.A_MERCENARY_CAN_BE_ASSIGNED_TO_A_POSITION_FROM_THE_BEGINNING_OF_THE_SEAL_VALIDATION_PERIOD_UNTIL_THE_TIME_WHEN_A_SIEGE_STARTS, ActionFail.STATIC);
            return false;
        }
        castle.getSpawnMerchantTickets().remove(item);
        CastleHiredGuardDAO.getInstance().delete(castle, item);
        return true;
    }

    @Override
    public final int[] getItemIds() {
        final TIntSet set = new TIntHashSet(100);
        final Collection<Castle> castles = ResidenceHolder.getInstance().getResidenceList(Castle.class);
        castles.stream().map(c -> c.getMerchantGuards().keySet()).forEach(set::addAll);
        return set.toArray();
    }
}
