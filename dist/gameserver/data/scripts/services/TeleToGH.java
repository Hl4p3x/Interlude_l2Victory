package services;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.npcdialog.INpcDialogAppender;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.listener.zone.OnZoneEnterLeaveListener;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PositionUtils;
import ru.j2dev.gameserver.utils.ReflectionUtils;

import java.util.Arrays;
import java.util.List;

public class TeleToGH extends Functions implements OnInitScriptListener, INpcDialogAppender {
    private static final String en = "<br>[scripts_services.TeleToGH:toGH @811;Giran Harbor|\"Move to Giran Harbor (offshore zone) - " + Config.SERVICES_GIRAN_HARBOR_PRICE + " Adena.\"]";
    private static final String ru = "<br>[scripts_services.TeleToGH:toGH @811;Giran Harbor|\"Giran Harbor (\u0442\u043e\u0440\u0433\u043e\u0432\u0430\u044f \u0437\u043e\u043d\u0430 \u0431\u0435\u0437 \u043d\u0430\u043b\u043e\u0433\u043e\u0432) - " + Config.SERVICES_GIRAN_HARBOR_PRICE + " Adena.\"]";
    private static final String en2 = "Altar Gatekeeper:<center><br>[scripts_services.ManaRegen:DoManaRegen|Full MP Regeneration. (1 MP for 5 Adena)]<br>[scripts_services.TeleToGH:fromGH @811;From Giran Harbor|\"Exit the Giran Harbor.\"]<br></center>";
    private static final String ru2 = "Altar Gatekeeper:<center><br>[scripts_services.ManaRegen:DoManaRegen|\u041f\u043e\u043b\u043d\u043e\u0435 \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435 MP. (1 MP \u0437\u0430 5 Adena)]<br>[scripts_services.TeleToGH:fromGH @811;From Giran Harbor|\"\u041f\u043e\u043a\u0438\u043d\u0443\u0442\u044c Giran Harbor.\"]<br></center>";
    private static final Zone _zone = ReflectionUtils.getZone("[giran_harbor_offshore]");
    private static ZoneListener _zoneListener;

    @Override
    public void onInit() {
        if (!Config.SERVICES_GIRAN_HARBOR_ENABLED) {
            return;
        }
        ReflectionManager.GIRAN_HARBOR.setCoreLoc(new Location(47416, 186568, -3480));
        _zoneListener = new ZoneListener();
        _zone.addListener(_zoneListener);
        _zone.setReflection(ReflectionManager.GIRAN_HARBOR);
        _zone.setActive(true);
        Zone zone = ReflectionUtils.getZone("[giran_harbor_peace_alt]");
        zone.setReflection(ReflectionManager.GIRAN_HARBOR);
        zone.setActive(true);
        zone = ReflectionUtils.getZone("[giran_harbor_no_trade]");
        zone.setReflection(ReflectionManager.GIRAN_HARBOR);
        zone.setActive(true);
    }

    public void toGH() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (player.getAdena() < Config.SERVICES_GIRAN_HARBOR_PRICE) {
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
        }
        player.reduceAdena((long) Config.SERVICES_GIRAN_HARBOR_PRICE, true);
        player.setVar("backCoords", player.getLoc().toXYZString(), -1L);
        player.teleToLocation(Location.findPointToStay(_zone.getSpawn(), 30, 200, ReflectionManager.GIRAN_HARBOR.getGeoIndex()), ReflectionManager.GIRAN_HARBOR);
    }

    public void fromGH() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (!NpcInstance.canBypassCheck(player, npc)) {
            return;
        }
        final String var = player.getVar("backCoords");
        if (var == null || "".equals(var)) {
            teleOut();
            return;
        }
        player.teleToLocation(Location.parseLoc(var), 0);
    }

    public void teleOut() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        player.teleToLocation(46776, 185784, -3528, 0);
        show(player.isLangRus() ? "\u042f \u043d\u0435 \u0437\u043d\u0430\u044e, \u043a\u0430\u043a \u0412\u044b \u043f\u043e\u043f\u0430\u043b\u0438 \u0441\u044e\u0434\u0430, \u043d\u043e \u044f \u043c\u043e\u0433\u0443 \u0412\u0430\u0441 \u043e\u0442\u043f\u0440\u0430\u0432\u0438\u0442\u044c \u0437\u0430 \u043e\u0433\u0440\u0430\u0436\u0434\u0435\u043d\u0438\u0435." : "I don't know from where you came here, but I can teleport you the another border side.", player, npc);
    }

    @Override
    public String getAppend(Player player, NpcInstance npc, int val) {
        if (val != 0 || !Config.SERVICES_GIRAN_HARBOR_ENABLED) {
            return "";
        }
        if (player == null) {
            return "";
        }

        if (npc.getNpcId() == 40030) {
            if (player.getReflection() != ReflectionManager.GIRAN_HARBOR) {
                return null;
            }
            return player.isLangRus() ? ru2 : en2;
        } else {
            return player.isLangRus() ? ru : en;
        }
    }

    @Override
    public List<Integer> getNpcIds() {
        return Arrays.asList(30059, 30080, 30177, 30233, 30256, 30320, 30848, 30878, 30899, 31210, 31275, 31320, 31964, 30006, 30134, 30146, 30576, 30540);
    }

    public class ZoneListener implements OnZoneEnterLeaveListener {
        @Override
        public void onZoneEnter(final Zone zone, final Creature cha) {
        }

        @Override
        public void onZoneLeave(final Zone zone, final Creature cha) {
            final Player player = cha.getPlayer();
            if (player != null && Config.SERVICES_GIRAN_HARBOR_ENABLED && player.getReflection() == ReflectionManager.GIRAN_HARBOR && player.isVisible()) {
                final double angle = PositionUtils.convertHeadingToDegree(cha.getHeading());
                final double radian = Math.toRadians(angle - 90.0);
                cha.teleToLocation((int) (cha.getX() + 50.0 * Math.sin(radian)), (int) (cha.getY() - 50.0 * Math.cos(radian)), cha.getZ());
            }
        }
    }
}
