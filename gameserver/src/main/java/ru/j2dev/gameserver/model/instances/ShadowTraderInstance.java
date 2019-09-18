package ru.j2dev.gameserver.model.instances;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.data.xml.holder.ShadowTradeHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.item.ItemGrade;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.templates.shadowtrade.ShadowTradeItem;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by JunkyFunky
 * on 18.01.2018 18:17
 * group j2dev
 */
@HideAccess
@StringEncryption
public class ShadowTraderInstance extends NpcInstance {
    private final Map<Integer, Long> time = new ConcurrentHashMap<>();
    private final Map<Integer, ShadowTradeItem> itemsMap = new ConcurrentHashMap<>();
    private final ShadowTradeHolder tradeHolder = ShadowTradeHolder.getInstance();
    private int enchant;

    public ShadowTraderInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        ShadowTradeItem tradeItem = itemsMap.get(player.getObjectId());
        if ("request_item".equalsIgnoreCase(command) && Functions.getPay(player, tradeItem.getPriceItemId(), tradeItem.getPriceCount()) && System.currentTimeMillis() < time.get(player.getObjectId())) {
            ItemInstance createditem = ItemFunctions.createItem(tradeItem.getItemId());
            createditem.setCount(tradeItem.getCount());
            if (createditem.getTemplate().getItemGrade() != ItemGrade.NONE) {
                if (createditem.isAccessory() || createditem.isArmor() || createditem.isWeapon()) {
                    enchant = Rnd.get(0, 3);
                    createditem.setEnchantLevel(enchant);

                }
            }
            time.remove(player.getObjectId());
            player.getInventory().addItem(createditem);
            player.sendPacket(SystemMessage2.obtainItems(tradeItem.getItemId(), tradeItem.getCount(), enchant));
        } else {
            player.sendMessage(player.isLangRus() ? "Похоже вы хотите меня обмануть!!!" : "It looks like you want to deceive me !!!");
        }
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        ShadowTradeItem tradeItem = itemsMap.get(player.getObjectId());
        NpcHtmlMessage html = tradeHolder.generateHtmlMassege(getObjectId(), tradeItem, enchant);
        MakeFString(1, getName(), player.getName());
        player.sendPacket(html);
    }

    @Override
    public void onAction(final Player player, final boolean shift) {
        final int objId = player.getObjectId();
        if (player.getPcBangPoints() >= 50) {
            itemsMap.remove(objId);
            itemsMap.put(objId, tradeHolder.getRndTradeItems());
            player.reducePcBangPoints(50);
            if (time.containsKey(objId)) {
                time.remove(objId);
            }
            long timestamp = 60 * 1000L;
            time.put(objId, System.currentTimeMillis() + timestamp);
            showChatWindow(player, 0, null);
            player.setTarget(this);
            player.sendActionFailed();
        } else {
            player.sendMessage(player.isLangRus() ? "У вас нет очков Pc Bang" : "You don have Pc Bang points");
            player.sendActionFailed();
        }

    }
}
