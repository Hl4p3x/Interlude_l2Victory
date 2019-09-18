package ru.j2dev.gameserver.handler.admincommands.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.manager.MapRegionManager;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.network.lineage2.serverpackets.DropItem;
import ru.j2dev.gameserver.templates.mapregion.DomainArea;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

public class AdminZone implements IAdminCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminZone.class);
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (activeChar == null || !activeChar.getPlayerAccess().CanTeleport) {
            return false;
        }
        switch (command) {
            case admin_zone_check: {
                activeChar.sendMessage("Current region: " + activeChar.getCurrentRegion());
                activeChar.sendMessage("Zone list:");
                final List<Zone> zones = new ArrayList<>();
                World.getZones(zones, activeChar.getLoc(), activeChar.getReflection());
                for (final Zone zone : zones) {
                    activeChar.sendMessage(zone.getType() + ", name: " + zone.getName() + ", state: " + (zone.isActive() ? "active" : "not active") + ", inside: " + zone.checkIfInZone(activeChar) + "/" + zone.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ()));
                }
                break;
            }
            case admin_region: {
                activeChar.sendMessage("Current region: " + activeChar.getCurrentRegion());
                activeChar.sendMessage("Objects list:");
                for (final GameObject o : activeChar.getCurrentRegion()) {
                    if (o != null) {
                        activeChar.sendMessage(o.toString());
                    }
                }
                break;
            }
            case admin_vis_count: {
                activeChar.sendMessage("Current region: " + activeChar.getCurrentRegion());
                activeChar.sendMessage("Players count: " + World.getAroundPlayers(activeChar).size());
                break;
            }
            case admin_pos: {
                final String pos = activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ() + ", " + activeChar.getHeading() + " Geo [" + (activeChar.getX() - World.MAP_MIN_X >> 4) + ", " + (activeChar.getY() - World.MAP_MIN_Y >> 4) + "] Ref " + activeChar.getReflectionId();
                activeChar.sendMessage("Pos: " + pos);
                activeChar.sendPacket(new DropItem(0, 0xFFFFFF & Rnd.nextInt(), 57, activeChar.getLoc().clone().setZ(activeChar.getZ() + 64), false, 1));
                break;
            }
            case admin_domain: {
                final DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, activeChar);
                final Castle castle = (domain != null) ? ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId()) : null;
                if (castle != null) {
                    activeChar.sendMessage("Domain: " + castle.getName());
                    break;
                }
                activeChar.sendMessage("Domain: Unknown");
                break;
            }
            case admin_zonecreate: {
                Location loc = activeChar.getLoc();
                System.out.println("<coords loc="+loc.getX()+" "+loc.getY()+" "+String.valueOf(loc.getZ()-150)+" "+String.valueOf(loc.getZ()+150)+" />");
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_zone_check,
        admin_region,
        admin_pos,
        admin_vis_count,
        admin_domain,
        admin_zonecreate
    }
}
