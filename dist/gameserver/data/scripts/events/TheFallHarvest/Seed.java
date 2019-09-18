package events.TheFallHarvest;

import handler.items.ScriptItemHandler;
import npc.model.SquashInstance;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.SimpleSpawner;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

public class Seed extends ScriptItemHandler {
    private static final int[] _itemIds = {6389, 6390};
    private static final int[] _npcIds = {12774, 12777};

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        final Player activeChar = (Player) playable;
        if (activeChar.isInZone(ZoneType.RESIDENCE)) {
            return false;
        }
        if (activeChar.isOlyParticipant()) {
            activeChar.sendMessage("\u041d\u0435\u043b\u044c\u0437\u044f \u0432\u0437\u0440\u0430\u0449\u0438\u0432\u0430\u0442\u044c \u0442\u044b\u043a\u0432\u0443 \u043d\u0430 \u0441\u0442\u0430\u0434\u0438\u043e\u043d\u0435.");
            return false;
        }
        if (!activeChar.getReflection().isDefault()) {
            activeChar.sendMessage("\u041d\u0435\u043b\u044c\u0437\u044f \u0432\u0437\u0440\u0430\u0449\u0438\u0432\u0430\u0442\u044c \u0442\u044b\u043a\u0432\u0443 \u0432 \u0438\u043d\u0441\u0442\u0430\u043d\u0441\u0435.");
            return false;
        }
        NpcTemplate template = null;
        final int itemId = item.getItemId();
        for (int i = 0; i < _itemIds.length; ++i) {
            if (_itemIds[i] == itemId) {
                template = NpcTemplateHolder.getInstance().getTemplate(_npcIds[i]);
                break;
            }
        }
        if (template == null) {
            return false;
        }
        if (!activeChar.getInventory().destroyItem(item, 1L)) {
            return false;
        }
        final SimpleSpawner spawn = new SimpleSpawner(template);
        spawn.setLoc(Location.findPointToStay(activeChar, 30, 70));
        final NpcInstance npc = spawn.doSpawn(true);
        npc.setAI(new SquashAI(npc));
        ((SquashInstance) npc).setSpawner(activeChar);
        ThreadPoolManager.getInstance().schedule(new DeSpawnScheduleTimerTask(spawn), 180000L);
        return true;
    }

    @Override
    public int[] getItemIds() {
        return _itemIds;
    }

    public class DeSpawnScheduleTimerTask extends RunnableImpl {
        SimpleSpawner spawnedPlant;

        DeSpawnScheduleTimerTask(final SimpleSpawner spawn) {
            spawnedPlant = null;
            spawnedPlant = spawn;
        }

        @Override
        public void runImpl() {
            spawnedPlant.deleteAll();
        }
    }
}
