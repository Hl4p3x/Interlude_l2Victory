package ru.j2dev.gameserver.handler.items;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.RandomUtils;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.VariationChanceHolder;
import ru.j2dev.gameserver.data.xml.holder.VariationGroupHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.ShortCut;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.templates.item.support.VariationChanceData;
import ru.j2dev.gameserver.templates.item.support.VariationGroupData;

import java.util.List;

public class RefineryHandler implements IRefineryHandler {
    protected static final Logger LOGGER = LoggerFactory.getLogger(RefineryHandler.class);
    private static final RefineryHandler _instance = new RefineryHandler();

    public static RefineryHandler getInstance() {
        return _instance;
    }

    private boolean checkPlayer(final Player player) {
        if (player.isInStoreMode()) {
            player.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
            return false;
        }
        if (player.isInTrade()) {
            player.sendActionFailed();
            return false;
        }
        if (player.isDead()) {
            player.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
            return false;
        }
        if (player.isParalyzed()) {
            player.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
            return false;
        }
        if (player.isFishing()) {
            player.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
            return false;
        }
        if (player.isSitting()) {
            player.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
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
            player.sendActionFailed();
            return;
        }
        player.sendPacket(Msg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowRefineryInterface.STATIC);
    }

    @Override
    public void onPutTargetItem(final Player player, final ItemInstance targetItem) {
        if (!checkPlayer(player)) {
            player.sendActionFailed();
            return;
        }
        if (targetItem.isAugmented()) {
            player.sendPacket(Msg.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN, ActionFail.STATIC);
            return;
        }
        final List<VariationGroupData> variationGroupDataList = VariationGroupHolder.getInstance().getDataForItemId(targetItem.getItemId());
        if (variationGroupDataList == null || variationGroupDataList.isEmpty()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        player.sendPacket(Msg.SELECT_THE_CATALYST_FOR_AUGMENTATION, new ExPutItemResultForVariationMake(targetItem.getObjectId(), true));
    }

    @Override
    public void onPutMineralItem(final Player player, final ItemInstance targetItem, final ItemInstance mineralItem) {
        if (!checkPlayer(player)) {
            player.sendPacket(ActionFail.STATIC);
            return;
        }
        if (targetItem.isAugmented()) {
            player.sendPacket(Msg.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN, ActionFail.STATIC);
            return;
        }
        final List<VariationGroupData> variationGroupDataList = VariationGroupHolder.getInstance().getDataForItemId(targetItem.getItemId());
        if (variationGroupDataList == null || variationGroupDataList.isEmpty()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        final int mineralItemId = mineralItem.getItemId();
        VariationGroupData variationGroupDataOfMineral = null;
        final Pair<VariationChanceData, VariationChanceData> variationChanceData = VariationChanceHolder.getInstance().getVariationChanceDataForMineral(mineralItemId);
        for (final VariationGroupData variationGroupData : variationGroupDataList) {
            if (variationGroupData.getMineralItemId() == mineralItemId) {
                variationGroupDataOfMineral = variationGroupData;
                break;
            }
        }
        if (null == variationGroupDataOfMineral || null == variationChanceData) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if (targetItem.getTemplate().isMageItem() && variationChanceData.getRight() == null) {
            LOGGER.warn("No mage variation for item " + targetItem.getItemId() + " and mineral " + mineralItem.getItemId());
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if (!targetItem.getTemplate().isMageItem() && variationChanceData.getLeft() == null) {
            LOGGER.warn("No warrior variation for item " + targetItem.getItemId() + " and mineral " + mineralItem.getItemId());
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if (!mineralItem.getTemplate().testCondition(player, mineralItem, true)) {
            player.sendPacket(ActionFail.STATIC);
            return;
        }
        player.sendPacket(new ExPutIntensiveResultForVariationMake(mineralItem.getObjectId(), mineralItemId, variationGroupDataOfMineral.getGemstoneItemId(), variationGroupDataOfMineral.getGemstoneItemCnt(), true), new SystemMessage(1959).addNumber(variationGroupDataOfMineral.getGemstoneItemCnt()).addItemName(variationGroupDataOfMineral.getGemstoneItemId()));
    }

    @Override
    public void onPutGemstoneItem(final Player player, final ItemInstance targetItem, final ItemInstance mineralItem, final ItemInstance gemstoneItem, final long gemstoneItemCnt) {
        if (!checkPlayer(player)) {
            player.sendPacket(ActionFail.STATIC);
            return;
        }
        if (targetItem.isAugmented()) {
            player.sendPacket(Msg.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN, ActionFail.STATIC);
            return;
        }
        final List<VariationGroupData> variationGroupDataList = VariationGroupHolder.getInstance().getDataForItemId(targetItem.getItemId());
        if (variationGroupDataList == null || variationGroupDataList.isEmpty()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        final int mineralItemId = mineralItem.getItemId();
        final int gemstoneItemId = gemstoneItem.getItemId();
        VariationGroupData variationGroupDataOfMineral = null;
        final Pair<VariationChanceData, VariationChanceData> variationChanceData = VariationChanceHolder.getInstance().getVariationChanceDataForMineral(mineralItemId);
        for (final VariationGroupData variationGroupData : variationGroupDataList) {
            if (variationGroupData.getMineralItemId() == mineralItemId && variationGroupData.getGemstoneItemId() == gemstoneItemId) {
                variationGroupDataOfMineral = variationGroupData;
                break;
            }
        }
        if (null == variationGroupDataOfMineral || variationChanceData == null) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if ((variationChanceData.getLeft() != null && variationChanceData.getLeft().getMineralItemId() != variationGroupDataOfMineral.getMineralItemId()) || (variationChanceData.getRight() != null && variationChanceData.getRight().getMineralItemId() != variationGroupDataOfMineral.getMineralItemId())) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if (targetItem.getTemplate().isMageItem() && variationChanceData.getRight() == null) {
            LOGGER.warn("No mage variation for item " + targetItem.getItemId() + " and mineral " + mineralItem.getItemId());
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if (!targetItem.getTemplate().isMageItem() && variationChanceData.getLeft() == null) {
            LOGGER.warn("No warrior variation for item " + targetItem.getItemId() + " and mineral " + mineralItem.getItemId());
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if (!mineralItem.getTemplate().testCondition(player, mineralItem, true)) {
            player.sendPacket(ActionFail.STATIC);
            return;
        }
        if (variationGroupDataOfMineral.getGemstoneItemCnt() > gemstoneItemCnt || player.getInventory().getCountOf(gemstoneItemId) < variationGroupDataOfMineral.getGemstoneItemCnt()) {
            player.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT, ActionFail.STATIC);
            return;
        }
        player.sendPacket(new ExPutCommissionResultForVariationMake(gemstoneItem.getObjectId(), variationGroupDataOfMineral.getGemstoneItemCnt()), Msg.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN);
    }

    @Override
    public void onRequestRefine(final Player player, final ItemInstance targetItem, final ItemInstance mineralItem, final ItemInstance gemstoneItem, final long gemstoneItemCnt) {
        if (!checkPlayer(player)) {
            player.sendPacket(ActionFail.STATIC);
            return;
        }
        if (targetItem.isAugmented()) {
            player.sendPacket(Msg.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN, ActionFail.STATIC);
            return;
        }
        final List<VariationGroupData> variationGroupDataList = VariationGroupHolder.getInstance().getDataForItemId(targetItem.getItemId());
        if (variationGroupDataList == null || variationGroupDataList.isEmpty()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        final int mineralItemId = mineralItem.getItemId();
        final int gemstoneItemId = gemstoneItem.getItemId();
        VariationGroupData variationGroupDataOfMineral = null;
        final Pair<VariationChanceData, VariationChanceData> variationChanceData = VariationChanceHolder.getInstance().getVariationChanceDataForMineral(mineralItemId);
        for (final VariationGroupData variationGroupData : variationGroupDataList) {
            if (variationGroupData.getMineralItemId() == mineralItemId && variationGroupData.getGemstoneItemId() == gemstoneItemId) {
                variationGroupDataOfMineral = variationGroupData;
                break;
            }
        }
        if (null == variationGroupDataOfMineral || variationChanceData == null) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if ((variationChanceData.getLeft() != null && variationChanceData.getLeft().getMineralItemId() != variationGroupDataOfMineral.getMineralItemId()) || (variationChanceData.getRight() != null && variationChanceData.getRight().getMineralItemId() != variationGroupDataOfMineral.getMineralItemId())) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if (targetItem.getTemplate().isMageItem() && variationChanceData.getRight() == null) {
            LOGGER.warn("No mage variation for item " + targetItem.getItemId() + " and mineral " + mineralItem.getItemId());
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if (!targetItem.getTemplate().isMageItem() && variationChanceData.getLeft() == null) {
            LOGGER.warn("No warrior variation for item " + targetItem.getItemId() + " and mineral " + mineralItem.getItemId());
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if (!mineralItem.getTemplate().testCondition(player, mineralItem, true)) {
            player.sendPacket(ActionFail.STATIC);
            return;
        }
        if (variationGroupDataOfMineral.getGemstoneItemCnt() > gemstoneItemCnt || player.getInventory().getCountOf(gemstoneItemId) < variationGroupDataOfMineral.getGemstoneItemCnt()) {
            player.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT, ActionFail.STATIC);
            return;
        }
        List<Pair<List<Pair<Integer, Double>>, Double>> variation1Groups;
        List<Pair<List<Pair<Integer, Double>>, Double>> variation2Groups;
        if (targetItem.getTemplate().isMageItem()) {
            variation1Groups = variationChanceData.getRight().getVariation1();
            variation2Groups = variationChanceData.getRight().getVariation2();
        } else {
            variation1Groups = variationChanceData.getLeft().getVariation1();
            variation2Groups = variationChanceData.getLeft().getVariation2();
        }
        final List<Pair<Integer, Double>> variation1 = RandomUtils.pickRandomSortedGroup(variation1Groups, 100.0);
        final List<Pair<Integer, Double>> variation2 = RandomUtils.pickRandomSortedGroup(variation2Groups, 100.0);
        final Integer option1 = (variation1 != null) ? RandomUtils.pickRandomSortedGroup(variation1, 100.0) : 0;
        final Integer option2 = (variation2 != null) ? RandomUtils.pickRandomSortedGroup(variation2, 100.0) : 0;
        if (!player.getInventory().destroyItem(gemstoneItem, variationGroupDataOfMineral.getGemstoneItemCnt())) {
            return;
        }
        if (!player.getInventory().destroyItem(mineralItem, 1L)) {
            return;
        }
        final boolean equipped;
        if (equipped = targetItem.isEquipped()) {
            player.getInventory().unEquipItem(targetItem);
        }
        targetItem.setVariationStat1(option1);
        targetItem.setVariationStat2(option2);
        if (equipped) {
            player.getInventory().equipItem(targetItem);
        }
        player.sendPacket(new InventoryUpdate().addModifiedItem(targetItem));
        for (final ShortCut sc : player.getAllShortCuts()) {
            if (sc.getId() == targetItem.getObjectId() && sc.getType() == 1) {
                player.sendPacket(new ShortCutRegister(player, sc));
            }
        }
        player.sendChanges();
        player.sendPacket(new ExVariationResult(option1, option2, 1));
    }

    @Override
    public void onInitRefineryCancel(final Player player) {
        if (!checkPlayer(player)) {
            return;
        }
        player.sendPacket(Msg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC);
    }

    @Override
    public void onPutTargetCancelItem(final Player player, final ItemInstance targetCancelItem) {
        if (!checkPlayer(player)) {
            player.sendPacket(ActionFail.STATIC);
            return;
        }
        final List<VariationGroupData> variationGroupDataList = VariationGroupHolder.getInstance().getDataForItemId(targetCancelItem.getItemId());
        if (variationGroupDataList == null || variationGroupDataList.isEmpty()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if (!targetCancelItem.isAugmented()) {
            player.sendPacket(Msg.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM, ActionFail.STATIC);
            return;
        }
        final VariationGroupData variationGroupData = variationGroupDataList.get(0);
        if (variationGroupData == null) {
            player.sendPacket(ActionFail.STATIC);
            return;
        }
        player.sendPacket(new ExPutItemResultForVariationCancel(targetCancelItem, variationGroupData.getCancelPrice(), true));
    }

    @Override
    public void onRequestCancelRefine(final Player player, final ItemInstance targetCancelItem) {
        if (!checkPlayer(player)) {
            player.sendPacket(ActionFail.STATIC);
            return;
        }
        final List<VariationGroupData> variationGroupDataList = VariationGroupHolder.getInstance().getDataForItemId(targetCancelItem.getItemId());
        if (variationGroupDataList == null || variationGroupDataList.isEmpty()) {
            player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        if (!targetCancelItem.isAugmented()) {
            player.sendPacket(Msg.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM, ActionFail.STATIC);
            return;
        }
        final VariationGroupData variationGroupData = variationGroupDataList.get(0);
        if (variationGroupData == null) {
            player.sendPacket(ActionFail.STATIC);
            return;
        }
        final long price = variationGroupData.getCancelPrice();
        if (price < 0L) {
            player.sendPacket(new ExVariationCancelResult(0));
        }
        if (!player.reduceAdena(price, true)) {
            player.sendPacket(ActionFail.STATIC, Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
        }
        final boolean equipped;
        if (equipped = targetCancelItem.isEquipped()) {
            player.getInventory().unEquipItem(targetCancelItem);
        }
        targetCancelItem.setVariationStat1(0);
        targetCancelItem.setVariationStat2(0);
        if (equipped) {
            player.getInventory().equipItem(targetCancelItem);
        }
        final InventoryUpdate iu = new InventoryUpdate().addModifiedItem(targetCancelItem);
        final SystemMessage sm = new SystemMessage(1965);
        sm.addItemName(targetCancelItem.getItemId());
        player.sendPacket(new ExVariationCancelResult(1), iu, sm);
        for (final ShortCut sc : player.getAllShortCuts()) {
            if (sc.getId() == targetCancelItem.getObjectId() && sc.getType() == 1) {
                player.sendPacket(new ShortCutRegister(player, sc));
            }
        }
        player.sendChanges();
    }
}
