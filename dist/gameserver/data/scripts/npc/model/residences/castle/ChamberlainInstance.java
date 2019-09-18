package npc.model.residences.castle;

import npc.model.residences.ResidenceManager;
import ru.j2dev.gameserver.dao.CastleDamageZoneDAO;
import ru.j2dev.gameserver.dao.CastleDoorUpgradeDAO;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.manager.CastleManorManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.entity.events.objects.CastleDamageZoneObject;
import ru.j2dev.gameserver.model.entity.events.objects.DoorObject;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.pledge.Privilege;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.ReflectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class ChamberlainInstance extends ResidenceManager {
    public ChamberlainInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    private static long modifyPrice(long price) {
        final int SSQ_DawnFactor_door = 80;
        final int SSQ_DrawFactor_door = 100;
        final int SSQ_DuskFactor_door = 300;
        switch (SevenSigns.getInstance().getSealOwner(3)) {
            case 1: {
                price = price * SSQ_DuskFactor_door / 100L;
                break;
            }
            case 2: {
                price = price * SSQ_DawnFactor_door / 100L;
                break;
            }
            default: {
                price = price * SSQ_DrawFactor_door / 100L;
                break;
            }
        }
        return price;
    }

    @Override
    public boolean canInteractWithKarmaPlayer() {
        return true;
    }

    @Override
    protected void setDialogs() {
        _mainDialog = "castle/chamberlain/chamberlain.htm";
        _failDialog = "castle/chamberlain/chamberlain-notlord.htm";
        _siegeDialog = _mainDialog;
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        final int condition = getCond(player);
        if (condition != 2) {
            return;
        }
        final StringTokenizer st = new StringTokenizer(command, " ");
        final String actualCommand = st.nextToken();
        String val = "";
        if (st.countTokens() >= 1) {
            val = st.nextToken();
        }
        final Castle castle = getCastle();
        if ("viewSiegeInfo".equalsIgnoreCase(actualCommand)) {
            if (!isHaveRigths(player, 131072)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            player.sendPacket(new CastleSiegeInfo(castle, player));
        } else if ("ManageTreasure".equalsIgnoreCase(actualCommand)) {
            if (!player.isClanLeader()) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("castle/chamberlain/chamberlain-castlevault.htm");
            html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
            html.replace("%CollectedShops%", String.valueOf(castle.getCollectedShops()));
            html.replace("%CollectedSeed%", String.valueOf(castle.getCollectedSeed()));
            player.sendPacket(html);
        } else if ("TakeTreasure".equalsIgnoreCase(actualCommand)) {
            if (!player.isClanLeader()) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (!"".equals(val)) {
                final long treasure = Long.parseLong(val);
                if (castle.getTreasury() < treasure) {
                    final NpcHtmlMessage html2 = new NpcHtmlMessage(player, this);
                    html2.setFile("castle/chamberlain/chamberlain-havenottreasure.htm");
                    html2.replace("%Treasure%", String.valueOf(castle.getTreasury()));
                    html2.replace("%Requested%", String.valueOf(treasure));
                    player.sendPacket(html2);
                    return;
                }
                if (treasure > 0L) {
                    castle.addToTreasuryNoTax(-treasure, false, false);
                    Log.add(castle.getName() + "|" + -treasure + "|CastleChamberlain", "treasury");
                    player.addAdena(treasure);
                }
            }
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("castle/chamberlain/chamberlain-castlevault.htm");
            html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
            html.replace("%CollectedShops%", String.valueOf(castle.getCollectedShops()));
            html.replace("%CollectedSeed%", String.valueOf(castle.getCollectedSeed()));
            player.sendPacket(html);
        } else if ("PutTreasure".equalsIgnoreCase(actualCommand)) {
            if (!"".equals(val)) {
                final long treasure = Long.parseLong(val);
                if (treasure > player.getAdena()) {
                    player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    return;
                }
                if (treasure > 0L) {
                    castle.addToTreasuryNoTax(treasure, false, false);
                    Log.add(castle.getName() + "|" + treasure + "|CastleChamberlain", "treasury");
                    player.reduceAdena(treasure, true);
                }
            }
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("castle/chamberlain/chamberlain-castlevault.htm");
            html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
            html.replace("%CollectedShops%", String.valueOf(castle.getCollectedShops()));
            html.replace("%CollectedSeed%", String.valueOf(castle.getCollectedSeed()));
            player.sendPacket(html);
        } else if ("manor".equalsIgnoreCase(actualCommand)) {
            if (!isHaveRigths(player, 65536)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            String filename;
            if (CastleManorManager.getInstance().isDisabled()) {
                filename = "npcdefault.htm";
            } else {
                final int cmd = Integer.parseInt(val);
                switch (cmd) {
                    case 0: {
                        filename = "castle/chamberlain/manor/manor.htm";
                        break;
                    }
                    case 4: {
                        filename = "castle/chamberlain/manor/manor_help00" + st.nextToken() + ".htm";
                        break;
                    }
                    default: {
                        filename = "castle/chamberlain/chamberlain-no.htm";
                        break;
                    }
                }
            }
            if (filename.length() > 0) {
                final NpcHtmlMessage html3 = new NpcHtmlMessage(player, this);
                html3.setFile(filename);
                player.sendPacket(html3);
            }
        } else if (actualCommand.startsWith("manor_menu_select")) {
            if (!isHaveRigths(player, 65536)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (CastleManorManager.getInstance().isUnderMaintenance()) {
                player.sendPacket(SystemMsg.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
                player.sendActionFailed();
                return;
            }
            final String params = actualCommand.substring(actualCommand.indexOf("?") + 1);
            final StringTokenizer str = new StringTokenizer(params, "&");
            final int ask = Integer.parseInt(str.nextToken().split("=")[1]);
            final int state = Integer.parseInt(str.nextToken().split("=")[1]);
            final int time = Integer.parseInt(str.nextToken().split("=")[1]);
            int castleId;
            if (state == -1) {
                castleId = castle.getId();
            } else {
                castleId = state;
            }
            switch (ask) {
                case 3: {
                    if (time == 1 && !ResidenceHolder.getInstance().getResidence(Castle.class, castleId).isNextPeriodApproved()) {
                        player.sendPacket(new ExShowSeedInfo(castleId, Collections.emptyList()));
                        break;
                    }
                    player.sendPacket(new ExShowSeedInfo(castleId, ResidenceHolder.getInstance().getResidence(Castle.class, castleId).getSeedProduction(time)));
                    break;
                }
                case 4: {
                    if (time == 1 && !ResidenceHolder.getInstance().getResidence(Castle.class, castleId).isNextPeriodApproved()) {
                        player.sendPacket(new ExShowCropInfo(castleId, Collections.emptyList()));
                        break;
                    }
                    player.sendPacket(new ExShowCropInfo(castleId, ResidenceHolder.getInstance().getResidence(Castle.class, castleId).getCropProcure(time)));
                    break;
                }
                case 5: {
                    player.sendPacket(new ExShowManorDefaultInfo());
                    break;
                }
                case 7: {
                    if (castle.isNextPeriodApproved()) {
                        player.sendPacket(SystemMsg.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_430_AM_AND_8_PM);
                        break;
                    }
                    player.sendPacket(new ExShowSeedSetting(castle.getId()));
                    break;
                }
                case 8: {
                    if (castle.isNextPeriodApproved()) {
                        player.sendPacket(SystemMsg.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_430_AM_AND_8_PM);
                        break;
                    }
                    player.sendPacket(new ExShowCropSetting(castle.getId()));
                    break;
                }
            }
        } else if ("operate_door".equalsIgnoreCase(actualCommand)) {
            if (!isHaveRigths(player, 32768)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (castle.getSiegeEvent().isInProgress()) {
                showChatWindow(player, "residence2/castle/chamberlain_saius021.htm");
                return;
            }
            if (!"".equals(val)) {
                final boolean open = Integer.parseInt(val) == 1;
                while (st.hasMoreTokens()) {
                    final DoorInstance door = ReflectionUtils.getDoor(Integer.parseInt(st.nextToken()));
                    if (open) {
                        door.openMe(player, true);
                    } else {
                        door.closeMe(player, true);
                    }
                }
            }
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("castle/chamberlain/" + getTemplate().npcId + "-d.htm");
            player.sendPacket(html);
        } else if ("tax_set".equalsIgnoreCase(actualCommand)) {
            if (!isHaveRigths(player, 1048576)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (!"".equals(val)) {
                int maxTax = 15;
                if (SevenSigns.getInstance().getSealOwner(3) == 1) {
                    maxTax = 5;
                } else if (SevenSigns.getInstance().getSealOwner(3) == 2) {
                    maxTax = 25;
                }
                final int tax = Integer.parseInt(val);
                if (tax < 0 || tax > maxTax) {
                    final NpcHtmlMessage html2 = new NpcHtmlMessage(player, this);
                    html2.setFile("castle/chamberlain/chamberlain-hightax.htm");
                    html2.replace("%CurrentTax%", String.valueOf(castle.getTaxPercent()));
                    player.sendPacket(html2);
                    return;
                }
                castle.setTaxPercent(player, tax);
            }
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("castle/chamberlain/chamberlain-settax.htm");
            html.replace("%CurrentTax%", String.valueOf(castle.getTaxPercent()));
            player.sendPacket(html);
        } else if ("upgrade_castle".equalsIgnoreCase(actualCommand)) {
            if (!checkSiegeFunctions(player)) {
                return;
            }
            showChatWindow(player, "castle/chamberlain/chamberlain-upgrades.htm");
        } else if ("reinforce".equalsIgnoreCase(actualCommand)) {
            if (!checkSiegeFunctions(player)) {
                return;
            }
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("castle/chamberlain/doorStrengthen-" + castle.getName() + ".htm");
            player.sendPacket(html);
        } else if ("trap_select".equalsIgnoreCase(actualCommand)) {
            if (!checkSiegeFunctions(player)) {
                return;
            }
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("castle/chamberlain/trap_select-" + castle.getName() + ".htm");
            player.sendPacket(html);
        } else if ("buy_trap".equalsIgnoreCase(actualCommand)) {
            if (!checkSiegeFunctions(player)) {
                return;
            }
            if (castle.getSiegeEvent().getObjects("bought_zones").contains(val)) {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.setFile("castle/chamberlain/trapAlready.htm");
                player.sendPacket(html);
                return;
            }
            final List<CastleDamageZoneObject> objects = castle.getSiegeEvent().getObjects(val);
            long price = 0L;
            for (final CastleDamageZoneObject o : objects) {
                price += o.getPrice();
            }
            price = modifyPrice(price);
            if (player.getClan().getAdenaCount() < price) {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }
            player.getClan().getWarehouse().destroyItemByItemId(57, price);
            castle.getSiegeEvent().addObject("bought_zones", val);
            CastleDamageZoneDAO.getInstance().insert(castle, val);
            final NpcHtmlMessage html4 = new NpcHtmlMessage(player, this);
            html4.setFile("castle/chamberlain/trapSuccess.htm");
            player.sendPacket(html4);
        } else if ("door_manage".equalsIgnoreCase(actualCommand)) {
            if (!isHaveRigths(player, 32768)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (castle.getSiegeEvent().isInProgress()) {
                showChatWindow(player, "residence2/castle/chamberlain_saius021.htm");
                return;
            }
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("castle/chamberlain/doorManage.htm");
            html.replace("%id%", val);
            html.replace("%type%", st.nextToken());
            player.sendPacket(html);
        } else if ("upgrade_door_confirm".equalsIgnoreCase(actualCommand)) {
            if (!isHaveRigths(player, 131072)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            final int id = Integer.parseInt(val);
            final int type = Integer.parseInt(st.nextToken());
            final int level = Integer.parseInt(st.nextToken());
            final long price2 = getDoorCost(type, level);
            final NpcHtmlMessage html5 = new NpcHtmlMessage(player, this);
            html5.setFile("castle/chamberlain/doorConfirm.htm");
            html5.replace("%id%", String.valueOf(id));
            html5.replace("%level%", String.valueOf(level));
            html5.replace("%type%", String.valueOf(type));
            html5.replace("%price%", String.valueOf(price2));
            player.sendPacket(html5);
        } else if ("upgrade_door".equalsIgnoreCase(actualCommand)) {
            if (!checkSiegeFunctions(player)) {
                return;
            }
            final int id = Integer.parseInt(val);
            final int type = Integer.parseInt(st.nextToken());
            final int level = Integer.parseInt(st.nextToken());
            final long price2 = getDoorCost(type, level);
            final List<DoorObject> doorObjects = castle.getSiegeEvent().getObjects("doors");
            DoorObject targetDoorObject = null;
            for (final DoorObject o2 : doorObjects) {
                if (o2.getUId() == id) {
                    targetDoorObject = o2;
                    break;
                }
            }
            final DoorInstance door2 = targetDoorObject.getDoor();
            final int upgradeHp = (door2.getMaxHp() - door2.getUpgradeHp()) * level - door2.getMaxHp();
            if (price2 == 0L || upgradeHp < 0) {
                player.sendMessage(new CustomMessage("common.Error", player));
                return;
            }
            if (door2.getUpgradeHp() >= upgradeHp) {
                final int oldLevel = door2.getUpgradeHp() / (door2.getMaxHp() - door2.getUpgradeHp()) + 1;
                final NpcHtmlMessage html6 = new NpcHtmlMessage(player, this);
                html6.setFile("castle/chamberlain/doorAlready.htm");
                html6.replace("%level%", String.valueOf(oldLevel));
                player.sendPacket(html6);
                return;
            }
            if (player.getClan().getAdenaCount() < price2) {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }
            player.getClan().getWarehouse().destroyItemByItemId(57, price2);
            player.sendMessage("Build reinforced");
            targetDoorObject.setUpgradeValue(castle.getSiegeEvent(), upgradeHp);
            CastleDoorUpgradeDAO.getInstance().insert(door2.getDoorId(), upgradeHp);
        } else if ("report".equalsIgnoreCase(actualCommand)) {
            if (!isHaveRigths(player, 262144)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("castle/chamberlain/chamberlain-report.htm");
            html.replace("%FeudName%", castle.getName());
            html.replace("%CharClan%", player.getClan().getName());
            html.replace("%CharName%", player.getName());
            switch (SevenSigns.getInstance().getCurrentPeriod()) {
                case 1:
                    html.replace("%SSPeriod%", new CustomMessage("ChamberlainInstance.NpcString.COMPETITION", player, new Object[0]).toString());
                    break;
                case 3:
                    html.replace("%SSPeriod%", new CustomMessage("ChamberlainInstance.NpcString.SEAL_VALIDATION", player, new Object[0]).toString());
                    break;
                default:
                    html.replace("%SSPeriod%", new CustomMessage("ChamberlainInstance.NpcString.PREPARATION", player, new Object[0]).toString());
                    break;
            }
            switch (SevenSigns.getInstance().getSealOwner(1)) {
                case 1: {
                    html.replace("%Avarice%", new CustomMessage("SevenSigns.NpcString.DUSK", player, new Object[0]).toString());
                    break;
                }
                case 2: {
                    html.replace("%Avarice%", new CustomMessage("SevenSigns.NpcString.DAWN", player, new Object[0]).toString());
                    break;
                }
                case 0: {
                    html.replace("%Avarice%", new CustomMessage("SevenSigns.NpcString.NO_OWNER", player, new Object[0]).toString());
                    break;
                }
            }
            switch (SevenSigns.getInstance().getSealOwner(2)) {
                case 1: {
                    html.replace("%Revelation%", new CustomMessage("SevenSigns.NpcString.DUSK", player, new Object[0]).toString());
                    break;
                }
                case 2: {
                    html.replace("%Revelation%", new CustomMessage("SevenSigns.NpcString.DAWN", player, new Object[0]).toString());
                    break;
                }
                case 0: {
                    html.replace("%Revelation%", new CustomMessage("SevenSigns.NpcString.NO_OWNER", player, new Object[0]).toString());
                    break;
                }
            }
            switch (SevenSigns.getInstance().getSealOwner(3)) {
                case 1: {
                    html.replace("%Strife%", new CustomMessage("SevenSigns.NpcString.DUSK", player, new Object[0]).toString());
                    break;
                }
                case 2: {
                    html.replace("%Strife%", new CustomMessage("SevenSigns.NpcString.DAWN", player, new Object[0]).toString());
                    break;
                }
                case 0: {
                    html.replace("%Strife%", new CustomMessage("SevenSigns.NpcString.NO_OWNER", player, new Object[0]).toString());
                    break;
                }
            }
            player.sendPacket(html);
        } else if ("Crown".equalsIgnoreCase(actualCommand)) {
            if (!player.isClanLeader()) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (player.getInventory().getItemByItemId(6841) == null) {
                player.getInventory().addItem(ItemFunctions.createItem(6841));
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.setFile("castle/chamberlain/chamberlain-givecrown.htm");
                html.replace("%CharName%", player.getName());
                html.replace("%FeudName%", castle.getName());
                player.sendPacket(html);
            } else {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.setFile("castle/chamberlain/alreadyhavecrown.htm");
                player.sendPacket(html);
            }
        } else if ("manageFunctions".equalsIgnoreCase(actualCommand)) {
            if (!player.hasPrivilege(Privilege.CS_FS_SET_FUNCTIONS)) {
                showChatWindow(player, "residence2/castle/chamberlain_saius063.htm");
            } else {
                showChatWindow(player, "residence2/castle/chamberlain_saius065.htm");
            }
        } else if ("manageSiegeFunctions".equalsIgnoreCase(actualCommand)) {
            if (!player.hasPrivilege(Privilege.CS_FS_SET_FUNCTIONS)) {
                showChatWindow(player, "residence2/castle/chamberlain_saius063.htm");
            } else if (SevenSigns.getInstance().getCurrentPeriod() != 3) {
                showChatWindow(player, "residence2/castle/chamberlain_saius068.htm");
            } else {
                showChatWindow(player, "residence2/castle/chamberlain_saius052.htm");
            }
        } else if ("items".equalsIgnoreCase(actualCommand)) {
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("residence2/castle/chamberlain_saius064.htm");
            html.replace("%npcId%", String.valueOf(getNpcId()));
            player.sendPacket(html);
        } else if ("default".equalsIgnoreCase(actualCommand)) {
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("castle/chamberlain/chamberlain.htm");
            player.sendPacket(html);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    protected int getCond(final Player player) {
        if (player.isGM()) {
            return 2;
        }
        final Residence castle = getCastle();
        if (castle != null && castle.getId() > 0 && player.getClan() != null) {
            if (castle.getSiegeEvent().isInProgress()) {
                return 1;
            }
            if (castle.getOwnerId() == player.getClanId()) {
                if (player.isClanLeader()) {
                    return 2;
                }
                if (isHaveRigths(player, 32768) || isHaveRigths(player, 65536) || isHaveRigths(player, 131072) || isHaveRigths(player, 262144) || isHaveRigths(player, 524288) || isHaveRigths(player, 1048576) || isHaveRigths(player, 2097152) || isHaveRigths(player, 4194304)) {
                    return 2;
                }
            }
        }
        return 0;
    }

    private long getDoorCost(final int type, final int level) {
        int price = 0;

        switch (type) {
            case 1: // Главные ворота
                switch (level) {
                    case 2:
                        price = 3000000;
                        break;
                    case 3:
                        price = 4000000;
                        break;
                    case 5:
                        price = 5000000;
                        break;
                }
                break;
            case 2: // Внутренние ворота
                switch (level) {
                    case 2:
                        price = 750000;
                        break;
                    case 3:
                        price = 900000;
                        break;
                    case 5:
                        price = 1000000;
                        break;
                }
                break;
            case 3: // Стены
                switch (level) {
                    case 2:
                        price = 1600000;
                        break;
                    case 3:
                        price = 1800000;
                        break;
                    case 5:
                        price = 2000000;
                        break;
                }
                break;
        }

        return modifyPrice(price);
    }

    @Override
    protected Residence getResidence() {
        return getCastle();
    }

    @Override
    public L2GameServerPacket decoPacket() {
        return null;
    }

    @Override
    protected int getPrivUseFunctions() {
        return 262144;
    }

    @Override
    protected int getPrivSetFunctions() {
        return 4194304;
    }

    @Override
    protected int getPrivDismiss() {
        return 524288;
    }

    @Override
    protected int getPrivDoors() {
        return 32768;
    }

    private boolean checkSiegeFunctions(final Player player) {
        final Castle castle = getCastle();
        if (!player.hasPrivilege(Privilege.CS_FS_SIEGE_WAR)) {
            player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return false;
        }
        if (castle.getSiegeEvent().isInProgress()) {
            showChatWindow(player, "residence2/castle/chamberlain_saius021.htm");
            return false;
        }
        return true;
    }
}
