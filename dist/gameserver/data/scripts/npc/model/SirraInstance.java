package npc.model;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExChangeClientEffectInfo;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

public class SirraInstance extends NpcInstance {
    private static final int[] questInstances = {140, 138, 141};
    private static final int[] warInstances = {139, 144};

    public SirraInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String htmlpath;
        if (ArrayUtils.contains(SirraInstance.questInstances, getReflection().getInstancedZoneId())) {
            htmlpath = "default/32762.htm";
        } else if (ArrayUtils.contains(SirraInstance.warInstances, getReflection().getInstancedZoneId())) {
            final DoorInstance door = getReflection().getDoor(23140101);
            if (door.isOpen()) {
                htmlpath = "default/32762_opened.htm";
            } else {
                htmlpath = "default/32762_closed.htm";
            }
        } else {
            htmlpath = "default/32762.htm";
        }
        return htmlpath;
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if ("teleport_in".equalsIgnoreCase(command)) {
            for (final NpcInstance n : getReflection().getNpcs()) {
                if (n.getNpcId() == 29179 || n.getNpcId() == 29180) {
                    player.sendPacket(new ExChangeClientEffectInfo(2));
                }
            }
            player.teleToLocation(new Location(114712, -113544, -11225));
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
