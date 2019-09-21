package ru.j2dev.gameserver.utils;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.manager.CursedWeaponsManager;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.PetInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;
import ru.j2dev.gameserver.model.items.attachment.PickableAttachment;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.tables.PetDataTable;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

import java.util.Arrays;
import java.util.Objects;

public final class ItemFunctions {
    public static final int[][] catalyst = {{12362, 14078, 14702}, {12363, 14079, 14703}, {12364, 14080, 14704}, {12365, 14081, 14705}, {12366, 14082, 14706}, {12367, 14083, 14707}, {12368, 14084, 14708}, {12369, 14085, 14709}, {12370, 14086, 14710}, {12371, 14087, 14711}};

    public static ItemInstance createItem(final int itemId) {
        final ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
        item.setLocation(ItemLocation.VOID);
        item.setCount(1L);
        return item;
    }

    public static void addItem(final Playable playable, final int itemId, final long count) {
        addItem(playable, itemId, count, true);
    }

    public static void addItem(final Playable playable, final int itemId, final long count, final boolean notify) {
        if (playable == null || count < 1L) {
            return;
        }
        Playable player;
        if (playable.isSummon()) {
            player = playable.getPlayer();
        } else {
            player = playable;
        }
        final ItemTemplate t = ItemTemplateHolder.getInstance().getTemplate(itemId);
        if (Objects.requireNonNull(t).isStackable()) {
            player.getInventory().addItem(itemId, count);
        } else {
            for (long i = 0L; i < count; ++i) {
                player.getInventory().addItem(itemId, 1L);
            }
        }
        if (notify) {
            player.sendPacket(SystemMessage2.obtainItems(itemId, count, 0));
        }
    }

    public static long getItemCount(final Playable playable, final int itemId) {
        if (playable == null) {
            return 0L;
        }
        final Playable player = playable.getPlayer();
        return player.getInventory().getCountOf(itemId);
    }



    public static long removeItem(final Playable playable, final int itemId, final long count, final boolean notify) {
        long removed = 0L;
        if (playable == null || count < 1L) {
            return removed;
        }
        final Playable player = playable.getPlayer();
        final ItemTemplate t = ItemTemplateHolder.getInstance().getTemplate(itemId);
        if (Objects.requireNonNull(t).isStackable()) {
            if (player.getInventory().destroyItemByItemId(itemId, count)) {
                removed = count;
            }
        } else {
            for (long i = 0L; i < count; ++i) {
                if (player.getInventory().destroyItemByItemId(itemId, 1L)) {
                    ++removed;
                }
            }
        }
        if (removed > 0L && notify) {
            player.sendPacket(SystemMessage2.removeItems(itemId, removed));
        }
        return removed;
    }

    public static SystemMessage checkIfCanEquip(final PetInstance pet, final ItemInstance item) {
        if (!item.isEquipable()) {
            return Msg.ITEM_NOT_AVAILABLE_FOR_PETS;
        }
        final int petId = pet.getNpcId();
        if (item.getTemplate().isPendant() || (PetDataTable.isWolf(petId) && item.getTemplate().isForWolf()) || (PetDataTable.isHatchling(petId) && item.getTemplate().isForHatchling()) || (PetDataTable.isStrider(petId) && item.getTemplate().isForStrider()) || (PetDataTable.isGWolf(petId) && item.getTemplate().isForGWolf()) || (PetDataTable.isBabyPet(petId) && item.getTemplate().isForPetBaby()) || (PetDataTable.isImprovedBabyPet(petId) && item.getTemplate().isForPetBaby())) {
            return null;
        }
        return Msg.ITEM_NOT_AVAILABLE_FOR_PETS;
    }

    public static L2GameServerPacket checkIfCanEquip(final Player player, final ItemInstance item) {
        final int itemId = item.getItemId();
        final int targetSlot = item.getTemplate().getBodyPart();
        final Clan clan = player.getClan();
        if (itemId >= 7850 && itemId <= 7859 && player.getLvlJoinedAcademy() == 0) {
            return Msg.THIS_ITEM_CAN_ONLY_BE_WORN_BY_A_MEMBER_OF_THE_CLAN_ACADEMY;
        }
        if (ArrayUtils.contains(ItemTemplate.ITEM_ID_CASTLE_CIRCLET, itemId) && (clan == null || itemId != ItemTemplate.ITEM_ID_CASTLE_CIRCLET[clan.getCastle()])) {
            return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
        }
        if (itemId == 6841 && (clan == null || !player.isClanLeader() || clan.getCastle() == 0)) {
            return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
        }
        if (targetSlot == 16384 || targetSlot == 256 || targetSlot == 128) {
            if (itemId != player.getInventory().getPaperdollItemId(7) && CursedWeaponsManager.getInstance().isCursed(player.getInventory().getPaperdollItemId(7))) {
                return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
            }
            if (player.isCursedWeaponEquipped() && itemId != player.getCursedWeaponEquippedId()) {
                return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
            }
        }
        return null;
    }

    public static boolean checkIfCanPickup(final Playable playable, final ItemInstance item) {
        final Player player = playable.getPlayer();
        return item.getDropTimeOwner() <= System.currentTimeMillis() || item.getDropPlayers().contains(player.getObjectId());
    }

    public static boolean canAddItem(final Player player, final ItemInstance item) {
        if (!player.getInventory().validateWeight(item)) {
            player.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
            return false;
        }
        if (!player.getInventory().validateCapacity(item)) {
            player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
            return false;
        }
        if (!item.getTemplate().getHandler().pickupItem(player, item)) {
            return false;
        }
        final PickableAttachment attachment = (item.getAttachment() instanceof PickableAttachment) ? ((PickableAttachment) item.getAttachment()) : null;
        return attachment == null || attachment.canPickUp(player);
    }

    public static boolean checkIfCanDiscard(final Player player, final ItemInstance item) {
        return !item.isHeroWeapon() && (!PetDataTable.isPetControlItem(item) || !player.isMounted()) && player.getPetControlItem() != item && player.getEnchantScroll() != item && !item.isCursed() && !item.getTemplate().isQuest();
    }

    public static boolean isBlessedEnchantScroll(final int itemId) {
        switch (itemId) {
            case 6569:
            case 6570:
            case 6571:
            case 6572:
            case 6573:
            case 6574:
            case 6575:
            case 6576:
            case 6577:
            case 6578: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public static boolean isCrystallEnchantScroll(final int itemId) {
        switch (itemId) {
            case 731:
            case 732:
            case 949:
            case 950:
            case 953:
            case 954:
            case 957:
            case 958:
            case 961:
            case 962: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public static int getEnchantCrystalId(final ItemInstance item, final ItemInstance scroll, final ItemInstance catalyst) {
        boolean scrollValid;
        boolean catalystValid;
        scrollValid = Arrays.stream(getEnchantScrollId(item)).anyMatch(scrollId -> scroll.getItemId() == scrollId);
        if (catalyst == null) {
            catalystValid = true;
        } else {
            catalystValid = Arrays.stream(getEnchantCatalystId(item)).anyMatch(catalystId -> catalystId == catalyst.getItemId());
        }
        if (scrollValid && catalystValid) {
            switch (item.getCrystalType().cry) {
                case 0: {
                    return 0;
                }
                case 1458: {
                    return 1458;
                }
                case 1459: {
                    return 1459;
                }
                case 1460: {
                    return 1460;
                }
                case 1461: {
                    return 1461;
                }
                case 1462: {
                    return 1462;
                }
            }
        }
        return -1;
    }

    public static int[] getEnchantScrollId(final ItemInstance item) {
        if (item.getTemplate().getType2() == 0) {
            switch (item.getCrystalType().cry) {
                case 0: {
                    return new int[]{13540};
                }
                case 1458: {
                    return new int[]{955, 6575, 957};
                }
                case 1459: {
                    return new int[]{951, 6573, 953};
                }
                case 1460: {
                    return new int[]{947, 6571, 949};
                }
                case 1461: {
                    return new int[]{729, 6569, 731};
                }
                case 1462: {
                    return new int[]{959, 6577, 961};
                }
            }
        } else if (item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2) {
            switch (item.getCrystalType().cry) {
                case 1458: {
                    return new int[]{956, 6576, 958};
                }
                case 1459: {
                    return new int[]{952, 6574, 954};
                }
                case 1460: {
                    return new int[]{948, 6572, 950};
                }
                case 1461: {
                    return new int[]{730, 6570, 732};
                }
                case 1462: {
                    return new int[]{960, 6578, 962};
                }
            }
        }
        return new int[0];
    }

    public static int[] getEnchantCatalystId(final ItemInstance item) {
        if (item.getTemplate().getType2() == 0) {
            switch (item.getCrystalType().cry) {
                case 1461: {
                    return catalyst[3];
                }
                case 1460: {
                    return catalyst[2];
                }
                case 1459: {
                    return catalyst[1];
                }
                case 1458: {
                    return catalyst[0];
                }
                case 1462: {
                    return catalyst[4];
                }
            }
        } else if (item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2) {
            switch (item.getCrystalType().cry) {
                case 1461: {
                    return catalyst[8];
                }
                case 1460: {
                    return catalyst[7];
                }
                case 1459: {
                    return catalyst[6];
                }
                case 1458: {
                    return catalyst[5];
                }
                case 1462: {
                    return catalyst[9];
                }
            }
        }
        return new int[]{0, 0, 0};
    }

    public static double getEnchantChance(final ItemInstance item) {
        int enchantLevel = item.getEnchantLevel();
        double[] enchantChances = null;
        switch (item.getTemplate().getBodyPart()) {
            case 32768: {
                enchantChances = Config.ENCHANT_CHANCES_FULL_ARMOR;
                break;
            }
            case 1:
            case 64:
            case 256:
            case 512:
            case 1024:
            case 2048:
            case 4096:
            case 8192: {
                enchantChances = Config.ENCHANT_CHANCES_ARMOR;
                break;
            }
            case 6:
            case 8:
            case 48: {
                enchantChances = Config.ENCHANT_CHANCES_JEWELRY;
                break;
            }
            case 128:
            case 16384: {
                enchantChances = Config.ENCHANT_CHANCES_WEAPON;
                break;
            }
        }
        if (enchantChances == null && Config.ALT_HAIR_TO_ACC_SLOT && (item.getTemplate().getBodyPart() == 65536 || item.getTemplate().getBodyPart() == 262144 || item.getTemplate().getBodyPart() == 524288)) {
            enchantChances = Config.ENCHANT_CHANCES_ARMOR;
        }
        if (enchantChances == null) {
            return 0.0;
        }
        if (enchantLevel >= enchantChances.length) {
            enchantLevel = enchantChances.length - 1;
        }
        return enchantChances[enchantLevel];
    }
}
