package events.StraightHands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class StraightHands extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(StraightHands.class);
    private static final String EVENT_NAME = "StraightHands";
    private static final List<Integer> _restrictedFuncHolder = new ArrayList<>();
    private static int[] _restrictedItemsId;
    private static boolean _active;

    private static boolean isActive() {
        return IsActive("StraightHands");
    }

    private static void removeRestrictedItemsStats() {
        for (final ItemTemplate item : ItemTemplateHolder.getInstance().getAllTemplates()) {
            if (item != null) {
                IntStream.of(_restrictedItemsId).filter(rid -> item.getItemId() == rid).forEach(rid -> {
                    item.setStatDisabled(true);
                    _restrictedFuncHolder.add(item.getItemId());
                    LOGGER.info("Event: StraightHands funcs of " + item.getName() + " removed.");
                });
            }
        }
    }

    private static void restoreRestrictedItemsStats() {
        _restrictedFuncHolder.stream().mapToInt(e -> e).mapToObj(e -> ItemTemplateHolder.getInstance().getTemplate(e)).forEach(item -> {
            if (item != null) {
                item.setStatDisabled(false);
            }
            LOGGER.info("Event: StraightHands funcs of " + item.getName() + " restored.");
        });
    }

    private static void unEquipRestrictedItems(final Player player) {
        player.getInventory().getItems().forEach(item -> Arrays.stream(_restrictedItemsId).filter(rid -> item.getItemId() == rid).forEach(rid -> {
            if (item.isEquipped()) {
                player.getInventory().unEquipItem(item);
            }
            player.sendMessage(new CustomMessage("scripts.events.StraightHands.ItemS1StatsRemoved", player).addItemName(rid));
        }));
    }

    private static void start() {
        _restrictedItemsId = Config.EVENT_StraightHands_Items.clone();
        removeRestrictedItemsStats();
        GameObjectsStorage.getAllPlayers().forEach(StraightHands::unEquipRestrictedItems);
    }

    private static void stop() {
        restoreRestrictedItemsStats();
    }

    public static void OnPlayerEnter(final Player player) {
        if (_active) {
            Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.StraightHands.AnnounceEventStarted", null);
            unEquipRestrictedItems(player);
        }
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("StraightHands", true)) {
            start();
            LOGGER.info("Event: StraightHands started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.StraightHands.AnnounceEventStarted", null);
        } else {
            player.sendMessage("Event StraightHands already started.");
        }
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("StraightHands", false)) {
            stop();
            LOGGER.info("Event: StraightHands stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.StraightHands.AnnounceEventStoped", null);
        } else {
            player.sendMessage("Event StraightHands not started.");
        }
        _active = false;
        show("admin/events/events.htm", player);
    }

    @Override
    public void onInit() {
        if (isActive()) {
            _active = true;
            start();
            LOGGER.info("Loaded Event: StraightHands [state: activated]");
        } else {
            LOGGER.info("Loaded Event: StraightHands [state: deactivated]");
        }
    }
}
