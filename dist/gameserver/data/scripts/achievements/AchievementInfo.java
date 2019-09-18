package achievements;

import ru.j2dev.commons.time.cron.NextTime;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.reward.RewardData;

import java.util.*;

public class AchievementInfo {
    private final int _id;
    private final AchievementMetricType _metricType;
    private final long _metricNotifyDelay;
    private final String _nameAddr;
    private final NextTime _expireTime;
    private final Map<Integer, AchievementInfoLevel> _levelsMap = new TreeMap<>();
    private final List<AchievementInfoLevel> _levels = new ArrayList<>();
    private AchievementInfoCategory _category;
    private int _minLevel;
    private int _maxLevel;
    private List<AchievementCondition> _condList = new ArrayList<>();
    private String _icon = "icon.etc_plan_i00";

    public AchievementInfo(final int id, final AchievementMetricType metricType, final long metricNotifyDelay, final String nameAddr, final NextTime expireTime) {
        _minLevel = Integer.MAX_VALUE;
        _maxLevel = Integer.MIN_VALUE;
        _id = id;
        _metricType = metricType;
        _metricNotifyDelay = metricNotifyDelay;
        _nameAddr = nameAddr;
        _expireTime = expireTime;
        _maxLevel = 0;
    }

    public String getIcon() {
        return _icon;
    }

    public void setIcon(final String icon) {
        _icon = icon;
    }

    public AchievementInfoCategory getCategory() {
        return _category;
    }

    public void setCategory(final AchievementInfoCategory category) {
        _category = category;
    }

    public long nextResetTimeMills() {
        if (_expireTime == null) {
            return -1L;
        }
        return _expireTime.next(System.currentTimeMillis());
    }

    public int getId() {
        return _id;
    }

    public void addCond(final AchievementCondition cond) {
        _condList.add(cond);
    }

    public void addLevel(final AchievementInfoLevel achInfoLevel) {
        if (achInfoLevel.getLevel() > _maxLevel) {
            _maxLevel = achInfoLevel.getLevel();
        }
        if (achInfoLevel.getLevel() < _minLevel) {
            _minLevel = achInfoLevel.getLevel();
        }
        _levelsMap.put(achInfoLevel.getLevel(), achInfoLevel);
        _levels.add(achInfoLevel);
        Collections.sort(_levels);
    }

    public AchievementInfoLevel addLevel(final int level, final int value, final String descAddr, final boolean resetMetric) {
        final AchievementInfoLevel achievementInfoLevel = new AchievementInfoLevel(this, level, value, descAddr, resetMetric);
        addLevel(achievementInfoLevel);
        return achievementInfoLevel;
    }

    public int getMaxLevel() {
        return _maxLevel;
    }

    public List<AchievementCondition> getCondList() {
        return _condList;
    }

    public void setCondList(final List<AchievementCondition> condList) {
        _condList = condList;
    }

    public AchievementMetricType getMetricType() {
        return _metricType;
    }

    public long getMetricNotifyDelay() {
        return _metricNotifyDelay;
    }

    public List<AchievementInfoLevel> getLevels() {
        return _levels;
    }

    public AchievementInfoLevel getLevel(final int lvl) {
        return _levelsMap.get(lvl);
    }

    public String getNameAddr() {
        return _nameAddr;
    }

    public String getName(final Player player) {
        return StringHolder.getInstance().getNotNull(player, getNameAddr());
    }

    public boolean testConds(final Player player, final Object... args) {
        return getCondList().stream().allMatch(cond -> cond.test(player, args));
    }

    public static class AchievementInfoCategory {
        private final String _name;
        private final String _titleAddress;

        public AchievementInfoCategory(final String name, final String titleAddress) {
            _name = name;
            _titleAddress = titleAddress;
        }

        public String getName() {
            return _name;
        }

        public String getTitle(final Player player) {
            return StringHolder.getInstance().getNotNull(player, _titleAddress);
        }
    }

    public static class AchievementInfoLevel implements Comparable<AchievementInfoLevel> {
        private final AchievementInfo _achievementInfo;
        private final int _level;
        private final int _value;
        private final String _descAddr;
        private final boolean _resetMetric;
        private final List<RewardData> _rewardDataList;
        private final List<AchievementCondition> _achievementConditions;

        public AchievementInfoLevel(final AchievementInfo info, final int level, final int value, final String descAddr, final boolean resetMetric) {
            _rewardDataList = new ArrayList<>();
            _achievementConditions = new ArrayList<>();
            _achievementInfo = info;
            _level = level;
            _value = value;
            _descAddr = descAddr;
            _resetMetric = resetMetric;
        }

        public AchievementInfo getAchievementInfo() {
            return _achievementInfo;
        }

        public boolean isResetMetric() {
            return _resetMetric;
        }

        public int getLevel() {
            return _level;
        }

        public int getValue() {
            return _value;
        }

        public String getDescAddr() {
            return _descAddr;
        }

        public List<RewardData> getRewardDataList() {
            return _rewardDataList;
        }

        public List<AchievementCondition> getCondList() {
            return _achievementConditions;
        }

        public void addCond(final AchievementCondition achievementCondition) {
            _achievementConditions.add(achievementCondition);
        }

        public void addRewardData(final RewardData rewardData) {
            _rewardDataList.add(rewardData);
        }

        public String getDesc(final Player player) {
            return StringHolder.getInstance().getNotNull(player, getDescAddr());
        }

        public boolean testConds(final Player player, final Object... args) {
            return getCondList().stream().allMatch(cond -> cond.test(player, args));
        }

        @Override
        public int compareTo(final AchievementInfoLevel other) {
            if (getAchievementInfo().getId() != other.getAchievementInfo().getId()) {
                return Integer.compare(getAchievementInfo().getId(), other.getAchievementInfo().getId());
            }
            return Integer.compare(getLevel(), other.getLevel());
        }
    }
}
