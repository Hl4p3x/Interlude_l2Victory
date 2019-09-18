package handler.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.items.IRefineryHandler;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;

public class Appearance extends ScriptItemHandler implements IRefineryHandler {
    private static final Logger LOG = LoggerFactory.getLogger(Appearance.class);
    private static final int[] HANDLED_ITEMS = {Config.APPEARANCE_APPLY_ITEM_ID, Config.APPEARANCE_CANCEL_ITEM_ID};

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (playable == null || !playable.isPlayer()) {
            return false;
        }
        final Player player = (Player) playable;
        if (item.getItemId() == Config.APPEARANCE_APPLY_ITEM_ID) {
            player.setRefineryHandler(this);
            onInitRefinery(player);
            return true;
        }
        if (item.getItemId() == Config.APPEARANCE_CANCEL_ITEM_ID) {
            player.setRefineryHandler(this);
            onInitRefineryCancel(player);
            return true;
        }
        return false;
    }

    @Override
    public int[] getItemIds() {
        return Appearance.HANDLED_ITEMS;
    }

    private boolean checkPlayer(final Player player) {
        if (player.getLevel() < 46) {
            player.sendMessage("You have to be level 46 or higher in order to appear an item");
            return false;
        }
        if (player.isInStoreMode() || player.isInTrade()) {
            player.sendMessage("You cannot appear items while trading.");
            return false;
        }
        if (player.isDead()) {
            player.sendMessage("You cannot appear items while dead.");
            return false;
        }
        if (player.isParalyzed()) {
            player.sendMessage("You cannot appear items while paralyzed.");
            return false;
        }
        if (player.isFishing()) {
            player.sendMessage("You cannot appear items while fishing.");
            return false;
        }
        if (player.isSitting()) {
            player.sendMessage("You cannot appear items while sitting down.");
            return false;
        }
        if (player.isActionsDisabled()) {
            player.sendActionFailed();
            return false;
        }
        return true;
    }

    @Override
    public void onInitRefinery(final Player player) {
        if (!checkPlayer(player)) {
            return;
        }
        player.sendMessage("Select the first item to make an appearance on it.");
        player.sendPacket(ExShowRefineryInterface.STATIC);
    }

    @Override
    public void onPutTargetItem(final Player player, final ItemInstance targetItem) {
        if (!checkPlayer(player)) {
            player.sendPacket(ExPutItemResultForVariationMake.FAIL_PACKET);
            return;
        }
        if (targetItem.getVisibleItemId() != targetItem.getItemId()) {
            player.sendMessage("Once an item is appeared it cannot be appeared again");
            return;
        }
        if ((!targetItem.isWeapon() && !targetItem.isArmor()) || !targetItem.canBeEnchanted(false)) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            return;
        }
        player.sendMessage("Select the second item to make an appearance from it.");
        player.sendPacket(new ExPutItemResultForVariationMake(targetItem.getObjectId(), true));
    }

    @Override
    public void onPutMineralItem(final Player player, final ItemInstance targetItem, final ItemInstance mineralItem) {
        if (!checkPlayer(player)) {
            player.sendActionFailed();
            return;
        }
        if (targetItem.getVisibleItemId() != targetItem.getItemId()) {
            player.sendMessage("One an item is appeared it cannot be appeared again");
            player.sendActionFailed();
            return;
        }
        if (targetItem == mineralItem || (!targetItem.isWeapon() && !targetItem.isArmor()) || !targetItem.canBeEnchanted(false) || (!mineralItem.isWeapon() && !mineralItem.isArmor()) || !mineralItem.canBeEnchanted(false)) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (targetItem.getTemplate().getBodyPart() != mineralItem.getTemplate().getBodyPart() || targetItem.getTemplate().getItemClass() != mineralItem.getTemplate().getItemClass()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (targetItem.getTemplate().getItemType() != mineralItem.getTemplate().getItemType()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        player.sendPacket(new ExPutIntensiveResultForVariationMake(mineralItem.getObjectId(), mineralItem.getItemId(), Config.APPEARANCE_SUPPORT_ITEM_ID, Config.APPEARANCE_SUPPORT_ITEM_CNT, true), new SystemMessage(1959).addNumber(Config.APPEARANCE_SUPPORT_ITEM_CNT).addItemName(Config.APPEARANCE_SUPPORT_ITEM_ID));
    }

    @Override
    public void onPutGemstoneItem(final Player player, final ItemInstance targetItem, final ItemInstance mineralItem, final ItemInstance gemstoneItem, final long gemstoneItemCnt) {
        if (!checkPlayer(player)) {
            player.sendActionFailed();
            return;
        }
        if (targetItem.getVisibleItemId() != targetItem.getItemId()) {
            player.sendMessage("One an item is appeared it cannot be appeared again");
            player.sendActionFailed();
            return;
        }
        if (targetItem == mineralItem || (!targetItem.isWeapon() && !targetItem.isArmor()) || !targetItem.canBeEnchanted(false) || (!mineralItem.isWeapon() && !mineralItem.isArmor()) || !mineralItem.canBeEnchanted(false)) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (targetItem.getTemplate().getBodyPart() != mineralItem.getTemplate().getBodyPart() || targetItem.getTemplate().getItemClass() != mineralItem.getTemplate().getItemClass()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (targetItem.getTemplate().getItemType() != mineralItem.getTemplate().getItemType()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (gemstoneItem.getItemId() != Config.APPEARANCE_SUPPORT_ITEM_ID) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (player.getInventory().getCountOf(Config.APPEARANCE_SUPPORT_ITEM_ID) < Config.APPEARANCE_SUPPORT_ITEM_CNT) {
            player.sendPacket(Msg.NOT_ENOUGH_MATERIALS);
            player.sendActionFailed();
            return;
        }
        player.sendPacket(new ExPutCommissionResultForVariationMake(gemstoneItem.getObjectId(), Config.APPEARANCE_SUPPORT_ITEM_CNT), Msg.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN);
    }

    @Override
    public void onRequestRefine(final Player player, final ItemInstance targetItem, final ItemInstance mineralItem, final ItemInstance gemstoneItem, final long gemstoneItemCnt) {
        if (!checkPlayer(player)) {
            player.sendActionFailed();
            return;
        }
        if (targetItem.getVisibleItemId() != targetItem.getItemId()) {
            player.sendMessage("One an item is appeared it cannot be appeared again");
            player.sendActionFailed();
            return;
        }
        if (targetItem == mineralItem || (!targetItem.isWeapon() && !targetItem.isArmor()) || !targetItem.canBeEnchanted(false) || (!mineralItem.isWeapon() && !mineralItem.isArmor()) || !mineralItem.canBeEnchanted(false)) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (targetItem.getTemplate().getBodyPart() != mineralItem.getTemplate().getBodyPart() || targetItem.getTemplate().getItemClass() != mineralItem.getTemplate().getItemClass()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (targetItem.getTemplate().getItemType() != mineralItem.getTemplate().getItemType()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (gemstoneItem.getItemId() != Config.APPEARANCE_SUPPORT_ITEM_ID) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (player.getInventory().getCountOf(Config.APPEARANCE_SUPPORT_ITEM_ID) < Config.APPEARANCE_SUPPORT_ITEM_CNT) {
            player.sendPacket(Msg.NOT_ENOUGH_MATERIALS);
            player.sendActionFailed();
            return;
        }
        if (!player.getInventory().destroyItem(gemstoneItem, Config.APPEARANCE_SUPPORT_ITEM_CNT)) {
            return;
        }
        final int secondItemId = mineralItem.getItemId();
        if (mineralItem.isEquipped()) {
            player.getInventory().unEquipItem(mineralItem);
        }
        if (!player.getInventory().destroyItem(mineralItem, 1L)) {
            return;
        }
        if (!player.getInventory().destroyItemByItemId(Config.APPEARANCE_APPLY_ITEM_ID, 1L)) {
            return;
        }
        final boolean equipped;
        if (equipped = targetItem.isEquipped()) {
            player.getInventory().unEquipItem(targetItem);
        }
        targetItem.setVisibleItemId(secondItemId);
        if (equipped) {
            player.getInventory().equipItem(targetItem);
        }
        player.sendPacket(new InventoryUpdate().addModifiedItem(targetItem));
        player.getAllShortCuts().stream().filter(sc -> sc.getId() == targetItem.getObjectId() && sc.getType() == 1).forEach(sc -> player.sendPacket(new ShortCutRegister(player, sc)));
        player.sendChanges();
        player.sendPacket(new ExVariationResult(targetItem.getVariationStat1(), targetItem.getVariationStat2(), 1));
        player.broadcastUserInfo(true);
    }

    @Override
    public void onInitRefineryCancel(final Player player) {
        if (!checkPlayer(player)) {
            return;
        }
        player.sendMessage("Select the item to cancel it appearance.");
        player.sendPacket(ExShowVariationCancelWindow.STATIC);
    }

    @Override
    public void onPutTargetCancelItem(final Player player, final ItemInstance targetCancelItem) {
        if (!checkPlayer(player)) {
            player.sendActionFailed();
            return;
        }
        if (targetCancelItem.getVisibleItemId() == targetCancelItem.getItemId()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (!targetCancelItem.isWeapon() && !targetCancelItem.isArmor()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        player.sendPacket(new ExPutItemResultForVariationCancel(targetCancelItem, Config.APPEARANCE_CANCEL_PRICE, true));
    }

    @Override
    public void onRequestCancelRefine(final Player player, final ItemInstance targetCancelItem) {
        if (!checkPlayer(player)) {
            player.sendActionFailed();
            return;
        }
        if (targetCancelItem.getVisibleItemId() == targetCancelItem.getItemId()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (!targetCancelItem.isWeapon() && !targetCancelItem.isArmor()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            player.sendActionFailed();
            return;
        }
        if (!player.reduceAdena(Config.APPEARANCE_CANCEL_PRICE, true)) {
            player.sendPacket(ExVariationCancelResult.FAIL_PACKET);
            player.sendActionFailed();
            return;
        }
        if (!player.getInventory().destroyItemByItemId(Config.APPEARANCE_CANCEL_ITEM_ID, 1L)) {
            return;
        }
        final boolean equipped;
        if (equipped = targetCancelItem.isEquipped()) {
            player.getInventory().unEquipItem(targetCancelItem);
        }
        targetCancelItem.setVisibleItemId(0);
        if (equipped) {
            player.getInventory().equipItem(targetCancelItem);
        }
        final InventoryUpdate iu = new InventoryUpdate().addModifiedItem(targetCancelItem);
        final SystemMessage sm = new SystemMessage(1965);
        sm.addItemName(targetCancelItem.getItemId());
        player.sendPacket(new ExVariationCancelResult(1), iu, sm);
        player.getAllShortCuts().stream().filter(sc -> sc.getId() == targetCancelItem.getObjectId() && sc.getType() == 1).forEach(sc -> player.sendPacket(new ShortCutRegister(player, sc)));
        player.sendChanges();
        player.broadcastUserInfo(true);
    }
}
