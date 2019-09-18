package events.TrickOfTrans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
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

public class TrickOfTrans extends Functions implements OnInitScriptListener{
    private static final Logger LOGGER = LoggerFactory.getLogger((Class) TrickOfTrans.class);
    private static final String TRICK_OF_TRANS_SPAWN = "[trick_of_trans_spawn]";
    private static final int RED_PSTC = 9162;
    private static final int BLUE_PSTC = 9163;
    private static final int ORANGE_PSTC = 9164;
    private static final int BLACK_PSTC = 9165;
    private static final int WHITE_PSTC = 9166;
    private static final int GREEN_PSTC = 9167;
    private static final int RED_PSTC_R = 9171;
    private static final int BLUE_PSTC_R = 9172;
    private static final int ORANGE_PSTC_R = 9173;
    private static final int BLACK_PSTC_R = 9174;
    private static final int WHITE_PSTC_R = 9175;
    private static final int GREEN_PSTC_R = 9176;
    private static final int A_CHEST_KEY = 9205;
    private static final int PhilosophersStoneOre = 9168;
    private static final int PhilosophersStoneOreMax = 17;
    private static final int PhilosophersStoneConversionFormula = 9169;
    private static final int MagicReagents = 9170;
    private static final int MagicReagentsMax = 30;
    private static boolean _active;

    private static boolean isActive() {
        return IsActive("trickoftrans");
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal(new ListenersImpl());
        if (isActive()) {
            _active = true;
            spawnEventManagers();
            LOGGER.info("Loaded Event: Trick of Trnasmutation [state: activated]");
        } else {
            LOGGER.info("Loaded Event: Trick of Trnasmutation [state: deactivated]");
        }
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("trickoftrans", true)) {
            spawnEventManagers();
            System.out.println("Event 'Trick of Transmutation' started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.TrickOfTrans.AnnounceEventStarted", null);
        } else {
            player.sendMessage("Event 'Trick of Transmutation' already started.");
        }
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("trickoftrans", false)) {
            unSpawnEventManagers();
            System.out.println("Event 'Trick of Transmutation' stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.TrickOfTrans.AnnounceEventStoped", null);
        } else {
            player.sendMessage("Event 'Trick of Transmutation' not started.");
        }
        _active = false;
        show("admin/events/events.htm", player);
    }

    private class ListenersImpl implements OnDeathListener, OnPlayerEnterListener {

        @Override
        public void onPlayerEnter(final Player player) {
            if (_active) {
                Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.TrickOfTrans.AnnounceEventStarted", null);
            }
        }

        @Override
        public void onDeath(final Creature cha, final Creature killer) {
            if (_active && SimpleCheckDrop(cha, killer) && Rnd.chance(Config.EVENT_TRICK_OF_TRANS_CHANCE * killer.getPlayer().getRateItems() * Config.RATE_DROP_ITEMS * ((NpcInstance) cha).getTemplate().rateHp)) {
                ((NpcInstance) cha).dropItem(killer.getPlayer(), A_CHEST_KEY, 1L);
            }
        }
    }

    private void spawnEventManagers() {
        SpawnManager.getInstance().spawn(TRICK_OF_TRANS_SPAWN);
    }

    private void unSpawnEventManagers() {
        SpawnManager.getInstance().despawn(TRICK_OF_TRANS_SPAWN);
    }


    public void accept() {
        final Player player = getSelf();
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        if (!player.findRecipe(RED_PSTC_R)) {
            addItem((Playable) player, RED_PSTC, 1L);
        }
        if (!player.findRecipe(BLACK_PSTC_R)) {
            addItem((Playable) player, BLACK_PSTC, 1L);
        }
        if (!player.findRecipe(BLUE_PSTC_R)) {
            addItem((Playable) player, BLUE_PSTC, 1L);
        }
        if (!player.findRecipe(GREEN_PSTC_R)) {
            addItem((Playable) player, GREEN_PSTC, 1L);
        }
        if (!player.findRecipe(ORANGE_PSTC_R)) {
            addItem((Playable) player, ORANGE_PSTC, 1L);
        }
        if (!player.findRecipe(WHITE_PSTC_R)) {
            addItem((Playable) player, WHITE_PSTC, 1L);
        }
        show("scripts/events/TrickOfTrans/TrickOfTrans_01.htm", player);
    }

    public void open() {
        final Player player = getSelf();
        if (getItemCount((Playable) player, A_CHEST_KEY) > 0L) {
            removeItem(player, A_CHEST_KEY, 1L);
            addItem((Playable) player, PhilosophersStoneOre, (long) Rnd.get(1, PhilosophersStoneOreMax));
            addItem((Playable) player, MagicReagents, (long) Rnd.get(1, MagicReagentsMax));
            if (Rnd.chance(80)) {
                addItem((Playable) player, PhilosophersStoneConversionFormula, 1L);
            }
            show("scripts/events/TrickOfTrans/TrickOfTrans_02.htm", player);
        } else {
            show("scripts/events/TrickOfTrans/TrickOfTrans_03.htm", player);
        }
    }

}
