package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.OptionDataHolder;
import ru.j2dev.gameserver.data.xml.holder.VariationGroupHolder;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.InventoryUpdate;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShortCutRegister;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.item.support.VariationGroupData;

import java.util.List;

public class AdminEnchant implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanEditChar) {
            return false;
        }
        int armorType = -1;
        switch (command) {
            case admin_enchant: {
                showMainPage(activeChar);
                return true;
            }
            case admin_seteh: {
                armorType = 6;
                break;
            }
            case admin_setec: {
                armorType = 10;
                break;
            }
            case admin_seteg: {
                armorType = 9;
                break;
            }
            case admin_seteb: {
                armorType = 12;
                break;
            }
            case admin_setel: {
                armorType = 11;
                break;
            }
            case admin_setew: {
                armorType = 7;
                break;
            }
            case admin_setes: {
                armorType = 8;
                break;
            }
            case admin_setle: {
                armorType = 2;
                break;
            }
            case admin_setre: {
                armorType = 1;
                break;
            }
            case admin_setlf: {
                armorType = 5;
                break;
            }
            case admin_setrf: {
                armorType = 4;
                break;
            }
            case admin_seten: {
                armorType = 3;
                break;
            }
            case admin_setun: {
                armorType = 0;
                break;
            }
            case admin_setba: {
                armorType = 13;
                break;
            }
            case admin_setha: {
                armorType = 15;
                break;
            }
            case admin_setdha: {
                armorType = 15;
                break;
            }
            case admin_variation: {
                adminSetVatiation(activeChar, wordList);
                break;
            }
        }
        if (armorType == -1 || wordList.length < 2) {
            showMainPage(activeChar);
            return true;
        }
        try {
            final int ench = Integer.parseInt(wordList[1]);
            if (ench < 0 || ench > 65535) {
                activeChar.sendMessage("You must set the enchant level to be between 0-65535.");
            } else {
                setEnchant(activeChar, ench, armorType);
            }
        } catch (StringIndexOutOfBoundsException e) {
            activeChar.sendMessage("Please specify a new enchant value.");
        } catch (NumberFormatException e2) {
            activeChar.sendMessage("Please specify a valid new enchant value.");
        }
        showMainPage(activeChar);
        return true;
    }

    private void setEnchant(final Player activeChar, final int ench, final int armorType) {
        GameObject target = activeChar.getTarget();
        if (target == null) {
            target = activeChar;
        }
        if (!target.isPlayer()) {
            activeChar.sendMessage("Wrong target type.");
            return;
        }
        final Player player = (Player) target;
        int curEnchant;
        final ItemInstance itemInstance = player.getInventory().getPaperdollItem(armorType);
        if (itemInstance != null) {
            curEnchant = itemInstance.getEnchantLevel();
            player.getInventory().unEquipItem(itemInstance);
            itemInstance.setEnchantLevel(ench);
            player.getInventory().equipItem(itemInstance);
            player.sendPacket(new InventoryUpdate().addModifiedItem(itemInstance));
            player.broadcastCharInfo();
            activeChar.sendMessage("Changed enchantment of " + player.getName() + "'s " + itemInstance.getName() + " from " + curEnchant + " to " + ench + ".");
            player.sendMessage("Admin has changed the enchantment of your " + itemInstance.getName() + " from " + curEnchant + " to " + ench + ".");
        }
    }

    private void adminSetVatiation(final Player activeChar, final String[] wordList) {
        final GameObject target = activeChar.getTarget();
        if (target != null && target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll)) {
            final Player player = (Player) target;
            if (wordList.length == 3) {
                final int id = Integer.parseInt(wordList[1]);
                final int level = Integer.parseInt(wordList[2]);
                final Skill skill = SkillTable.getInstance().getInfo(id, level);
                if (skill != null) {
                    int variation = getVariationId(id, level);
                    if(variation > 0) {
                        processAugment(activeChar, player, variation);
                    }
                } else {
                    activeChar.sendMessage("Error: there is no such skill or variation.");
                }
            }
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    private int getVariationId(final int skllId, final int skillLvl) {
        return OptionDataHolder.getInstance().getVariationStatBySkillIdAndLvl(skllId, skillLvl);
    }

    private void processAugment(final Player activeChar, final Player player, final int _variationOption1) {
        if (player == null || player.isLogoutStarted() || player.isTeleporting()) {
            return;
        }
        final ItemInstance targetItem = player.getInventory().getPaperdollItem(7);
        if (targetItem == null) {
            activeChar.sendMessage(new CustomMessage("services.VariationSellService.process.EquippedItemRequired", player));
            return;
        }
        if (targetItem.isAugmented()) {
            activeChar.sendPacket(Msg.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN, ActionFail.STATIC);
            return;
        }
        final List<VariationGroupData> variationGroupDataList = VariationGroupHolder.getInstance().getDataForItemId(targetItem.getItemId());
        if (variationGroupDataList == null || variationGroupDataList.isEmpty()) {
            activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
            return;
        }
        final boolean equipped;
        if (equipped = targetItem.isEquipped()) {
            player.getInventory().unEquipItem(targetItem);
        }
        targetItem.setVariationStat1(3580);
        targetItem.setVariationStat2(_variationOption1);
        if (equipped) {
            player.getInventory().equipItem(targetItem);
        }
        activeChar.sendMessage("Changed augment of " + player.getName() + "'s " + targetItem.getName() + ".");
        player.sendMessage("Admin has changed the augment of your " + targetItem.getName() + ".");
        player.sendPacket(new InventoryUpdate().addModifiedItem(targetItem));
        player.getAllShortCuts().stream().filter(sc -> sc.getId() == targetItem.getObjectId() && sc.getType() == 1).forEach(sc -> player.sendPacket(new ShortCutRegister(player, sc)));
        player.sendChanges();
    }

    private void showMainPage(final Player activeChar) {
        GameObject target = activeChar.getTarget();
        if (target == null) {
            target = activeChar;
        }
        Player player = activeChar;
        if (target.isPlayer()) {
            player = (Player) target;
        }
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5).setFile("admin/enchant.htm");
        adminReply.replace("%player%", player.getName());
        activeChar.sendPacket(adminReply);
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_seteh,
        admin_setec,
        admin_seteg,
        admin_setel,
        admin_seteb,
        admin_setew,
        admin_setes,
        admin_setle,
        admin_setre,
        admin_setlf,
        admin_setrf,
        admin_seten,
        admin_setun,
        admin_setba,
        admin_setha,
        admin_setdha,
        admin_setlbr,
        admin_setrbr,
        admin_setbelt,
        admin_enchant,
        admin_variation
    }
}
