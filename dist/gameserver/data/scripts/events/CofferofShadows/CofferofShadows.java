package events.CofferofShadows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.npcdialog.INpcDialogAppender;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerEnterListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Util;

import java.util.Collections;
import java.util.List;

public class CofferofShadows extends Functions implements OnInitScriptListener, INpcDialogAppender {
    private static final Logger LOGGER = LoggerFactory.getLogger(CofferofShadows.class);
    private static final int COFFER_PRICE = 50000;
    private static final int COFFER_ID = 8659;
    private static final String EVENT_COFFER_SPAWN = "[event_coffer_of_shadows]";
    private static final int[] buycoffer_counts = {1, 5, 10, 50};
    private static boolean _active;

    private static boolean isActive() {
        return IsActive("CofferofShadows");
    }

    private void spawnEventManagers() {
        SpawnManager.getInstance().spawn(EVENT_COFFER_SPAWN);
    }

    private void unSpawnEventManagers() {
        SpawnManager.getInstance().despawn(EVENT_COFFER_SPAWN);
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("CofferofShadows", true)) {
            spawnEventManagers();
            System.out.println("Event: Coffer of Shadows started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.CofferofShadows.AnnounceEventStarted", null);
        } else {
            player.sendMessage("Event 'Coffer of Shadows' already started.");
        }
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("CofferofShadows", false)) {
            unSpawnEventManagers();
            System.out.println("Event: Coffer of Shadows stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.CofferofShadows.AnnounceEventStoped", null);
        } else {
            player.sendMessage("Event 'Coffer of Shadows' not started.");
        }
        _active = false;
        show("admin/events/events.htm", player);
    }

    public void buycoffer(final String[] var) {
        final Player player = getSelf();
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        if (!NpcInstance.canBypassCheck(player, player.getLastNpc())) {
            return;
        }
        int coffer_count = 1;
        try {
            coffer_count = Integer.valueOf(var[0]);
        } catch (Exception ignored) {
        }
        final long need_adena = (long) (COFFER_PRICE * Config.EVENT_CofferOfShadowsPriceRate * coffer_count);
        if (player.getAdena() < need_adena) {
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
        }
        player.reduceAdena(need_adena, true);
        Functions.addItem((Playable) player, COFFER_ID, (long) coffer_count);
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal((OnPlayerEnterListener) player -> {
            if (_active) {
                Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.CofferofShadows.AnnounceEventStarted", null);
            }
        });
        if (isActive()) {
            _active = true;
            spawnEventManagers();
            LOGGER.info("Loaded Event: Coffer of Shadows [state: activated]");
        } else {
            LOGGER.info("Loaded Event: Coffer of Shadows [state: deactivated]");
        }
    }

    @Override
    public String getAppend(Player player, NpcInstance npc, int val) {
        if (val != 0) {
            return "";
        }
        StringBuilder append = new StringBuilder();
        for (final int cnt : buycoffer_counts) {
            final String price = Util.formatAdena((long) (COFFER_PRICE * Config.EVENT_CofferOfShadowsPriceRate * cnt));
            append.append("<a action=\"bypass -h scripts_events.CofferofShadows.CofferofShadows:buycoffer ").append(cnt).append("\">");
            if (cnt == 1) {
                append.append(new CustomMessage("scripts.events.CofferofShadows.buycoffer", player).addString(price));
            } else {
                append.append(new CustomMessage("scripts.events.CofferofShadows.buycoffers", player).addNumber((long) cnt).addString(price));
            }
            append.append("</a><br>");
        }
        return append.toString();
    }

    @Override
    public List<Integer> getNpcIds() {
        return Collections.singletonList(32091);
    }
}
