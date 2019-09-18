import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.TeleportLocation;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExPledgeCrestLarge;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.scripts.Scripts;
import ru.j2dev.gameserver.utils.CapchaUtil;
import ru.j2dev.gameserver.utils.Location;

import java.util.Arrays;


public class Util extends Functions {
    private static final int BaseCapchaCID = 0x4000000;


    public static void RequestCapcha(final String onSuccess, final int store_id, final int time) {
        final Player player = GameObjectsStorage.getPlayer(store_id);
        if (player == null || !player.isOnline() || player.isLogoutStarted()) {
            return;
        }
        int serverId = Config.REQUEST_ID;
        if (player.isConnected() && player.getNetConnection() != null) {
            serverId = player.getNetConnection().getServerId();
        }
        final int capcha = CapchaUtil.RndCapcha();
        final int bgcolor = CapchaUtil.RndRGB888Color();
        final int cid = CapchaUtil.getId(capcha) | 0x4000000;
        final byte[] img = CapchaUtil.getCapchaImage(capcha, bgcolor);
        player.sendPacket(new ExPledgeCrestLarge(cid, img));
        NpcHtmlMessage html = new NpcHtmlMessage(player, null);
        html.setFile("capcha.htm");
        html = html.replace("%SN%", String.valueOf(serverId));
        html = html.replace("%CID%", String.valueOf(cid));
        player.setVar("capacha-code", String.valueOf(capcha), -1L);
        player.setVar("capacha-time", String.valueOf(System.currentTimeMillis() / 1000L + time), -1L);
        player.setVar("capacha-success", onSuccess, -1L);
        player.sendPacket(html);
        player.sendMessage(new CustomMessage("scripts.Util.CapchaConfirm.RequestCapcha", player, time));
    }

    public void Gatekeeper(final String[] param) {
        if (param.length < 4) {
            throw new IllegalArgumentException();
        }
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        final long price = Long.parseLong(param[param.length - 1]);
        if (!NpcInstance.canBypassCheck(player, player.getLastNpc())) {
            return;
        }
        if (price > 0L && player.getAdena() < price) {
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
        }
        if (player.getMountType() == 2) {
            show("scripts/wyvern-no.htm", player);
            return;
        }
        if (player.getLastNpc() != null) {
            final int npcId = player.getLastNpc().getNpcId();
            switch (npcId) {
                case 30483: {
                    if (player.getLevel() > Config.CRUMA_GATEKEEPER_LVL) {
                        show("teleporter/30483-no.htm", player);
                        return;
                    }
                    break;
                }
                case 32864:
                case 32865:
                case 32866:
                case 32867:
                case 32868:
                case 32869:
                case 32870: {
                    if (player.getLevel() < 80) {
                        show("teleporter/" + npcId + "-no.htm", player);
                        return;
                    }
                    break;
                }
            }
        }
        final int x = Integer.parseInt(param[0]);
        final int y = Integer.parseInt(param[1]);
        final int z = Integer.parseInt(param[2]);
        final int castleId = (param.length > 4) ? Integer.parseInt(param[3]) : 0;
        if (player.getLastNpc() != null) {
            TeleportLocation teleportLocation;
            final TeleportLocation[][] array = player.getLastNpc().getTemplate().getTeleportList().values().toArray(new TeleportLocation[player.getLastNpc().getTemplate().getTeleportList().size()][]);
            teleportLocation = Arrays.stream(array).flatMap(Arrays::stream).filter(tl -> tl.getX() == x && tl.getY() == y && tl.getZ() == z).findFirst().orElse(null);
            if (getTeleportErrorMsg(player, teleportLocation)) {
                return;
            }
        }
        if (player.getReflection().isDefault()) {
            final Castle castle = (castleId > 0) ? ResidenceHolder.getInstance().getResidence(Castle.class, castleId) : null;
            if (castle != null && castle.getSiegeEvent().isInProgress()) {
                player.sendPacket(Msg.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
                return;
            }
        }
        final Location pos = Location.findPointToStay(x, y, z, 50, 100, player.getGeoIndex());
        if (price > 0L) {
            player.reduceAdena(price, true);
        }
        player.teleToLocation(pos);
    }

    public void SSGatekeeper(final String[] param) {
        if (param.length < 4) {
            throw new IllegalArgumentException();
        }
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        final int type = Integer.parseInt(param[3]);
        if (!NpcInstance.canBypassCheck(player, player.getLastNpc())) {
            return;
        }
        if (Config.ALT_ENABLE_SEVEN_SING_TELEPORTER_PROTECTION && type > 0) {
            final int player_cabal = SevenSigns.getInstance().getPlayerCabal(player);
            final int period = SevenSigns.getInstance().getCurrentPeriod();
            if (period == 1 && player_cabal == 0) {
                player.sendPacket(Msg.USED_ONLY_DURING_A_QUEST_EVENT_PERIOD);
                return;
            }
            final int winner;
            if (period == 3 && (winner = SevenSigns.getInstance().getCabalHighestScore()) != 0) {
                if (winner != player_cabal) {
                    return;
                }
                if (type == 1 && SevenSigns.getInstance().getSealOwner(1) != player_cabal) {
                    return;
                }
                if (type == 2 && SevenSigns.getInstance().getSealOwner(2) != player_cabal) {
                    return;
                }
            }
        }
        player.teleToLocation(Integer.parseInt(param[0]), Integer.parseInt(param[1]), Integer.parseInt(param[2]));
    }

    public void QuestGatekeeper(final String[] param) {
        if (param.length < 5) {
            throw new IllegalArgumentException();
        }
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        final long count = Long.parseLong(param[3]);
        final int item = Integer.parseInt(param[4]);
        if (!NpcInstance.canBypassCheck(player, player.getLastNpc())) {
            return;
        }
        final int x = Integer.parseInt(param[0]);
        final int y = Integer.parseInt(param[1]);
        final int z = Integer.parseInt(param[2]);
        if (player.getLastNpc() != null) {
            TeleportLocation teleportLocation = null;
            final TeleportLocation[][] teleportLocations = player.getLastNpc().getTemplate().getTeleportList().values().toArray(new TeleportLocation[player.getLastNpc().getTemplate().getTeleportList().size()][]);
            for (final TeleportLocation[] array2 : teleportLocations) {
                teleportLocation = Arrays.stream(array2).filter(tl -> tl.getX() == x && tl.getY() == y && tl.getZ() == z).findFirst().orElse(teleportLocation);
            }
            if (getTeleportErrorMsg(player, teleportLocation)) {
                return;
            }
        }
        if (count > 0L) {
            if (!player.getInventory().destroyItemByItemId(item, count)) {
                player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                return;
            }
            player.sendPacket(SystemMessage2.removeItems(item, count));
        }
        Location pos;
        if (Config.ALT_SPREADING_AFTER_TELEPORT) {
            pos = Location.findPointToStay(x, y, z, 20, 70, player.getGeoIndex());
        } else {
            pos = new Location(x, y, z).correctGeoZ();
        }
        player.teleToLocation(pos);
    }

    private boolean getTeleportErrorMsg(Player player, TeleportLocation teleportLocation) {
        if (teleportLocation != null) {
            if (teleportLocation.getMinLevel() > 0 && player.getLevel() < teleportLocation.getMinLevel()) {
                player.sendMessage(new CustomMessage("Gatekeeper.LevelToLow", player, teleportLocation.getMinLevel()));
                return true;
            }
            if (teleportLocation.getMaxLevel() > 0 && player.getLevel() > teleportLocation.getMaxLevel()) {
                player.sendMessage(new CustomMessage("Gatekeeper.LevelToHigh", player, teleportLocation.getMaxLevel()));
                return true;
            }
        }
        return false;
    }

    public void ReflectionGatekeeper(final String[] param) {
        if (param.length < 5) {
            throw new IllegalArgumentException();
        }
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        player.setReflection(Integer.parseInt(param[4]));
        Gatekeeper(param);
    }

    public void TokenJump(final String[] param) {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (player.getLevel() <= 19) {
            QuestGatekeeper(param);
        } else {
            show("Only for newbies", player);
        }
    }

    public void NoblessTeleport() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (player.isNoble() || Config.ALLOW_NOBLE_TP_TO_ALL) {
            show("scripts/noble.htm", player);
        } else {
            show("scripts/nobleteleporter-no.htm", player);
        }
    }

    public void PayPage(final String[] param) {
        if (param.length < 2) {
            throw new IllegalArgumentException();
        }
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        final String page = param[0];
        final int item = Integer.parseInt(param[1]);
        final long price = Long.parseLong(param[2]);
        if (getItemCount(player, item) < price) {
            player.sendPacket((item == 57) ? Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA : SystemMsg.INCORRECT_ITEM_COUNT);
            return;
        }
        removeItem(player, item, price);
        show(page, player);
    }

    public void MakeEchoCrystal(final String[] param) {
        if (param.length < 2) {
            throw new IllegalArgumentException();
        }
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!NpcInstance.canBypassCheck(player, player.getLastNpc())) {
            return;
        }
        final int crystal = Integer.parseInt(param[0]);
        final int score = Integer.parseInt(param[1]);
        if (crystal < 4411 || crystal > 4417) {
            return;
        }
        if (getItemCount(player, score) == 0L) {
            player.getLastNpc().onBypassFeedback(player, "Chat 1");
            return;
        }
        if (getItemCount(player, 57) < 200L) {
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
        }
        removeItem(player, 57, 200L);
        addItem(player, crystal, 1L);
    }

    public void TakeNewbieWeaponCoupon() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.ALT_ALLOW_SHADOW_WEAPONS) {
            show(new CustomMessage("common.Disabled", player), player);
            return;
        }
        if (player.getLevel() > 19 || player.getClassId().getLevel() > 1) {
            show("Your level is too high!", player);
            return;
        }
        if (player.getLevel() < 6) {
            show("Your level is too low!", player);
            return;
        }
        if (player.getVarB("newbieweapon")) {
            show("Your already got your newbie weapon!", player);
            return;
        }
        addItem(player, 7832, 5L);
        player.setVar("newbieweapon", "true", -1L);
    }

    public void TakeAdventurersArmorCoupon() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.ALT_ALLOW_SHADOW_WEAPONS) {
            show(new CustomMessage("common.Disabled", player), player);
            return;
        }
        if (player.getLevel() > 39 || player.getClassId().getLevel() > 2) {
            show("Your level is too high!", player);
            return;
        }
        if (player.getLevel() < 20 || player.getClassId().getLevel() < 2) {
            show("Your level is too low!", player);
            return;
        }
        if (player.getVarB("newbiearmor")) {
            show("Your already got your newbie weapon!", player);
            return;
        }
        addItem(player, 7833, 1L);
        player.setVar("newbiearmor", "true", -1L);
    }

    public void enter_dc() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (!NpcInstance.canBypassCheck(player, npc)) {
            return;
        }
        player.setVar("DCBackCoords", player.getLoc().toXYZString(), -1L);
        player.teleToLocation(-114582, -152635, -6742);
    }

    public void exit_dc() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (!NpcInstance.canBypassCheck(player, npc)) {
            return;
        }
        final String var = player.getVar("DCBackCoords");
        if (var == null || var.isEmpty()) {
            player.teleToLocation(new Location(43768, -48232, -800), 0);
            return;
        }
        player.teleToLocation(Location.parseLoc(var), 0);
        player.unsetVar("DCBackCoords");
    }

    public void CapchaConfirm(final String[] param) {
        if (param.length < 1) {
            throw new IllegalArgumentException();
        }
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        final String scapcha = player.getVar("capacha-code");
        final String sendtime = player.getVar("capacha-time");
        final String ssuccess = player.getVar("capacha-success");
        if (scapcha == null || sendtime == null || ssuccess == null) {
            return;
        }
        try {
            final int capcha = Integer.parseInt(scapcha);
            final long endtime = Long.parseLong(sendtime);
            final String code = param[0];
            if (endtime < System.currentTimeMillis() / 1000L) {
                player.sendMessage(new CustomMessage("scripts.Util.CapchaConfirm.TimeExpired", player));
                return;
            }
            if (!CapchaUtil.IsValidEntry(capcha, code)) {
                player.sendMessage(new CustomMessage("scripts.Util.CapchaConfirm.WrongCode", player));
                return;
            }
            Scripts.getInstance().callScripts(player, ssuccess.split(":")[0], ssuccess.split(":")[1]);
            player.sendMessage(new CustomMessage("scripts.Util.CapchaConfirm.Success", player));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
