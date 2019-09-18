package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.EnchantItemHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.PcInventory;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.EnchantResult;
import ru.j2dev.gameserver.network.lineage2.serverpackets.InventoryUpdate;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.templates.item.support.EnchantScroll;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

import java.util.List;

public class RequestEnchantItem extends L2GameClientPacket {
    private int _objectId;

    private static void broadcastResult(final Player enchanter, final ItemInstance item) {
        if (item.getTemplate().getType2() == 0) {
            if (item.getEnchantLevel() == 7 || item.getEnchantLevel() == 15) {
                final MagicSkillUse msu = new MagicSkillUse(enchanter, enchanter, 2025, 1, 500, 1500L);
                final List<Player> players = World.getAroundPlayers(enchanter);
                for (final Player player : players) {
                    player.sendMessage(new CustomMessage("_C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3", player, enchanter, item, item.getEnchantLevel()));
                    player.sendPacket(msu);
                }
            }
        } else if (item.getEnchantLevel() == 6) {
            final MagicSkillUse msu = new MagicSkillUse(enchanter, enchanter, 2025, 1, 500, 1500L);
            final List<Player> players = World.getAroundPlayers(enchanter);
            for (final Player player : players) {
                player.sendMessage(new CustomMessage("_C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3", player, enchanter, item, item.getEnchantLevel()));
                player.sendPacket(msu);
            }
        }
    }

    @Override
    protected void readImpl() {
        _objectId = readD();
    }

    @Override
    protected void runImpl() {
        final GameClient client = getClient();
        if (client == null) {
            return;
        }
        final Player player = client.getActiveChar();
        if (player == null) {
            return;
        }
        if (player.isActionsDisabled()) {
            player.setEnchantScroll(null);
            player.sendActionFailed();
            return;
        }
        final long now = System.currentTimeMillis();
        if (now - client.getLastIncomePacketTimeStamp(RequestEnchantItem.class) < Config.ENCHANT_PACKET_DELAY) {
            player.setEnchantScroll(null);
            player.sendActionFailed();
            return;
        }
        client.setLastIncomePacketTimeStamp(RequestEnchantItem.class, now);
        if (player.isInTrade()) {
            player.setEnchantScroll(null);
            player.sendActionFailed();
            return;
        }
        if (player.isInStoreMode()) {
            player.setEnchantScroll(null);
            player.sendPacket(EnchantResult.CANCEL);
            player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
            player.sendActionFailed();
            return;
        }
        final PcInventory inventory = player.getInventory();
        inventory.writeLock();
        try {
            final ItemInstance item = inventory.getItemByObjectId(_objectId);
            final ItemInstance scroll = player.getEnchantScroll();
            if (item == null || scroll == null) {
                player.sendActionFailed();
                return;
            }
            final EnchantScroll enchantScroll = EnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId());
            if (enchantScroll == null) {
                player.sendActionFailed();
                return;
            }
            if (!item.canBeEnchanted(false)) {
                player.sendPacket(EnchantResult.CANCEL);
                player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
                player.sendActionFailed();
                return;
            }
            if (!enchantScroll.isUsableWith(item)) {
                player.sendPacket(EnchantResult.CANCEL);
                player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
                player.sendActionFailed();
                return;
            }
            double chanceMod = 1.0 + enchantScroll.getChanceMod();
            final int itemEnchantLevel = item.getEnchantLevel();
            final int itemId = item.getItemId();
            final int toLvl = itemEnchantLevel + enchantScroll.getIncrement();
            chanceMod *= player.getBonus().getEnchantItemMul();
            if (!inventory.destroyItem(scroll, 1L)) {
                player.sendPacket(EnchantResult.CANCEL);
                player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
                player.sendActionFailed();
                return;
            }
            final boolean equipped;
            if (equipped = item.isEquipped()) {
                inventory.unEquipItem(item);
            }
            final double chance = ItemFunctions.getEnchantChance(item);
            if (enchantScroll.isInfallible() || Rnd.chance(chance * chanceMod)) {
                item.setEnchantLevel(toLvl);
                Log.LogItem(player, ItemLog.EnchantSuccess, item);
                if (equipped) {
                    inventory.equipItem(item);
                }
                player.sendPacket(new InventoryUpdate().addModifiedItem(item));
                player.sendPacket(new SystemMessage(63).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
                player.sendPacket(EnchantResult.SUCESS);
                if (Config.SHOW_ENCHANT_EFFECT_RESULT) {
                    broadcastResult(player, item);
                }
            } else {
                switch (enchantScroll.getOnFailAction()) {
                    case CRYSTALIZE: {
                        if (item.isEquipped()) {
                            player.sendDisarmMessage(item);
                        }
                        Log.LogItem(player, ItemLog.EnchantFail, item);
                        final int itemCrystalId = item.getCrystalType().cry;
                        final int itemCrystalCount = item.getTemplate().getCrystalCount();
                        if (!inventory.destroyItem(item, 1L)) {
                            player.sendActionFailed();
                            return;
                        }
                        if (itemCrystalId > 0 && itemCrystalCount > 0) {
                            int crystalAmount = (int) (itemCrystalCount * 0.87);
                            if (itemEnchantLevel > 3) {
                                crystalAmount += (int) (itemCrystalCount * 0.25 * (itemEnchantLevel - 3));
                            }
                            if (crystalAmount < 1) {
                                crystalAmount = 1;
                            }
                            player.sendPacket(new EnchantResult(1, itemCrystalId, crystalAmount));
                            player.sendPacket(new SystemMessage(65).addNumber(itemEnchantLevel).addItemName(itemId));
                            ItemFunctions.addItem(player, itemCrystalId, crystalAmount, true);
                            break;
                        }
                        player.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);
                        player.sendPacket(new SystemMessage(64).addItemName(item.getItemId()));
                        break;
                    }
                    case RESET: {
                        final int resetLvl = Math.min(item.getEnchantLevel(), enchantScroll.getFailResultLevel());
                        item.setEnchantLevel(resetLvl);
                        if (equipped) {
                            inventory.equipItem(item);
                        }
                        Log.LogItem(player, ItemLog.EnchantFail, item);
                        player.sendPacket(new InventoryUpdate().addModifiedItem(item));
                        player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED);
                        player.sendPacket(EnchantResult.BLESSED_FAILED);
                        break;
                    }
                    case NONE: {
                        if (equipped) {
                            inventory.equipItem(item);
                        }
                        Log.LogItem(player, ItemLog.EnchantFail, item);
                        player.sendPacket(EnchantResult.ANCIENT_FAILED);
                        player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
                        break;
                    }
                }
            }
        } finally {
            inventory.writeUnlock();
            player.setEnchantScroll(null);
            player.updateStats();
        }
    }
}
