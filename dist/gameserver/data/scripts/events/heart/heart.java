package events.heart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.text.PrintfFormat;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerEnterListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class heart extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(heart.class);
    private static final Map<Integer, Integer> Guesses = new HashMap<>();
    private static final String[][] variants = {{"Rock", "\u041a\u0430\u043c\u0435\u043d\u044c"}, {"Scissors", "\u041d\u043e\u0436\u043d\u0438\u0446\u044b"}, {"Paper", "\u0411\u0443\u043c\u0430\u0433\u0430"}};
    private static final String HEART_EVENT_SPAWN_LIST = "[event_heart_spawn]";
    private static final int[] hearts = {4209, 4210, 4211, 4212, 4213, 4214, 4215, 4216, 4217};
    private static final int[] potions = {1374, 1375, 6036, 1539};
    private static final int[] scrolls = {3926, 3927, 3928, 3929, 3930, 3931, 3932, 3933, 3934, 3935};
    private static boolean _active;
    private static String links_en;
    private static String links_ru;

    static {
        final PrintfFormat fmt = new PrintfFormat("<br><a action=\"bypass -h scripts_events.heart.heart:play %d\">\"%s!\"</a>");
        IntStream.range(0, variants.length).forEach(i -> {
            links_en += fmt.sprintf(i, variants[i][0]);
            links_ru += fmt.sprintf(i, variants[i][1]);
        });
    }

    private static boolean isActive() {
        return IsActive("heart");
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("heart", true)) {
            spawnEventManagers();
            System.out.println("Event 'Change of Heart' started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.ChangeofHeart.AnnounceEventStarted", null);
        } else {
            player.sendMessage("Event 'Change of Heart' already started.");
        }
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("heart", false)) {
            unSpawnEventManagers();
            System.out.println("Event 'Change of Heart' stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.ChangeofHeart.AnnounceEventStoped", null);
        } else {
            player.sendMessage("Event 'Change of Heart' not started.");
        }
        _active = false;
        show("admin/events/events.htm", player);
    }

    public void letsplay() {
        final Player player = getSelf();
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        zeroGuesses(player);
        if (haveAllHearts(player)) {
            show(link(HtmCache.getInstance().getNotNull("scripts/events/heart/hearts_01.htm", player), isRus(player)), player);
        } else {
            show("scripts/events/heart/hearts_00.htm", player);
        }
    }

    public void play(final String[] var) {
        final Player player = getSelf();
        if (!player.isQuestContinuationPossible(true) || var.length == 0) {
            return;
        }
        if (!haveAllHearts(player)) {
            if (var[0].equalsIgnoreCase("Quit")) {
                show("scripts/events/heart/hearts_00b.htm", player);
            } else {
                show("scripts/events/heart/hearts_00a.htm", player);
            }
            return;
        }
        if (var[0].equalsIgnoreCase("Quit")) {
            final int curr_guesses = getGuesses(player);
            takeHeartsSet(player);
            reward(player, curr_guesses);
            show("scripts/events/heart/hearts_reward_" + curr_guesses + ".htm", player);
            zeroGuesses(player);
            return;
        }
        final int var_cat = Rnd.get(variants.length);
        int var_player = 0;
        try {
            var_player = Integer.parseInt(var[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (var_player == var_cat) {
            show(fillvars(HtmCache.getInstance().getNotNull("scripts/events/heart/hearts_same.htm", player), var_player, var_cat, player), player);
            return;
        }
        if (playerWins(var_player, var_cat)) {
            incGuesses(player);
            final int curr_guesses2 = getGuesses(player);
            if (curr_guesses2 == 10) {
                takeHeartsSet(player);
                reward(player, curr_guesses2);
                zeroGuesses(player);
            }
            show(fillvars(HtmCache.getInstance().getNotNull("scripts/events/heart/hearts_level_" + curr_guesses2 + ".htm", player), var_player, var_cat, player), player);
            return;
        }
        takeHeartsSet(player);
        reward(player, getGuesses(player) - 1);
        show(fillvars(HtmCache.getInstance().getNotNull("scripts/events/heart/hearts_loose.htm", player), var_player, var_cat, player), player);
        zeroGuesses(player);
    }

    private void reward(final Player player, final int guesses) {
        switch (guesses) {
            case -1:
            case 0: {
                addItem((Playable) player, scrolls[Rnd.get(scrolls.length)], 1L);
                break;
            }
            case 1: {
                addItem((Playable) player, potions[Rnd.get(potions.length)], 10L);
                break;
            }
            case 2: {
                addItem((Playable) player, 1538, 1L);
                break;
            }
            case 3: {
                addItem((Playable) player, 3936, 1L);
                break;
            }
            case 4: {
                addItem((Playable) player, 951, 2L);
                break;
            }
            case 5: {
                addItem((Playable) player, 948, 4L);
                break;
            }
            case 6: {
                addItem((Playable) player, 947, 1L);
                break;
            }
            case 7: {
                addItem((Playable) player, 730, 3L);
                break;
            }
            case 8: {
                addItem((Playable) player, 729, 1L);
                break;
            }
            case 9: {
                addItem((Playable) player, 960, 2L);
                break;
            }
            case 10: {
                addItem((Playable) player, 959, 1L);
                break;
            }
        }
    }

    private String fillvars(final String s, final int var_player, final int var_cat, final Player player) {
        final boolean rus = isRus(player);
        return link(s.replaceFirst("Player", player.getName()).replaceFirst("%var_payer%", variants[var_player][rus ? 1 : 0]).replaceFirst("%var_cat%", variants[var_cat][rus ? 1 : 0]), rus);
    }

    private boolean isRus(final Player player) {
        return player.isLangRus();
    }

    private String link(final String s, final boolean rus) {
        return s.replaceFirst("%links%", rus ? links_ru : links_en);
    }

    private boolean playerWins(final int var_player, final int var_cat) {
        if (var_player == 0) {
            return var_cat == 1;
        }
        if (var_player == 1) {
            return var_cat == 2;
        }
        return var_player == 2 && var_cat == 0;
    }

    private int getGuesses(final Player player) {
        return Guesses.getOrDefault(player.getObjectId(), 0);
    }

    private void incGuesses(final Player player) {
        int val = 1;
        if (Guesses.containsKey(player.getObjectId())) {
            val = Guesses.remove(player.getObjectId()) + 1;
        }
        Guesses.put(player.getObjectId(), val);
    }

    private void zeroGuesses(final Player player) {
        Guesses.remove(player.getObjectId());
    }

    private void takeHeartsSet(final Player player) {
        IntStream.of(hearts).forEach(heart_id -> removeItem(player, heart_id, 1L));
    }

    private boolean haveAllHearts(final Player player) {
        return IntStream.of(hearts).noneMatch(heart_id -> player.getInventory().getCountOf(heart_id) < 1L);
    }

    private class Listeners implements OnDeathListener, OnPlayerEnterListener {

        @Override
        public void onDeath(final Creature cha, final Creature killer) {
            if (_active && SimpleCheckDrop(cha, killer)) {
                ((NpcInstance) cha).dropItem(killer.getPlayer(), hearts[Rnd.get(hearts.length)], Util.rollDrop(1L, 1L, Config.EVENT_CHANGE_OF_HEART_CHANCE * killer.getPlayer().getRateItems() * ((MonsterInstance) cha).getTemplate().rateHp * 10000.0, true));
            }
        }

        @Override
        public void onPlayerEnter(final Player player) {
            if (_active) {
                Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.ChangeofHeart.AnnounceEventStarted", null);
            }
        }
    }

    private void spawnEventManagers() {
        SpawnManager.getInstance().spawn(HEART_EVENT_SPAWN_LIST);
    }

    private void unSpawnEventManagers() {
        SpawnManager.getInstance().despawn(HEART_EVENT_SPAWN_LIST);
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal(new Listeners());
        if (isActive()) {
            _active = true;
            spawnEventManagers();
            LOGGER.info("Loaded Event: Change of Heart [state: activated]");
        } else {
            LOGGER.info("Loaded Event: Change of Heart[state: deactivated]");
        }
    }
}
