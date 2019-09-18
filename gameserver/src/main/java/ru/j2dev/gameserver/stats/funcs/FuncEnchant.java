package ru.j2dev.gameserver.stats.funcs;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.item.ItemType;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;

public class FuncEnchant extends Func {
    public FuncEnchant(final Stats stat, final int order, final Object owner, final double value) {
        super(stat, order, owner);
    }

    private int getItemImpliedEnchantLevel(final ItemInstance item) {
        if (item == null) {
            return 0;
        }
        int enchLvlLim = -1;
        if (item.isArmor()) {
            enchLvlLim = Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_ARMOR;
        } else if (item.isWeapon()) {
            enchLvlLim = (item.getTemplate().isMageItem() ? Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_WEAPON_MAGE : Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_WEAPON_PHYS);
        } else if (item.isAccessory()) {
            enchLvlLim = Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_ACCESSORY;
        }
        final int enchant = item.getEnchantLevel();
        if (enchLvlLim < 0) {
            return enchant;
        }
        final int itemOwnerId = item.getOwnerId();
        final Player player = GameObjectsStorage.getPlayer(itemOwnerId);
        if (player == null || !player.isOlyParticipant()) {
            return enchant;
        }
        return Math.min(enchant, enchLvlLim);
    }

    @Override
    public void calc(final Env env) {
        final ItemInstance item = (ItemInstance) getOwner();
        final int enchant = getItemImpliedEnchantLevel(item);
        final int overenchant = Math.max(0, enchant - 3);
        switch (getStat()) {
            case MAGIC_ATTACK_SPEED:
            case POWER_ATTACK_SPEED: {
                env.value += (enchant + overenchant * 2) / 10;
            }
            case SHIELD_DEFENCE:
            case MAGIC_DEFENCE:
            case POWER_DEFENCE: {
                env.value += enchant + overenchant * 2;
            }
            case MAGIC_ATTACK: {
                switch (item.getTemplate().getCrystalType().cry) {
                    case 1462: {
                        env.value += 4 * (enchant + overenchant);
                        break;
                    }
                    case 1461: {
                        env.value += 3 * (enchant + overenchant);
                        break;
                    }
                    case 1460: {
                        env.value += 3 * (enchant + overenchant);
                        break;
                    }
                    case 1459: {
                        env.value += 3 * (enchant + overenchant);
                        break;
                    }
                    case 0:
                    case 1458: {
                        env.value += 2 * (enchant + overenchant);
                        break;
                    }
                }
            }
            case POWER_ATTACK: {
                final ItemType itemType = item.getItemType();
                final boolean isBow = itemType == WeaponType.BOW;
                final boolean isDSword = (itemType == WeaponType.DUALFIST || itemType == WeaponType.DUAL || itemType == WeaponType.BIGSWORD || itemType == WeaponType.SWORD) && item.getTemplate().getBodyPart() == 16384;
                switch (item.getTemplate().getCrystalType().cry) {
                    case 1462: {
                        if (isBow) {
                            env.value += 10 * (enchant + overenchant);
                            break;
                        }
                        if (isDSword) {
                            env.value += 6 * (enchant + overenchant);
                            break;
                        }
                        env.value += 5 * (enchant + overenchant);
                        break;
                    }
                    case 1461: {
                        if (isBow) {
                            env.value += 8 * (enchant + overenchant);
                            break;
                        }
                        if (isDSword) {
                            env.value += 5 * (enchant + overenchant);
                            break;
                        }
                        env.value += 4 * (enchant + overenchant);
                        break;
                    }
                    case 1459:
                    case 1460: {
                        if (isBow) {
                            env.value += 6 * (enchant + overenchant);
                            break;
                        }
                        if (isDSword) {
                            env.value += 4 * (enchant + overenchant);
                            break;
                        }
                        env.value += 3 * (enchant + overenchant);
                        break;
                    }
                    case 0:
                    case 1458: {
                        if (isBow) {
                            env.value += 4 * (enchant + overenchant);
                            break;
                        }
                        env.value += 2 * (enchant + overenchant);
                        break;
                    }
                }
            }
        }
    }
}
