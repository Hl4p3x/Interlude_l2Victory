package events.l2day;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.handler.npcdialog.INpcDialogAppender;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerEnterListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.reward.RewardData;
import ru.j2dev.gameserver.model.reward.RewardGroup;
import ru.j2dev.gameserver.model.reward.RewardList;
import ru.j2dev.gameserver.model.reward.RewardType;
import ru.j2dev.gameserver.scripts.Functions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class LettersCollection extends Functions implements INpcDialogAppender, OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(LettersCollection.class);
    private static final String EVENT_LETTER_COLLECTION_SPAWN = "[event_letter_collection_spawn]";
    private static final RewardList DROP_DATA;
    protected static boolean _active;
    protected static String _name;
    protected static int[][] letters;
    protected static String _msgStarted;
    protected static String _msgEnded;
    protected static int A = 3875;
    protected static int C = 3876;
    protected static int E = 3877;
    protected static int F = 3878;
    protected static int G = 3879;
    protected static int H = 3880;
    protected static int I = 3881;
    protected static int L = 3882;
    protected static int N = 3883;
    protected static int O = 3884;
    protected static int R = 3885;
    protected static int S = 3886;
    protected static int T = 3887;
    protected static int II = 3888;
    protected static Map<String, Integer[][]> _words = new HashMap<>();
    protected static Map<String, RewardData[]> _rewards = new HashMap<>();

    protected static boolean isActive() {
        return IsActive(_name);
    }
    static {
        final RewardGroup eventDrop = new RewardGroup(150000.);
        eventDrop.setNotRate(true);
        eventDrop.addData(new RewardData(A, 1, 1, 200000.));
        eventDrop.addData(new RewardData(C, 1, 1, 200000.));
        eventDrop.addData(new RewardData(E, 1, 1, 450000.));
        eventDrop.addData(new RewardData(F, 1, 1, 50000.));
        eventDrop.addData(new RewardData(G, 1, 1, 20000.));
        eventDrop.addData(new RewardData(H, 1, 1, 40000.));
        eventDrop.addData(new RewardData(I, 1, 1, 20000.));
        eventDrop.addData(new RewardData(L, 1, 1, 20000.));
        eventDrop.addData(new RewardData(N, 1, 1, 20000.));
        eventDrop.addData(new RewardData(O, 1, 1, 20000.));
        eventDrop.addData(new RewardData(R, 1, 1, 20000.));
        eventDrop.addData(new RewardData(S, 1, 1, 20000.));
        eventDrop.addData(new RewardData(T, 1, 1, 20000.));
        eventDrop.addData(new RewardData(II, 1, 1, 20000.));

        DROP_DATA = new RewardList(RewardType.EVENT, true);
        DROP_DATA.add(eventDrop);
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal(new OnPlayerEnterListenerImpl());
        if (isActive()) {
            _active = true;
            spawnEventManagers();
            LOGGER.info("Loaded Event: " + _name + " [state: activated]");
        } else {
            LOGGER.info("Loaded Event: " + _name + " [state: deactivated]");
        }
    }

    protected void spawnEventManagers() {
        SpawnManager.getInstance().spawn(EVENT_LETTER_COLLECTION_SPAWN);
    }

    protected void unSpawnEventManagers() {
        SpawnManager.getInstance().despawn(EVENT_LETTER_COLLECTION_SPAWN);
    }

    private class OnPlayerEnterListenerImpl implements OnPlayerEnterListener {

        @Override
        public void onPlayerEnter(final Player player) {
            if (_active) {
                Announcements.getInstance().announceToPlayerByCustomMessage(player, _msgStarted, null);
            }
        }
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive(_name, true)) {
            spawnEventManagers();
            LOGGER.info("Event '" + _name + "' started.");
            Announcements.getInstance().announceByCustomMessage(_msgStarted, null);
        } else {
            player.sendMessage("Event '" + _name + "' already started.");
        }
        NpcTemplateHolder.getInstance().addEventDrop(DROP_DATA);
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive(_name, false)) {
            unSpawnEventManagers();
            LOGGER.info("Event '" + _name + "' stopped.");
            Announcements.getInstance().announceByCustomMessage(_msgEnded, null);
        } else {
            player.sendMessage("Event '" + _name + "' not started.");
        }
        NpcTemplateHolder.getInstance().removeEventDrop();
        _active = false;
        show("admin/events/events.htm", player);
    }

    public void exchange(final String[] var) {
        final Player player = getSelf();
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        if (!NpcInstance.canBypassCheck(player, player.getLastNpc())) {
            return;
        }
        final Integer[][] mss = _words.get(var[0]);
        for (final Integer[] l : mss) {
            if (getItemCount((Playable) player, l[0]) < l[1]) {
                player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                return;
            }
        }
        Stream.of(mss).forEach(l -> removeItem(player, l[0], (long) l[1]));
        final RewardData[] rewards = _rewards.get(var[0]);
        int sum = Stream.of(rewards).mapToInt(r -> (int) r.getChance()).sum();
        final int random = Rnd.get(sum);
        sum = 0;
        for (final RewardData r2 : rewards) {
            sum += (int) r2.getChance();
            if (sum > random) {
                addItem((Playable) player, r2.getItemId(), Rnd.get(r2.getMinDrop(), r2.getMaxDrop()));
                return;
            }
        }
    }

    @Override
    public String getAppend(Player player, NpcInstance npc, int val) {
        if (val != 0) {
            return "";
        }
        if (!_active) {
            return "";
        }
        final StringBuilder append = new StringBuilder("<br><br>");
        for (final String word : _words.keySet()) {
            append.append("[scripts_").append(getClass().getName()).append(":exchange ").append(word).append("|").append(word).append("]<br1>");
        }
        return append.toString();
    }

    @Override
    public List<Integer> getNpcIds() {
        return Collections.singletonList(31230);
    }

}
