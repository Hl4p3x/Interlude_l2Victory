package services;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.handler.npcdialog.INpcDialogAppender;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SetupGauge;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.Collections;
import java.util.List;

public class RideHire extends Functions implements INpcDialogAppender {
    private static boolean canBeStarted() {
        for (final Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class)) {
            if (c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getAppend(Player player, NpcInstance npc, int val) {
        if (Config.SERVICES_ALLOW_WYVERN_RIDE) {
            return player.isLangRus() ? "<br>[scripts_services.RideHire:ride_prices|\u0412\u0437\u044f\u0442\u044c \u043d\u0430 \u043f\u0440\u043e\u043a\u0430\u0442 \u0435\u0437\u0434\u043e\u0432\u043e\u0435 \u0436\u0438\u0432\u043e\u0442\u043d\u043e\u0435.]" : "<br>[scripts_services.RideHire:ride_prices|Ride hire mountable pet.]";
        }
        return "";
    }

    public void ride_prices() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        show("scripts/services/ride-prices.htm", player, npc);
    }

    public void ride(final String[] args) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        final boolean ru = player.isLangRus();
        if (args.length != 3) {
            show(ru ? "\u041d\u0435\u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u044b\u0435 \u0434\u0430\u043d\u043d\u044b\u0435" : "Incorrect input", player, npc);
            return;
        }
        if (!NpcInstance.canBypassCheck(player, npc)) {
            return;
        }
        if (player.getActiveWeaponFlagAttachment() != null) {
            player.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
            return;
        }
        if (player.getTransformation() != 0) {
            show(ru ? "\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0432\u0437\u044f\u0442\u044c \u043f\u0435\u0442\u0430 \u0432 \u043f\u0440\u043e\u043a\u0430\u0442, \u043f\u043e\u043a\u0430 \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0432 \u0440\u0435\u0436\u0438\u043c\u0435 \u0442\u0440\u0430\u043d\u0441\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u0438." : "Can't ride while in transformation mode.", player, npc);
            return;
        }
        if (player.getPet() != null || player.isMounted()) {
            player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
            return;
        }
        int npc_id;
        switch (Integer.parseInt(args[0])) {
            case 1: {
                npc_id = 12621;
                break;
            }
            case 2: {
                npc_id = 12526;
                break;
            }
            default: {
                show(ru ? "\u0423 \u043c\u0435\u043d\u044f \u043d\u0435\u0442 \u0442\u0430\u043a\u0438\u0445 \u043f\u0438\u0442\u043e\u043c\u0446\u0435\u0432!" : "Unknown pet.", player, npc);
                return;
            }
        }
        if ((npc_id == 12621 || npc_id == 12526) && !canBeStarted()) {
            show(ru ? "\u041f\u0440\u043e\u043a\u0430\u0442 \u0432\u0438\u0432\u0435\u0440\u043d/\u0441\u0442\u0440\u0430\u0439\u0434\u0435\u0440\u043e\u0432 \u043d\u0435 \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0432\u043e \u0432\u0440\u0435\u043c\u044f \u043e\u0441\u0430\u0434\u044b." : "Can't ride wyvern/strider while Siege in progress.", player, npc);
            return;
        }
        final Integer time = Integer.parseInt(args[1]);
        final Long price = Long.parseLong(args[2]);
        if (time > 3600) {
            show(ru ? "\u0421\u043b\u0438\u0448\u043a\u043e\u043c \u0431\u043e\u043b\u044c\u0448\u043e\u0435 \u0432\u0440\u0435\u043c\u044f." : "Too long time to ride.", player, npc);
            return;
        }
        if (ItemFunctions.getItemCount(player, Config.SERVICES_WYVERN_ITEM_ID) < price) {
            if (Config.SERVICES_WYVERN_ITEM_ID == 57) {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            } else {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            }
            return;
        }
        ItemFunctions.removeItem(player, Config.SERVICES_WYVERN_ITEM_ID, price, true);
        doLimitedRide(player, npc_id, time);
    }

    public void doLimitedRide(final Player player, final Integer npc_id, final Integer time) {
        if (!ride(player, npc_id)) {
            return;
        }
        player.sendPacket(new SetupGauge(player, 3, time * 1000));
        executeTask(player, "services.RideHire", "rideOver", new Object[0], (long) (time * 1000));
    }

    public void rideOver() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        unRide(player);
        show(player.isLangRus() ? "\u0412\u0440\u0435\u043c\u044f \u043f\u0440\u043e\u043a\u0430\u0442\u0430 \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u043b\u043e\u0441\u044c. \u041f\u0440\u0438\u0445\u043e\u0434\u0438\u0442\u0435 \u0435\u0449\u0435!" : "Ride time is over.<br><br>Welcome back again!", player);
    }

    @Override
    public List<Integer> getNpcIds() {
        return Collections.singletonList(30827);
    }
}
