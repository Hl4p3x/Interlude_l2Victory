package events.CofferofShadows;

import handler.items.ScriptItemHandler;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.reward.RewardData;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Coffer extends ScriptItemHandler {
    protected static final RewardData[] _dropmats = {new RewardData(4041, 1L, 1L, 250.0), new RewardData(4042, 1L, 1L, 450.0), new RewardData(4040, 1L, 1L, 500.0), new RewardData(1890, 1L, 3L, 833.0), new RewardData(5550, 1L, 3L, 833.0), new RewardData(4039, 1L, 1L, 833.0), new RewardData(4043, 1L, 1L, 833.0), new RewardData(4044, 1L, 1L, 833.0), new RewardData(1888, 1L, 3L, 1000.0), new RewardData(1877, 1L, 3L, 1000.0), new RewardData(1894, 1L, 3L, 3000.0), new RewardData(1874, 1L, 5L, 3000.0), new RewardData(1875, 1L, 5L, 3000.0), new RewardData(1887, 1L, 3L, 3000.0), new RewardData(1866, 1L, 10L, 16666.0), new RewardData(1882, 1L, 10L, 16666.0), new RewardData(1881, 1L, 10L, 10000.0), new RewardData(1873, 1L, 10L, 10000.0), new RewardData(1879, 1L, 5L, 10000.0), new RewardData(1880, 1L, 5L, 10000.0), new RewardData(1876, 1L, 5L, 10000.0), new RewardData(1864, 1L, 20L, 25000.0), new RewardData(1865, 1L, 20L, 25000.0), new RewardData(1868, 1L, 15L, 25000.0), new RewardData(1869, 1L, 15L, 25000.0), new RewardData(1870, 1L, 15L, 25000.0), new RewardData(1871, 1L, 15L, 25000.0), new RewardData(1872, 1L, 20L, 30000.0), new RewardData(1867, 1L, 20L, 33333.0)};
    protected static final RewardData[] _dropacc = {new RewardData(8660, 1L, 1L, 1000.0), new RewardData(8661, 1L, 1L, 1000.0), new RewardData(4393, 1L, 1L, 300.0), new RewardData(5590, 1L, 1L, 200.0), new RewardData(7058, 1L, 1L, 50.0), new RewardData(8350, 1L, 1L, 50.0), new RewardData(5133, 1L, 1L, 50.0), new RewardData(5817, 1L, 1L, 50.0), new RewardData(9140, 1L, 1L, 30.0), new RewardData(9177, 1L, 1L, 100.0), new RewardData(9178, 1L, 1L, 100.0), new RewardData(9179, 1L, 1L, 100.0), new RewardData(9180, 1L, 1L, 100.0), new RewardData(9181, 1L, 1L, 100.0), new RewardData(9182, 1L, 1L, 100.0), new RewardData(9183, 1L, 1L, 100.0), new RewardData(9184, 1L, 1L, 100.0), new RewardData(9185, 1L, 1L, 100.0), new RewardData(9186, 1L, 1L, 100.0), new RewardData(9187, 1L, 1L, 100.0), new RewardData(9188, 1L, 1L, 100.0), new RewardData(9189, 1L, 1L, 100.0), new RewardData(9190, 1L, 1L, 100.0), new RewardData(9191, 1L, 1L, 100.0), new RewardData(9192, 1L, 1L, 100.0), new RewardData(9193, 1L, 1L, 100.0), new RewardData(9194, 1L, 1L, 100.0), new RewardData(9195, 1L, 1L, 100.0), new RewardData(9196, 1L, 1L, 100.0), new RewardData(9197, 1L, 1L, 100.0), new RewardData(9198, 1L, 1L, 100.0), new RewardData(9199, 1L, 1L, 100.0)};
    protected static final RewardData[] _dropevents = {new RewardData(9146, 1L, 1L, 3000.0), new RewardData(9147, 1L, 1L, 3000.0), new RewardData(9148, 1L, 1L, 3000.0), new RewardData(9149, 1L, 1L, 3000.0), new RewardData(9150, 1L, 1L, 3000.0), new RewardData(9151, 1L, 1L, 3000.0), new RewardData(9152, 1L, 1L, 3000.0), new RewardData(9153, 1L, 1L, 3000.0), new RewardData(9154, 1L, 1L, 3000.0), new RewardData(9155, 1L, 1L, 3000.0), new RewardData(9156, 1L, 1L, 2000.0), new RewardData(9157, 1L, 1L, 1000.0), new RewardData(5234, 1L, 5L, 25000.0), new RewardData(7609, 50L, 100L, 24000.0), new RewardData(7562, 2L, 4L, 10000.0), new RewardData(6415, 1L, 3L, 20000.0), new RewardData(1461, 1L, 3L, 15000.0), new RewardData(6406, 1L, 3L, 20000.0), new RewardData(6407, 1L, 1L, 20000.0), new RewardData(6403, 1L, 5L, 20000.0), new RewardData(6036, 1L, 5L, 30000.0), new RewardData(5595, 1L, 1L, 21000.0), new RewardData(1374, 1L, 5L, 20000.0), new RewardData(1375, 1L, 5L, 20000.0), new RewardData(1540, 1L, 3L, 20000.0), new RewardData(5126, 1L, 1L, 1000.0)};
    protected static final RewardData[] _dropench = {new RewardData(955, 1L, 1L, 400.0), new RewardData(956, 1L, 1L, 2000.0), new RewardData(951, 1L, 1L, 300.0), new RewardData(952, 1L, 1L, 1500.0), new RewardData(947, 1L, 1L, 200.0), new RewardData(948, 1L, 1L, 1000.0), new RewardData(729, 1L, 1L, 100.0), new RewardData(730, 1L, 1L, 500.0), new RewardData(959, 1L, 1L, 50.0), new RewardData(960, 1L, 1L, 300.0), new RewardData(5577, 1L, 1L, 90.0), new RewardData(5578, 1L, 1L, 90.0), new RewardData(5579, 1L, 1L, 90.0), new RewardData(5580, 1L, 1L, 70.0), new RewardData(5581, 1L, 1L, 70.0), new RewardData(5582, 1L, 1L, 70.0)};
    private static final int[] _itemIds = {8659};

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (!playable.isPlayer()) {
            return false;
        }
        final Player activeChar = playable.getPlayer();
        if (!activeChar.isQuestContinuationPossible(true)) {
            return false;
        }
        final Map<Integer, Long> items = new HashMap<>();
        long count = 0L;
        do {
            ++count;
            getGroupItem(activeChar, _dropmats, items);
            getGroupItem(activeChar, _dropacc, items);
            getGroupItem(activeChar, _dropevents, items);
            getGroupItem(activeChar, _dropench, items);
        } while (ctrl && item.getCount() > count && activeChar.isQuestContinuationPossible(false));
        activeChar.getInventory().destroyItem(item, count);
        activeChar.sendPacket(SystemMessage2.removeItems(item.getItemId(), count));
        for (final Entry<Integer, Long> e : items.entrySet()) {
            activeChar.sendPacket(SystemMessage2.obtainItems(e.getKey(), e.getValue(), 0));
        }
        return true;
    }

    public void getGroupItem(final Player activeChar, final RewardData[] dropData, final Map<Integer, Long> report) {
        long count;
        for (final RewardData d : dropData) {
            if (Rnd.get(1, 1000000) <= d.getChance() * Config.EVENT_CofferOfShadowsRewardRate) {
                count = Rnd.get(d.getMinDrop(), d.getMaxDrop());
                final ItemInstance item = ItemFunctions.createItem(d.getItemId());
                item.setCount(count);
                activeChar.getInventory().addItem(item);
                final Long old = report.get(d.getItemId());
                report.put(d.getItemId(), (old != null) ? (old + count) : count);
            }
        }
    }

    @Override
    public final int[] getItemIds() {
        return _itemIds;
    }
}
