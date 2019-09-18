package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.tables.FishTable;
import ru.j2dev.gameserver.templates.FishTemplate;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PositionUtils;

import java.util.ArrayList;
import java.util.List;

public class FishingSkill extends Skill {
    public FishingSkill(final StatsSet set) {
        super(set);
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        final Player player = (Player) activeChar;
        if (player.getSkillLevel(1315) == -1) {
            return false;
        }
        if (player.isFishing()) {
            player.stopFishing();
            player.sendPacket(Msg.CANCELS_FISHING);
            return false;
        }
        if (player.isInBoat()) {
            activeChar.sendPacket(Msg.YOU_CANT_FISH_WHILE_YOU_ARE_ON_BOARD);
            return false;
        }
        if (player.getPrivateStoreType() != 0) {
            activeChar.sendPacket(Msg.YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_MANUFACTURE_OR_PRIVATE_STORE);
            return false;
        }
        final Zone fishingZone = player.getZone(ZoneType.FISHING);
        if (fishingZone == null) {
            player.sendPacket(Msg.YOU_CANT_FISH_HERE);
            return false;
        }
        if (player.isInWater()) {
            player.sendPacket(Msg.YOU_CANT_FISH_HERE);
            return false;
        }
        final WeaponTemplate weaponItem = player.getActiveWeaponItem();
        if (weaponItem == null || weaponItem.getItemType() != WeaponType.ROD) {
            player.sendPacket(Msg.FISHING_POLES_ARE_NOT_INSTALLED);
            return false;
        }
        final ItemInstance lure = player.getInventory().getPaperdollItem(8);
        if (lure == null || lure.getCount() < 1L) {
            player.sendPacket(Msg.BAITS_ARE_NOT_PUT_ON_A_HOOK);
            return false;
        }
        final int rnd = Rnd.get(50) + 150;
        final double angle = PositionUtils.convertHeadingToDegree(player.getHeading());
        final double radian = Math.toRadians(angle - 90.0);
        final double sin = Math.sin(radian);
        final double cos = Math.cos(radian);
        final int x1 = -(int) (sin * rnd);
        final int y1 = (int) (cos * rnd);
        final int x2 = player.getX() + x1;
        final int y2 = player.getY() + y1;
        int z = GeoEngine.getHeight(x2, y2, player.getZ(), player.getGeoIndex()) + 1;
        boolean isInWater = fishingZone.getParams().getInteger("fishing_place_type") == 2;
        final List<Zone> zones = new ArrayList<>();
        World.getZones(zones, new Location(x2, y2, z), player.getReflection());
        for (final Zone zone : zones) {
            if (zone.getType() == ZoneType.water) {
                z = zone.getTerritory().getZmax();
                isInWater = true;
                break;
            }
        }
        zones.clear();
        if (!isInWater) {
            player.sendPacket(Msg.YOU_CANT_FISH_HERE);
            return false;
        }
        player.getFishing().setFishLoc(new Location(x2, y2, z));
        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature caster, final List<Creature> targets) {
        if (caster == null || !caster.isPlayer()) {
            return;
        }
        final Player player = (Player) caster;
        final ItemInstance lure = player.getInventory().getPaperdollItem(8);
        if (lure == null || lure.getCount() < 1L) {
            player.sendPacket(Msg.BAITS_ARE_NOT_PUT_ON_A_HOOK);
            return;
        }
        final Zone zone = player.getZone(ZoneType.FISHING);
        if (zone == null) {
            return;
        }
        final int distributionId = zone.getParams().getInteger("distribution_id");
        final int lureId = lure.getItemId();
        final int fishLvl = Fishing.getRandomFishLvl(player);
        final int group = Fishing.getFishGroup(lureId);
        final int type = Fishing.getRandomFishType(lureId, fishLvl, distributionId);
        final List<FishTemplate> fishs = FishTable.getInstance().getFish(group, type, fishLvl);
        if (fishs == null || fishs.size() == 0) {
            player.sendPacket(Msg.SYSTEM_ERROR);
            return;
        }
        if (!player.getInventory().destroyItemByObjectId(player.getInventory().getPaperdollObjectId(8), 1L)) {
            player.sendPacket(Msg.NOT_ENOUGH_BAIT);
            return;
        }
        final int check = Rnd.get(fishs.size());
        final FishTemplate fish = fishs.get(check);
        player.startFishing(fish, lureId);
    }
}
