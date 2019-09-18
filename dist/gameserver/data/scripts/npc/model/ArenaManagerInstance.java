package npc.model;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.WarehouseInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class ArenaManagerInstance extends WarehouseInstance {
    private static final int RECOVER_CP_SKILLID = 4380;

    public ArenaManagerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if (!player.isInPeaceZone() || player.isCursedWeaponEquipped()) {
            return;
        }
        if (command.startsWith("CPRecovery")) {
            if (Functions.getItemCount(player, 57) >= 100L) {
                Functions.removeItem(player, 57, 100L);
                doCast(SkillTable.getInstance().getInfo(4380, 1), player, true);
            } else {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
