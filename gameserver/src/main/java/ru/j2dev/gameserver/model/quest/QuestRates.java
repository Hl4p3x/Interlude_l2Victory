package ru.j2dev.gameserver.model.quest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestRates {
    private static final Logger LOG = LoggerFactory.getLogger(QuestRates.class);

    private final int _questId;
    private double _dropRate;
    private double _rewardRate;
    private double _expRate;
    private double _spRate;

    public QuestRates(final int questId) {
        _questId = questId;
        _dropRate = 1.0;
        _rewardRate = 1.0;
        _expRate = 1.0;
        _spRate = 1.0;
    }

    public void updateParam(final String paramName, final String paramValue) {
        if ("Drop".equalsIgnoreCase(paramName) || "DropRate".equalsIgnoreCase(paramName)) {
            setDropRate(Double.parseDouble(paramValue));
        } else if ("Reward".equalsIgnoreCase(paramName) || "RewardRate".equalsIgnoreCase(paramName)) {
            setRewardRate(Double.parseDouble(paramValue));
        } else if ("Exp".equalsIgnoreCase(paramName) || "ExpRate".equalsIgnoreCase(paramName)) {
            setExpRate(Double.parseDouble(paramValue));
        } else {
            if (!"Sp".equalsIgnoreCase(paramName) && !"SpRate".equalsIgnoreCase(paramName)) {
                throw new IllegalArgumentException("Unknown param \"" + paramName + "\"");
            }
            setExpRate(Double.parseDouble(paramValue));
        }
    }

    public double getDropRate() {
        return _dropRate;
    }

    public void setDropRate(final double dropRate) {
        _dropRate = dropRate;
    }

    public double getRewardRate() {
        return _rewardRate;
    }

    public void setRewardRate(final double rewardRate) {
        _rewardRate = rewardRate;
    }

    public double getExpRate() {
        return _expRate;
    }

    public void setExpRate(final double expRate) {
        _expRate = expRate;
    }

    public double getSpRate() {
        return _spRate;
    }

    public void setSpRate(final double spRate) {
        _spRate = spRate;
    }
}
