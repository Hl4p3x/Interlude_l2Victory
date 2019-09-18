package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.BuyListHolder;
import ru.j2dev.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.Inventory;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShopPreviewInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShopPreviewList;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

import java.util.HashMap;
import java.util.Map;

public class RequestPreviewItem extends L2GameClientPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestPreviewItem.class);

    private int _unknow;
    private int _listId;
    private int _count;
    private int[] _items;

    @Override
    protected void readImpl() {
        _unknow = readD();
        _listId = readD();
        _count = readD();
        if (_count * 4 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new int[_count];
        for (int i = 0; i < _count; ++i) {
            _items[i] = readD();
        }
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || _count == 0) {
            return;
        }
        if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isInStoreMode()) {
            activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }
        if (activeChar.isInTrade()) {
            activeChar.sendActionFailed();
            return;
        }
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0 && !activeChar.isGM()) {
            activeChar.sendActionFailed();
            return;
        }
        final NpcInstance merchant = activeChar.getLastNpc();
        final boolean isValidMerchant = merchant != null && merchant.isMerchantNpc();
        if (!activeChar.isGM() && (merchant == null || !isValidMerchant || !merchant.isInActingRange(activeChar))) {
            activeChar.sendActionFailed();
            return;
        }
        final NpcTradeList list = BuyListHolder.getInstance().getBuyList(_listId);
        if (list == null) {
            activeChar.sendActionFailed();
            return;
        }
        final int slots = 0;
        long totalPrice = 0L;
        final Map<Integer, Integer> itemList = new HashMap<>();
        try {
            for (int i = 0; i < _count; ++i) {
                final int itemId = _items[i];
                if (list.getItemByItemId(itemId) == null) {
                    activeChar.sendActionFailed();
                    return;
                }
                final ItemTemplate template = ItemTemplateHolder.getInstance().getTemplate(itemId);
                if (template != null) {
                    if (template.isEquipable()) {
                        final int paperdoll = Inventory.getPaperdollIndex(template.getBodyPart());
                        if (paperdoll >= 0) {
                            if (itemList.containsKey(paperdoll)) {
                                activeChar.sendPacket(Msg.THOSE_ITEMS_MAY_NOT_BE_TRIED_ON_SIMULTANEOUSLY);
                                return;
                            }
                            itemList.put(paperdoll, itemId);
                            totalPrice += ShopPreviewList.getWearPrice(template);
                        }
                    }
                }
            }
            if (!activeChar.reduceAdena(totalPrice)) {
                activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }
        } catch (ArithmeticException ae) {
            sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        }
        if (!itemList.isEmpty()) {
            activeChar.sendPacket(new ShopPreviewInfo(itemList));
            ThreadPoolManager.getInstance().schedule(new RemoveWearItemsTask(activeChar), Config.WEAR_DELAY * 1000);
        }
    }

    private static class RemoveWearItemsTask extends RunnableImpl {
        private final Player _activeChar;

        public RemoveWearItemsTask(final Player activeChar) {
            _activeChar = activeChar;
        }

        @Override
        public void runImpl() {
            _activeChar.sendPacket(Msg.TRYING_ON_MODE_HAS_ENDED);
            _activeChar.sendUserInfo(true);
        }
    }
}
