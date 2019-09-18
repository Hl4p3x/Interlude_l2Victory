package ru.j2dev.gameserver.utils;

import ru.j2dev.gameserver.data.xml.holder.InstantZoneHolder;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.CommandChannel;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.templates.InstantZone;

import java.util.*;
import java.util.stream.Collectors;

public class ReflectionUtils {
    public static DoorInstance getDoor(final int id) {
        return ReflectionManager.DEFAULT.getDoor(id);
    }

    public static List<DoorInstance> getDoors(final int... ids) {
        return Arrays.stream(ids).mapToObj(ReflectionUtils::getDoor).filter(Objects::nonNull).collect(Collectors.toCollection(() -> new ArrayList<>(ids.length)));
    }

    public static Zone getZone(final String name) {
        return ReflectionManager.DEFAULT.getZone(name);
    }

    public static Collection<Zone> getZones() {
        return ReflectionManager.DEFAULT.getZones();
    }

    public static List<Zone> getZonesByType(final ZoneType zoneType) {
        final Collection<Zone> zones = ReflectionManager.DEFAULT.getZones();
        if (zones.isEmpty()) {
            return Collections.emptyList();
        }
        return zones.stream().filter(z -> z.getType() == zoneType).collect(Collectors.toCollection(() -> new ArrayList<>(5)));
    }

    public static Reflection enterReflection(final Player invoker, final int instancedZoneId) {
        final InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
        return enterReflection(invoker, new Reflection(), iz);
    }

    public static Reflection enterReflection(final Player invoker, final Reflection r, final int instancedZoneId) {
        final InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
        return enterReflection(invoker, r, iz);
    }

    public static Reflection enterReflection(final Player invoker, final Reflection r, final InstantZone iz) {
        r.init(iz);
        if (r.getReturnLoc() == null) {
            r.setReturnLoc(invoker.getLoc());
        }
        switch (iz.getEntryType()) {
            case SOLO: {
                if (iz.getRemovedItemId() > 0) {
                    ItemFunctions.removeItem(invoker, iz.getRemovedItemId(), iz.getRemovedItemCount(), true);
                }
                if (iz.getGiveItemId() > 0) {
                    ItemFunctions.addItem(invoker, iz.getGiveItemId(), iz.getGiveItemCount(), true);
                }
                if (iz.isDispelBuffs()) {
                    invoker.dispelBuffs();
                }
                if (iz.getSetReuseUponEntry() && iz.getResetReuse().next(System.currentTimeMillis()) > System.currentTimeMillis()) {
                    invoker.setInstanceReuse(iz.getId(), System.currentTimeMillis());
                }
                invoker.setVar("backCoords", invoker.getLoc().toXYZString(), -1L);
                invoker.teleToLocation(iz.getTeleportCoord(), r);
                break;
            }
            case PARTY: {
                final Party party = invoker.getParty();
                party.setReflection(r);
                r.setParty(party);
                party.getPartyMembers().forEach(member -> {
                    if (iz.getRemovedItemId() > 0) {
                        ItemFunctions.removeItem(member, iz.getRemovedItemId(), iz.getRemovedItemCount(), true);
                    }
                    if (iz.getGiveItemId() > 0) {
                        ItemFunctions.addItem(member, iz.getGiveItemId(), iz.getGiveItemCount(), true);
                    }
                    if (iz.isDispelBuffs()) {
                        member.dispelBuffs();
                    }
                    if (iz.getSetReuseUponEntry()) {
                        member.setInstanceReuse(iz.getId(), System.currentTimeMillis());
                    }
                    member.setVar("backCoords", invoker.getLoc().toXYZString(), -1L);
                    member.teleToLocation(iz.getTeleportCoord(), r);
                });
                break;
            }
            case COMMAND_CHANNEL: {
                final Party commparty = invoker.getParty();
                final CommandChannel cc = commparty.getCommandChannel();
                cc.setReflection(r);
                r.setCommandChannel(cc);
                for (final Player member2 : cc) {
                    if (iz.getRemovedItemId() > 0) {
                        ItemFunctions.removeItem(member2, iz.getRemovedItemId(), iz.getRemovedItemCount(), true);
                    }
                    if (iz.getGiveItemId() > 0) {
                        ItemFunctions.addItem(member2, iz.getGiveItemId(), iz.getGiveItemCount(), true);
                    }
                    if (iz.isDispelBuffs()) {
                        member2.dispelBuffs();
                    }
                    if (iz.getSetReuseUponEntry()) {
                        member2.setInstanceReuse(iz.getId(), System.currentTimeMillis());
                    }
                    member2.setVar("backCoords", invoker.getLoc().toXYZString(), -1L);
                    member2.teleToLocation(iz.getTeleportCoord(), r);
                }
                break;
            }
        }
        return r;
    }
}
