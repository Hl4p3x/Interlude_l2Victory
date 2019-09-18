package npc.model;

import ru.j2dev.gameserver.listener.zone.OnZoneEnterLeaveListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.EventTrigger;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ReflectionUtils;

public final class OddGlobeInstance extends NpcInstance {
    private static final int instancedZoneId = 151;

    public OddGlobeInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if ("monastery_enter".equalsIgnoreCase(command)) {
            final Reflection r = player.getActiveReflection();
            if (r != null) {
                if (player.canReenterInstance(151)) {
                    player.teleToLocation(r.getTeleportLoc(), r);
                }
            } else if (player.canEnterInstance(151)) {
                final Reflection newfew = ReflectionUtils.enterReflection(player, 151);
                final ZoneListener zoneL = new ZoneListener();
                newfew.getZone("[ssq_holy_burial_ground]").addListener(zoneL);
                final ZoneListener2 zoneL2 = new ZoneListener2();
                newfew.getZone("[ssq_holy_seal]").addListener(zoneL2);
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public class ZoneListener implements OnZoneEnterLeaveListener {
        private boolean done;

        public ZoneListener() {
            done = false;
        }

        @Override
        public void onZoneEnter(final Zone zone, final Creature cha) {
            final Player player = cha.getPlayer();
            if (player == null || !cha.isPlayer() || done) {
                return;
            }
            done = true;
            player.showQuestMovie(24);
        }

        @Override
        public void onZoneLeave(final Zone zone, final Creature cha) {
        }
    }

    public class ZoneListener2 implements OnZoneEnterLeaveListener {
        private boolean done;

        public ZoneListener2() {
            done = false;
        }

        @Override
        public void onZoneEnter(final Zone zone, final Creature cha) {
            final Player player = cha.getPlayer();
            if (player == null || !cha.isPlayer()) {
                return;
            }
            player.broadcastPacket(new EventTrigger(21100100, true));
            if (!done) {
                done = true;
                player.showQuestMovie(26);
            }
        }

        @Override
        public void onZoneLeave(final Zone zone, final Creature cha) {
        }
    }
}
