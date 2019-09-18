package ru.j2dev.gameserver.model.reward;

import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RewardGroup implements Cloneable {
    private final List<RewardData> _items;
    private double _chance;
    private boolean _isAdena;
    private boolean _isSealStone;
    private boolean _notRate;
    private double _chanceSum;

    public RewardGroup(final double chance) {
        _isAdena = false;
        _isSealStone = false;
        _notRate = false;
        _items = new ArrayList<>();
        setChance(chance);
    }

    public boolean notRate() {
        return _notRate;
    }

    public void setNotRate(final boolean notRate) {
        _notRate = notRate;
    }

    public double getChance() {
        return _chance;
    }

    public void setChance(final double chance) {
        _chance = chance;
    }

    public boolean isAdena() {
        return _isAdena;
    }

    public boolean isSealStone() {
        return _isSealStone;
    }

    public void setIsAdena(final boolean isAdena) {
        _isAdena = isAdena;
    }

    public void addData(final RewardData item) {
        if (item.getItem().isAdena()) {
            _isAdena = true;
        } else if (item.getItem().isSealStone()) {
            _isSealStone = true;
        }
        item.setChanceInGroup(_chanceSum += item.getChance());
        _items.add(item);
    }

    public List<RewardData> getItems() {
        return _items;
    }

    @Override
    public RewardGroup clone() {
        final RewardGroup ret = new RewardGroup(_chance);
        _items.stream().map(RewardData::clone).forEach(ret::addData);
        return ret;
    }

    public List<RewardItem> roll(final RewardType type, final Player player, final double mod, final boolean isRaid, final boolean isSiegeGuard) {
        if (player == null || !player.isConnected()) {
            return Collections.emptyList();
        }
        switch (type) {
            case NOT_RATED_GROUPED:
            case NOT_RATED_NOT_GROUPED: {
                return rollItems(mod, 1.0, 1.0);
            }
            case EVENT: {
                return rollItems(mod, 1.0, 1.0);
            }
            case SWEEP: {
                return rollSpoil(Config.RATE_DROP_SPOIL, player.getRateSpoil(), mod);
            }
            case RATED_GROUPED: {
                if (_isAdena) {
                    return rollAdena(mod, player.getRateAdena());
                }
                if (_isSealStone) {
                    return rollSealStones(mod, player.getRateItems());
                }
                if (isRaid) {
                    return rollItems(mod, Config.RATE_DROP_RAIDBOSS * player.getBonus().getDropRaidItems(), 1.0);
                }
                if (isSiegeGuard) {
                    return rollItems(mod, Config.RATE_DROP_SIEGE_GUARD, 1.0);
                }
                return rollItems(mod, Config.RATE_DROP_ITEMS, player.getRateItems());
            }
            default: {
                return Collections.emptyList();
            }
        }
    }

    private List<RewardItem> rollSealStones(final double mod, final double playerRate) {
        final List<RewardItem> ret = rollItems(mod, Config.RATE_DROP_SEAL_STONES, playerRate);
        ret.forEach(rewardItem -> rewardItem.isSealStone = true);
        return ret;
    }

    public List<RewardItem> rollItems(final double mod, final double baseRate, final double playerRate) {
        if (mod <= 0.0) {
            return Collections.emptyList();
        }
        double rate;
        if (_notRate) {
            rate = Math.min(mod, 1.0);
        } else {
            rate = baseRate * playerRate * mod;
        }
        final double mult = Math.ceil(rate);
        boolean firstPass = true;
        final List<RewardItem> ret = new ArrayList<>(_items.size() * 3 / 2);
        for (long n = 0L; n < mult; ++n) {
            final double gmult = rate - n;
            if (Rnd.get(1, 1000000) <= _chance * Math.min(gmult, 1.0)) {
                if (!Config.ALT_MULTI_DROP) {
                    rollFinal(_items, ret, Math.max(gmult, 1.0), firstPass);
                    break;
                }
                rollFinal(_items, ret, 1.0, firstPass);
            }
            firstPass = false;
        }
        return ret;
    }

    private List<RewardItem> rollSpoil(final double baseRate, final double playerRate, final double mod) {
        if (mod <= 0.0) {
            return Collections.emptyList();
        }
        double rate;
        if (_notRate) {
            rate = Math.min(mod, 1.0);
        } else {
            rate = baseRate * playerRate * mod;
        }
        final double mult = Math.ceil(rate);
        boolean firstPass = true;
        final List<RewardItem> ret = new ArrayList<>(_items.size() * 3 / 2);
        for (long n = 0L; n < mult; ++n) {
            if (Rnd.get(1, 1000000) <= _chance * Math.min(rate - n, 1.0)) {
                rollFinal(_items, ret, 1.0, firstPass);
            }
            firstPass = false;
        }
        return ret;
    }

    private List<RewardItem> rollAdena(final double mod, final double playerRate) {
        return rollAdena(mod, Config.RATE_DROP_ADENA, playerRate);
    }

    private List<RewardItem> rollAdena(double mod, final double baseRate, final double playerRate) {
        double chance = _chance;
        if (mod > 10.0) {
            mod *= _chance / 1000000.0;
            chance = 1000000.0;
        }
        if (mod <= 0.0) {
            return Collections.emptyList();
        }
        if (Rnd.get(1, 1000000) > chance) {
            return Collections.emptyList();
        }
        final double rate = baseRate * playerRate * mod;
        final List<RewardItem> ret = new ArrayList<>(_items.size());
        rollFinal(_items, ret, rate, true);
        ret.forEach(i -> i.isAdena = true);
        return ret;
    }

    private void rollFinal(final List<RewardData> items, final List<RewardItem> ret, final double mult, final boolean firstPass) {
        final int chance = Rnd.get(0, (int) Math.max(_chanceSum, 1000000.0));
        for (final RewardData i : items) {
            if (!firstPass && i.onePassOnly()) {
                continue;
            }
            if (chance >= i.getChanceInGroup() || chance <= i.getChanceInGroup() - i.getChance()) {
                continue;
            }
            final double imult = i.notRate() ? 1.0 : mult;
            long count = (long) Math.floor(i.getMinDrop() * imult);
            long max = (long) Math.ceil(i.getMaxDrop() * imult);
            if (count != max) {
                if (max < count) {
                    max = count;
                }
                count = Rnd.get(count, max);
            }
            RewardItem t = ret.stream().filter(r -> i.getItemId() == r.itemId).findFirst().orElse(null);
            if (t == null) {
                ret.add(t = new RewardItem(i.getItemId()));
                t.count = count;
                break;
            }
            if (!i.notRate()) {
                t.count = SafeMath.addAndLimit(t.count, count);
                break;
            }
            break;
        }
    }
}
