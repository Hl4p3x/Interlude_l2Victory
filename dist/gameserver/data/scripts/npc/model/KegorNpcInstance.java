package npc.model;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class KegorNpcInstance extends NpcInstance {
    public KegorNpcInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String htmlpath;
        if (getReflection().isDefault()) {
            htmlpath = "default/32761-default.htm";
        } else {
            htmlpath = "default/32761.htm";
        }
        return htmlpath;
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if ("request_stone".equalsIgnoreCase(command)) {
            if (player.getInventory().getCountOf(15469) == 0L && player.getInventory().getCountOf(15470) == 0L) {
                Functions.addItem(player, 15469, 1L);
            } else {
                player.sendMessage("You can't take more than 1 Frozen Core.");
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
