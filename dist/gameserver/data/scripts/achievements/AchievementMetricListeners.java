package achievements;

import ru.j2dev.commons.listener.Listener;
import ru.j2dev.gameserver.listener.CharListener;
import ru.j2dev.gameserver.listener.PlayerListener;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.actor.OnKillListener;
import ru.j2dev.gameserver.listener.actor.player.*;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.actor.listener.PlayerListenerList;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadGame;
import ru.j2dev.gameserver.model.instances.RaidBossInstance;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.utils.Location;

import java.util.*;
import java.util.stream.Collectors;

public class AchievementMetricListeners {
    private static final AchievementMetricListeners INSTANCE = new AchievementMetricListeners();

    private final List<? extends Listener<Creature>> _listenersInstances = Arrays.asList(new AchievementOnPlayerEnter(),
            new AchievementOnKill(),
            new AchievementOnDeath(),
            new AchievementOnPvPPkKill(),
            new AchievementOnGainExpSp(),
            new AchievementOnOlyCompetitionCompleted(),
            new AchievementOnQuestStateChange());


    public static AchievementMetricListeners getInstance() {
        return AchievementMetricListeners.INSTANCE;
    }

    public void metricEvent(final Player player, final AchievementMetricType eventType, final Object... args) {
        if (!Achievements.getInstance().isEnabled()) {
            return;
        }
        final List<AchievementInfo> achievementInfos = Achievements.getInstance().getAchievementInfosByMetric(eventType);
        if (achievementInfos == null || achievementInfos.isEmpty()) {
            return;
        }
        for (final AchievementInfo ie : achievementInfos) {
            final Achievement achievement = new Achievement(ie, player);
            if (achievement.isCompleted()) {
                continue;
            }
            achievement.onMetricEvent(args);
        }
    }

    public void init() {
        _listenersInstances.forEach(listener -> {
            if (listener instanceof PlayerListener) {
                PlayerListenerList.addGlobal(listener);
            } else {
                if (!(listener instanceof CharListener)) {
                    throw new IllegalStateException("Unknown listener " + listener.getClass());
                }
                CharListenerList.addGlobal(listener);
            }
        });
    }

    public void done() {
        _listenersInstances.forEach(listener -> {
            if (listener instanceof PlayerListener) {
                PlayerListenerList.removeGlobal(listener);
            } else {
                if (!(listener instanceof CharListener)) {
                    return;
                }
                CharListenerList.removeGlobal(listener);
            }
        });
    }

    public static class AchievementOnPlayerEnter implements OnPlayerEnterListener {
        @Override
        public void onPlayerEnter(final Player player) {
            AchievementMetricListeners.getInstance().metricEvent(player, AchievementMetricType.LOGIN, player);
        }
    }

    public static class AchievementOnKill implements OnKillListener {
        @Override
        public void onKill(final Creature actor, final Creature victim) {
            if (actor == null || !actor.isPlayer() || victim == null) {
                return;
            }
            if (victim.isNpc()) {
                AchievementMetricListeners.getInstance().metricEvent(actor.getPlayer(), AchievementMetricType.NPC_KILL, victim);
                if (victim instanceof RaidBossInstance) {
                    final RaidBossInstance raidBoss = (RaidBossInstance) victim;
                    final Location raidBossLoc = raidBoss.getLoc();
                    final List<Creature> raidParticipants = new ArrayList<>(raidBoss.getAggroList().getCharMap().keySet());
                    final Set<Player> raidPlayerParticipants = raidParticipants.stream().filter(Objects::nonNull).filter(creature -> !(raidBossLoc.distance3D(creature.getLoc()) > 1500.0)).filter(creature -> creature instanceof Player).map(creature -> (Player) creature).collect(Collectors.toCollection(LinkedHashSet::new));
                    raidPlayerParticipants.forEach(raidParticipant -> AchievementMetricListeners.getInstance().metricEvent(raidParticipant, AchievementMetricType.RAID_PARTICIPATION, victim));
                }
            }
        }

        @Override
        public boolean ignorePetOrSummon() {
            return true;
        }
    }

    public static class AchievementOnDeath implements OnDeathListener {
        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            if (actor.isPlayer()) {
                AchievementMetricListeners.getInstance().metricEvent(actor.getPlayer(), AchievementMetricType.DEATH, killer);
            }
        }
    }

    public static class AchievementOnPvPPkKill implements OnPvpPkKillListener {
        @Override
        public void onPvpPkKill(final Player killer, final Player victim, final boolean isPk) {
            if (!isPk) {
                AchievementMetricListeners.getInstance().metricEvent(killer, AchievementMetricType.PVP_KILL, killer.getPvpKills(), victim);
            }
        }
    }

    public static class AchievementOnGainExpSp implements OnGainExpSpListener {
        @Override
        public void onGainExpSp(final Player player, final long exp, final long sp) {
            AchievementMetricListeners.getInstance().metricEvent(player, AchievementMetricType.LEVEL, player.getLevel());
        }
    }

    public static class AchievementOnOlyCompetitionCompleted implements OnOlyCompetitionListener {
        @Override
        public void onOlyCompetitionCompleted(final Player player, final OlympiadGame olympiadGame, final boolean isWin) {
            AchievementMetricListeners.getInstance().metricEvent(player, AchievementMetricType.OLYMPIAD, olympiadGame, isWin);
        }
    }

    public static class AchievementOnQuestStateChange implements OnQuestStateChangeListener {
        @Override
        public void onQuestStateChange(final Player player, final QuestState questState) {
            AchievementMetricListeners.getInstance().metricEvent(player, AchievementMetricType.QUEST_STATE, questState);
        }
    }
}
