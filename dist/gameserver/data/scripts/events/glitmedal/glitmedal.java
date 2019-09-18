package events.glitmedal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.MultiSellHolder;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerEnterListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Util;

import java.io.File;

public class glitmedal extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(glitmedal.class);
    private static final String EVENT_GLITMEDAL_ROY = "[event_giltmedal_roy_spawn]";
    private static final String EVENT_GLITMEDAL_WINNIE = "[event_giltmedal_winnie_spawn]";
    private static final int EVENT_MEDAL = 6392;
    private static final int EVENT_GLITTMEDAL = 6393;
    private static final int Badge_of_Rabbit = 6399;
    private static final int Badge_of_Hyena = 6400;
    private static final int Badge_of_Fox = 6401;
    private static final int Badge_of_Wolf = 6402;
    private static final File[] multiSellFiles = {new File(Config.DATAPACK_ROOT, "data/html/en/scripts/events/glitmedal/502.xml"), new File(Config.DATAPACK_ROOT, "data/html/en/scripts/events/glitmedal/503.xml"), new File(Config.DATAPACK_ROOT, "data/html/en/scripts/events/glitmedal/504.xml"), new File(Config.DATAPACK_ROOT, "data/html/en/scripts/events/glitmedal/505.xml"), new File(Config.DATAPACK_ROOT, "data/html/en/scripts/events/glitmedal/506.xml")};
    private static boolean _active;
    private static boolean MultiSellLoaded;
    private int isTalker;

    private static boolean isActive() {
        return IsActive("glitter");
    }

    private static void loadMultiSell() {
        if (MultiSellLoaded) {
            return;
        }
        for (final File f : multiSellFiles) {
            MultiSellHolder.getInstance().parseFile(f);
        }
        MultiSellLoaded = true;
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal(new OnPlayerEnterListenerImpl());
        CharListenerList.addGlobal(new OnDeathListenerImpl());
        if (isActive()) {
            _active = true;
            loadMultiSell();
            spawnEventManagers();
            LOGGER.info("Loaded Event: L2 Medal Collection Event [state: activated]");
        } else {
            LOGGER.info("Loaded Event: L2 Medal Collection Event [state: deactivated]");
        }
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("glitter", true)) {
            loadMultiSell();
            spawnEventManagers();
            System.out.println("Event 'L2 Medal Collection Event' started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.glitmedal.AnnounceEventStarted", null);
        } else {
            player.sendMessage("Event 'L2 Medal Collection Event' already started.");
        }
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("glitter", false)) {
            unSpawnEventManagers();
            System.out.println("Event 'L2 Medal Collection Event' stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.glitmedal.AnnounceEventStoped", null);
        } else {
            player.sendMessage("Event 'L2 Medal Collection Event' not started.");
        }
        _active = false;
        show("admin/events/events.htm", player);
    }

    private class OnPlayerEnterListenerImpl implements OnPlayerEnterListener {

        @Override
        public void onPlayerEnter(final Player player) {
            if (_active) {
                Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.glitmedal.AnnounceEventStarted", null);
            }
        }
    }

    private void spawnEventManagers() {
        SpawnManager.getInstance().spawn(EVENT_GLITMEDAL_ROY);
        SpawnManager.getInstance().spawn(EVENT_GLITMEDAL_WINNIE);
    }

    private void unSpawnEventManagers() {
        SpawnManager.getInstance().despawn(EVENT_GLITMEDAL_ROY);
        SpawnManager.getInstance().despawn(EVENT_GLITMEDAL_WINNIE);
    }

    private class OnDeathListenerImpl implements OnDeathListener {

        @Override
        public void onDeath(final Creature cha, final Creature killer) {
            if (_active && SimpleCheckDrop(cha, killer)) {
                final long count = Util.rollDrop(1L, 1L, Config.EVENT_GLITTMEDAL_NORMAL_CHANCE * killer.getPlayer().getRateItems() * ((MonsterInstance) cha).getTemplate().rateHp * 10000.0, true);
                if (count > 0L) {
                    addItem((Playable) killer.getPlayer(), EVENT_MEDAL, count);
                }
                if (killer.getPlayer().getInventory().getCountOf(Badge_of_Wolf) == 0L && Rnd.chance(Config.EVENT_GLITTMEDAL_GLIT_CHANCE * killer.getPlayer().getRateItems() * ((MonsterInstance) cha).getTemplate().rateHp)) {
                    addItem((Playable) killer.getPlayer(), EVENT_GLITTMEDAL, 1L);
                }
            }
        }
    }

    public void glitchang() {
        final Player player = getSelf();
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        if (getItemCount((Playable) player, EVENT_MEDAL) >= 1000L) {
            removeItem(player, EVENT_MEDAL, 1000L);
            addItem((Playable) player, EVENT_GLITTMEDAL, 10L);
            return;
        }
        player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
    }

    public void medal() {
        final Player player = getSelf();
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Wolf) >= 1L) {
            show("scripts/events/glitmedal/event_col_agent1_q0996_05.htm", player);
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Fox) >= 1L) {
            show("scripts/events/glitmedal/event_col_agent1_q0996_04.htm", player);
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Hyena) >= 1L) {
            show("scripts/events/glitmedal/event_col_agent1_q0996_03.htm", player);
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Rabbit) >= 1L) {
            show("scripts/events/glitmedal/event_col_agent1_q0996_02.htm", player);
            return;
        }
        show("scripts/events/glitmedal/event_col_agent1_q0996_01.htm", player);
    }

    public void medalb() {
        final Player player = getSelf();
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Wolf) >= 1L) {
            show("scripts/events/glitmedal/event_col_agent2_q0996_05.htm", player);
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Fox) >= 1L) {
            show("scripts/events/glitmedal/event_col_agent2_q0996_04.htm", player);
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Hyena) >= 1L) {
            show("scripts/events/glitmedal/event_col_agent2_q0996_03.htm", player);
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Rabbit) >= 1L) {
            show("scripts/events/glitmedal/event_col_agent2_q0996_02.htm", player);
            return;
        }
        show("scripts/events/glitmedal/event_col_agent2_q0996_01.htm", player);
    }

    public void game() {
        final Player player = getSelf();
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Fox) >= 1L) {
            if (getItemCount((Playable) player, EVENT_GLITTMEDAL) >= 40L) {
                show("scripts/events/glitmedal/event_col_agent2_q0996_11.htm", player);
                return;
            }
            show("scripts/events/glitmedal/event_col_agent2_q0996_12.htm", player);
        } else if (getItemCount((Playable) player, Badge_of_Hyena) >= 1L) {
            if (getItemCount((Playable) player, EVENT_GLITTMEDAL) >= 20L) {
                show("scripts/events/glitmedal/event_col_agent2_q0996_11.htm", player);
                return;
            }
            show("scripts/events/glitmedal/event_col_agent2_q0996_12.htm", player);
        } else if (getItemCount((Playable) player, Badge_of_Rabbit) >= 1L) {
            if (getItemCount((Playable) player, EVENT_GLITTMEDAL) >= 10L) {
                show("scripts/events/glitmedal/event_col_agent2_q0996_11.htm", player);
                return;
            }
            show("scripts/events/glitmedal/event_col_agent2_q0996_12.htm", player);
        } else {
            if (getItemCount((Playable) player, EVENT_GLITTMEDAL) >= 5L) {
                show("scripts/events/glitmedal/event_col_agent2_q0996_11.htm", player);
                return;
            }
            show("scripts/events/glitmedal/event_col_agent2_q0996_12.htm", player);
        }
    }

    public void gamea() {
        final Player player = getSelf();
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        isTalker = Rnd.get(2);
        if (getItemCount((Playable) player, Badge_of_Fox) >= 1L) {
            if (getItemCount((Playable) player, EVENT_GLITTMEDAL) >= 40L) {
                if (isTalker == 1) {
                    removeItem(player, Badge_of_Fox, 1L);
                    removeItem(player, EVENT_GLITTMEDAL, getItemCount((Playable) player, EVENT_GLITTMEDAL));
                    addItem((Playable) player, Badge_of_Wolf, 1L);
                    show("scripts/events/glitmedal/event_col_agent2_q0996_24.htm", player);
                    return;
                }
                if (isTalker == 0) {
                    removeItem(player, EVENT_GLITTMEDAL, 40L);
                    show("scripts/events/glitmedal/event_col_agent2_q0996_25.htm", player);
                    return;
                }
            }
            show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Hyena) >= 1L) {
            if (getItemCount((Playable) player, EVENT_GLITTMEDAL) >= 20L) {
                if (isTalker == 1) {
                    removeItem(player, Badge_of_Hyena, 1L);
                    removeItem(player, EVENT_GLITTMEDAL, 20L);
                    addItem((Playable) player, Badge_of_Fox, 1L);
                    show("scripts/events/glitmedal/event_col_agent2_q0996_23.htm", player);
                    return;
                }
                if (isTalker == 0) {
                    removeItem(player, EVENT_GLITTMEDAL, 20L);
                    show("scripts/events/glitmedal/event_col_agent2_q0996_25.htm", player);
                    return;
                }
            }
            show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Rabbit) >= 1L) {
            if (getItemCount((Playable) player, EVENT_GLITTMEDAL) >= 10L) {
                if (isTalker == 1) {
                    removeItem(player, Badge_of_Rabbit, 1L);
                    removeItem(player, EVENT_GLITTMEDAL, 10L);
                    addItem((Playable) player, Badge_of_Hyena, 1L);
                    show("scripts/events/glitmedal/event_col_agent2_q0996_22.htm", player);
                    return;
                }
                if (isTalker == 0) {
                    removeItem(player, EVENT_GLITTMEDAL, 10L);
                    show("scripts/events/glitmedal/event_col_agent2_q0996_25.htm", player);
                    return;
                }
            }
            show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
            return;
        }
        if (getItemCount((Playable) player, EVENT_GLITTMEDAL) >= 5L) {
            if (isTalker == 1) {
                removeItem(player, EVENT_GLITTMEDAL, 5L);
                addItem((Playable) player, Badge_of_Rabbit, 1L);
                show("scripts/events/glitmedal/event_col_agent2_q0996_21.htm", player);
                return;
            }
            if (isTalker == 0) {
                removeItem(player, EVENT_GLITTMEDAL, 5L);
                show("scripts/events/glitmedal/event_col_agent2_q0996_25.htm", player);
                return;
            }
        }
        show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
    }

    public void gameb() {
        final Player player = getSelf();
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        isTalker = Rnd.get(2);
        if (getItemCount((Playable) player, Badge_of_Fox) >= 1L) {
            if (getItemCount((Playable) player, EVENT_GLITTMEDAL) >= 40L) {
                if (isTalker == 1) {
                    removeItem(player, Badge_of_Fox, 1L);
                    removeItem(player, EVENT_GLITTMEDAL, 40L);
                    addItem((Playable) player, Badge_of_Wolf, 1L);
                    show("scripts/events/glitmedal/event_col_agent2_q0996_34.htm", player);
                    return;
                }
                if (isTalker == 0) {
                    removeItem(player, EVENT_GLITTMEDAL, 40L);
                    show("scripts/events/glitmedal/event_col_agent2_q0996_35.htm", player);
                    return;
                }
            }
            show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Hyena) >= 1L) {
            if (getItemCount((Playable) player, EVENT_GLITTMEDAL) >= 20L) {
                if (isTalker == 1) {
                    removeItem(player, Badge_of_Hyena, 1L);
                    removeItem(player, EVENT_GLITTMEDAL, 20L);
                    addItem((Playable) player, Badge_of_Fox, 1L);
                    show("scripts/events/glitmedal/event_col_agent2_q0996_33.htm", player);
                    return;
                }
                if (isTalker == 0) {
                    removeItem(player, EVENT_GLITTMEDAL, 20L);
                    show("scripts/events/glitmedal/event_col_agent2_q0996_35.htm", player);
                    return;
                }
            }
            show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
            return;
        }
        if (getItemCount((Playable) player, Badge_of_Rabbit) >= 1L) {
            if (getItemCount((Playable) player, EVENT_GLITTMEDAL) >= 10L) {
                if (isTalker == 1) {
                    removeItem(player, Badge_of_Rabbit, 1L);
                    removeItem(player, EVENT_GLITTMEDAL, 10L);
                    addItem((Playable) player, Badge_of_Hyena, 1L);
                    show("scripts/events/glitmedal/event_col_agent2_q0996_32.htm", player);
                    return;
                }
                if (isTalker == 0) {
                    removeItem(player, EVENT_GLITTMEDAL, 10L);
                    show("scripts/events/glitmedal/event_col_agent2_q0996_35.htm", player);
                    return;
                }
            }
            show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
            return;
        }
        if (getItemCount((Playable) player, EVENT_GLITTMEDAL) >= 5L) {
            if (isTalker == 1) {
                removeItem(player, EVENT_GLITTMEDAL, 5L);
                addItem((Playable) player, Badge_of_Rabbit, 1L);
                show("scripts/events/glitmedal/event_col_agent2_q0996_31.htm", player);
                return;
            }
            if (isTalker == 0) {
                removeItem(player, EVENT_GLITTMEDAL, 5L);
                show("scripts/events/glitmedal/event_col_agent2_q0996_35.htm", player);
                return;
            }
        }
        show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
    }

}
