package handler.items;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ru.j2dev.gameserver.ai.NextAction;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SendTradeDone;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;
import ru.j2dev.gameserver.utils.ItemFunctions;

public class EquipableItem extends ScriptItemHandler {
    private final int[] _itemIds;

    public EquipableItem() {
        final TIntSet set = new TIntHashSet();
        for (final ItemTemplate template : ItemTemplateHolder.getInstance().getAllTemplates()) {
            if (template != null) {
                if (template.isEquipable()) {
                    set.add(template.getItemId());
                }
            }
        }
        _itemIds = set.toArray();
    }

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (!playable.isPlayer()) {
            return false;
        }
        final Player player = playable.getPlayer();
        if (player.isAttackingNow() || player.isCastingNow()) {
            player.sendPacket(new SystemMessage(104));
            if (player.isProcessingRequest()) {
                final Request request = player.getRequest();
                if (player.isInTrade()) {
                    final Player parthner = request.getOtherPlayer(player);
                    player.sendPacket(SendTradeDone.FAIL);
                    parthner.sendPacket(SendTradeDone.FAIL);
                }
                request.cancel();
            }
            player.getAI().setNextAction(NextAction.EQUIP, item, null, ctrl, false);
            return false;
        }
        if (player.isStunned() || player.isSleeping() || player.isParalyzed() || player.isAlikeDead() || player.isFakeDeath()) {
            player.sendPacket(new SystemMessage(113).addItemName(item.getItemId()));
            return false;
        }
        final int bodyPart = item.getBodyPart();
        if ((bodyPart == 16384 || bodyPart == 256 || bodyPart == 128) && (player.isMounted() || player.isCursedWeaponEquipped() || player.getActiveWeaponFlagAttachment() != null || (player.isWeaponEquipBlocked() && item.getItemType() != WeaponType.NONE))) {
            player.sendPacket(new SystemMessage(113).addItemName(item.getItemId()));
            return false;
        }
        if (item.isCursed()) {
            player.sendPacket(new SystemMessage(113).addItemName(item.getItemId()));
            return false;
        }
        if (item.isEquipped()) {
            final ItemInstance weapon = player.getActiveWeaponInstance();
            if (item == weapon) {
                player.abortAttack(true, true);
                player.abortCast(true, true);
            }
            player.sendDisarmMessage(item);
            player.getInventory().unEquipItem(item);
            return false;
        }
        final L2GameServerPacket p = ItemFunctions.checkIfCanEquip(player, item);
        if (p != null) {
            player.sendPacket(p);
            return false;
        }
        player.getInventory().equipItem(item);
        if (!item.isEquipped()) {
            player.sendActionFailed();
            return false;
        }
        SystemMessage sm;
        if (item.getEnchantLevel() > 0) {
            sm = new SystemMessage(368);
            sm.addNumber(item.getEnchantLevel());
            sm.addItemName(item.getItemId());
        } else {
            sm = new SystemMessage(49).addItemName(item.getItemId());
        }
        player.sendPacket(sm);
        return true;
    }

    @Override
    public int[] getItemIds() {
        return _itemIds;
    }
}
