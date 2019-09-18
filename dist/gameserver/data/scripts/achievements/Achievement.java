package achievements;

import achievements.AchievementInfo.AchievementInfoLevel;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class Achievement {
    public static final int BASE_ACHIEVEMENT_LEVEL = 1;
    private static final String VAR_NAME_FORMAT = "ex_achievement_%d_%s";
    private final AchievementInfo _achInfo;
    private final HardReference<Player> _playerRef;
    private final AchievementCounter _counter;
    private final long _expireTime;

    public Achievement(final AchievementInfo achInfo, final Player player) {
        _achInfo = achInfo;
        _playerRef = player.getRef();
        _counter = achInfo.getMetricType().getCounter(player, achInfo);
        _expireTime = Long.parseLong(getVar("expire_time", String.valueOf(_achInfo.nextResetTimeMills())));
        checkExpire();
    }

    private Player getPlayer() {
        return _playerRef.get();
    }

    public AchievementInfo getAchInfo() {
        return _achInfo;
    }

    private int getNextLevelNum() {
        return getVar("next_lvl", 1);
    }

    private void setNextLevelNum(final int nextLevelNum) {
        setVar("next_lvl", nextLevelNum);
    }

    public void checkExpire() {
        final long now = System.currentTimeMillis();
        if (_expireTime >= 0L) {
            setVar("expire_time", String.valueOf(_expireTime));
        }
        if (_expireTime < 0L || _expireTime > now) {
            return;
        }
        _counter.setVal(0);
        _counter.store();
        setNextLevelNum(1);
        _achInfo.getLevels().stream().filter(this::isLevelRewarded).forEach(level -> setLevelRewarded(level, false));
        final long nextExpire = _achInfo.nextResetTimeMills();
        setVar("expire_time", String.valueOf(nextExpire));
    }

    private String getVar(final String key, final String defVal) {
        final Player player = getPlayer();
        if (player == null) {
            return defVal;
        }
        final String val = player.getVar(String.format(VAR_NAME_FORMAT, _achInfo.getId(), key));
        return (val != null) ? val : defVal;
    }

    private int getVar(final String key, final int defVal) {
        return Integer.parseInt(getVar(key, String.valueOf(defVal)));
    }

    private void setVar(final String key, final String val) {
        final Player player = getPlayer();
        if (player == null) {
            return;
        }
        player.setVar(String.format(VAR_NAME_FORMAT, _achInfo.getId(), key), val, -1L);
    }

    private void setVar(final String key, final int val) {
        setVar(key, String.valueOf(val));
    }

    public AchievementInfoLevel getLevel() {
        return _achInfo.getLevel(getNextLevelNum() - 1);
    }

    public boolean isRewardableLevel(final AchievementInfoLevel level) {
        return level != null && level.getAchievementInfo() == getAchInfo() && level.getLevel() < getNextLevelNum() && level.getLevel() >= 1 && !isLevelRewarded(level);
    }

    public boolean isLevelRewarded(final int level) {
        return getVar(String.format("lvl_%d_rewarded", level), 0) != 0;
    }

    public boolean isLevelRewarded(final AchievementInfoLevel level) {
        return isLevelRewarded(level.getLevel());
    }

    public void setLevelRewarded(final int level, final boolean rewarded) {
        setVar(String.format("lvl_%d_rewarded", level), rewarded ? 1 : 0);
    }

    public void setLevelRewarded(final AchievementInfoLevel level, final boolean rewarded) {
        setLevelRewarded(level.getLevel(), rewarded);
    }

    public AchievementInfoLevel getNextLevel() {
        return _achInfo.getLevel(getNextLevelNum());
    }

    public boolean isCompleted() {
        final int nextLevelNum = getNextLevelNum();
        return nextLevelNum > _achInfo.getMaxLevel();
    }

    public AchievementCounter getCounter() {
        return _counter;
    }

    public void onMetricEvent(final Object... args) {
        final Player player = getPlayer();
        if (player == null) {
            return;
        }
        if (!_achInfo.testConds(getPlayer(), args)) {
            return;
        }
        if (_achInfo.getMetricNotifyDelay() > 0L) {
            ThreadPoolManager.getInstance().schedule(new EventMetric(player, args), _achInfo.getMetricNotifyDelay() * 1000L);
        } else {
            ThreadPoolManager.getInstance().execute(new EventMetric(player, args));
        }
    }

    private class EventMetric extends RunnableImpl {
        private final Object[] _args;
        private final HardReference<Player> _playerRef;

        private EventMetric(final Player player, final Object[] args) {
            _args = args;
            _playerRef = player.getRef();
        }

        @Override
        public void runImpl() {
            final Player player = _playerRef.get();
            if (player == null) {
                return;
            }
            final AchievementInfoLevel nextLevelInfo = getNextLevel();
            if (nextLevelInfo != null && nextLevelInfo.testConds(player, _args)) {
                final AchievementCounter counter = getCounter();
                final int value = counter.incrementAndGetValue();
                AchievementInfoLevel currLevelInfo;
                if (value >= nextLevelInfo.getValue()) {
                    currLevelInfo = nextLevelInfo;
                    setNextLevelNum(currLevelInfo.getLevel() + 1);
                    if (currLevelInfo.isResetMetric()) {
                        counter.setVal(0);
                    }
                    final String screenText = new CustomMessage("achievements.achievementS1Unlocked", player, new Object[]{_achInfo.getName(player)}).toString();
                    player.sendPacket(new ExShowScreenMessage(screenText, 5000, 0, ScreenMessageAlign.TOP_CENTER, false, 1, -1, true));
                    player.sendMessage(new CustomMessage("achievements.achievedS1LevelS2", player, _achInfo.getName(player), nextLevelInfo.getLevel()));
                }
                counter.store();
            }
        }
    }
}
