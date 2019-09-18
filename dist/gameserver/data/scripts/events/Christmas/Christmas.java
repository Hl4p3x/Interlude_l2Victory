package events.Christmas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerEnterListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;

public class Christmas extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger((Class) Christmas.class);
    private static final String CHRISTMAS_EVENT_MANAGER = "[christmas_event]";
    private static final String CHRISTMAS_EVENT_CHEST = "[christmas_event_chest]";
    private static final int[][] _dropdata = {{5556, 20}, {5557, 20}, {5558, 50}, {5559, 5}};
    private static boolean _active;

    private static boolean isActive() {
        return IsActive("Christmas");
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal(new ListenersImpl());
        if (isActive()) {
            _active = true;
            spawnEventManagers();
            LOGGER.info("Loaded Event: Christmas [state: activated]");
        } else {
            LOGGER.info("Loaded Event: Christmas [state: deactivated]");
        }
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("Christmas", true)) {
            spawnEventManagers();
            System.out.println("Event 'Christmas' started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.Christmas.AnnounceEventStarted", null);
        } else {
            player.sendMessage("Event 'Christmas' already started.");
        }
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("Christmas", false)) {
            unSpawnEventManagers();
            System.out.println("Event 'Christmas' stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.Christmas.AnnounceEventStoped", null);
        } else {
            player.sendMessage("Event 'Christmas' not started.");
        }
        _active = false;
        show("admin/events/events.htm", player);
    }

    private void spawnEventManagers() {
        SpawnManager.getInstance().spawn(CHRISTMAS_EVENT_CHEST);
        SpawnManager.getInstance().spawn(CHRISTMAS_EVENT_MANAGER);
    }

    private void unSpawnEventManagers() {
        SpawnManager.getInstance().despawn(CHRISTMAS_EVENT_MANAGER);
        SpawnManager.getInstance().despawn(CHRISTMAS_EVENT_CHEST);
    }

    private class ListenersImpl implements OnDeathListener, OnPlayerEnterListener {

        @Override
        public void onDeath(final Creature cha, final Creature killer) {
            if (_active && SimpleCheckDrop(cha, killer)) {
                int dropCounter = 0;
                for (final int[] drop : _dropdata) {
                    if (Rnd.chance(drop[1] * killer.getPlayer().getRateItems() * Config.EVENT_CHRISTMAS_CHANCE * 0.1)) {
                        ++dropCounter;
                        ((NpcInstance) cha).dropItem(killer.getPlayer(), drop[0], 1L);
                        if (dropCounter > 2) {
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public void onPlayerEnter(final Player player) {
            if (_active) {
                Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Christmas.AnnounceEventStarted", null);
            }
        }
    }

    public void exchange(final String[] var) {
        final Player player = getSelf();
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        if (player.isActionsDisabled() || player.isSitting() || player.getLastNpc() == null || player.getLastNpc().getDistance(player) > 300.0) {
            return;
        }
        if (var[0].equalsIgnoreCase("0")) {
            if (getItemCount((Playable) player, 5556) >= 4L && getItemCount((Playable) player, 5557) >= 4L && getItemCount((Playable) player, 5558) >= 10L && getItemCount((Playable) player, 5559) >= 1L) {
                removeItem(player, 5556, 4L);
                removeItem(player, 5557, 4L);
                removeItem(player, 5558, 10L);
                removeItem(player, 5559, 1L);
                addItem((Playable) player, 5560, 1L);
                return;
            }
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
        }
        if (var[0].equalsIgnoreCase("1")) {
            if (getItemCount((Playable) player, 5560) >= 10L) {
                removeItem(player, 5560, 10L);
                addItem((Playable) player, 5561, 1L);
                return;
            }
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
        }
        if (var[0].equalsIgnoreCase("2")) {
            if (getItemCount((Playable) player, 5560) >= 10L) {
                removeItem(player, 5560, 10L);
                addItem((Playable) player, 7836, 1L);
                return;
            }
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
        }
        if (var[0].equalsIgnoreCase("3")) {
            if (getItemCount((Playable) player, 5560) >= 10L) {
                removeItem(player, 5560, 10L);
                addItem((Playable) player, 8936, 1L);
                return;
            }
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
        }
    }

}
