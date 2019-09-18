package ru.j2dev.gameserver.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.manager.MapRegionManager;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.RestartType;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.templates.mapregion.RestartArea;
import ru.j2dev.gameserver.templates.mapregion.RestartPoint;

public class TeleportUtils {
    public static final Location DEFAULT_RESTART = new Location(17817, 170079, -3530);
    private static final Logger LOGGER = LoggerFactory.getLogger(TeleportUtils.class);

    public static Location getRestartLocation(final Player player, final RestartType restartType) {
        return getRestartLocation(player, player.getLoc(), restartType);
    }

    public static Location getRestartLocation(final Player player, final Location from, final RestartType restartType) {
        final Reflection r = player.getReflection();
        if (r != ReflectionManager.DEFAULT) {
            if (r.getCoreLoc() != null) {
                return r.getCoreLoc();
            }
            if (r.getReturnLoc() != null) {
                return r.getReturnLoc();
            }
        }
        final Clan clan = player.getClan();
        if (clan != null) {
            if (restartType == RestartType.TO_CLANHALL && clan.getHasHideout() != 0) {
                return ResidenceHolder.getInstance().getResidence(clan.getHasHideout()).getOwnerRestartPoint();
            }
            if (restartType == RestartType.TO_CASTLE && clan.getCastle() != 0) {
                return ResidenceHolder.getInstance().getResidence(clan.getCastle()).getOwnerRestartPoint();
            }
        }
        if (player.getKarma() > 1) {
            if (player.getPKRestartPoint() != null) {
                return player.getPKRestartPoint();
            }
        } else if (player.getRestartPoint() != null) {
            return player.getRestartPoint();
        }
        final RestartArea ra = MapRegionManager.getInstance().getRegionData(RestartArea.class, from);
        if (ra != null) {
            final RestartPoint rp = ra.getRestartPoint().get(player.getRace());
            final Location restartPoint = Rnd.get(rp.getRestartPoints());
            final Location PKrestartPoint = Rnd.get(rp.getPKrestartPoints());
            return (player.getKarma() > 1) ? PKrestartPoint : restartPoint;
        }
        LOGGER.warn("Cannot find restart location from coordinates: " + from + "!");
        return DEFAULT_RESTART;
    }
}
