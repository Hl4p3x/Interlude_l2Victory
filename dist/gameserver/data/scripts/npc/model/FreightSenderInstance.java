package npc.model;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.MerchantInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PackageToList;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.WarehouseFunctions;

public class FreightSenderInstance extends MerchantInstance {
    public FreightSenderInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if ("deposit_items".equalsIgnoreCase(command)) {
            player.sendPacket(new PackageToList(player));
        } else if ("withdraw_items".equalsIgnoreCase(command)) {
            WarehouseFunctions.showFreightWindow(player);
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
