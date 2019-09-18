package npc.model;

import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ReflectionUtils;

public final class EventReflectionManagerInstance extends NpcInstance {
    public EventReflectionManagerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if (command.startsWith("event_instance")) {
            final int val = Integer.parseInt(command.substring(15));
            final Reflection r = player.getActiveReflection();
            if (r != null) {
                if (player.canReenterInstance(val)) {
                    player.teleToLocation(r.getTeleportLoc(), r);
                }
            } else if (player.canEnterInstance(val)) {
                ReflectionUtils.enterReflection(player, val);
            }
        } else if (command.startsWith("escape_event_instance")) {
            if (player.getParty() == null || !player.getParty().isLeader(player)) {
                showChatWindow(player, "not_party_leader.htm");
                return;
            }
            player.getReflection().collapse();
        } else if (command.startsWith("return")) {
            final Reflection r2 = player.getReflection();
            if (r2.getReturnLoc() != null) {
                player.teleToLocation(r2.getReturnLoc(), ReflectionManager.DEFAULT);
            } else {
                player.setReflection(ReflectionManager.DEFAULT);
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        super.showChatWindow(player, val);
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }
        return "events/instances/" + pom + ".htm";
    }
}
