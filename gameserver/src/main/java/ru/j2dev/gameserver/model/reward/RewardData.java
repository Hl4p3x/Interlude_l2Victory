package ru.j2dev.gameserver.model.reward;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;

public class RewardData implements Cloneable {
    private final ItemTemplate _item;
    private boolean _notRate;
    private boolean _onePassOnly;
    private long _mindrop;
    private long _maxdrop;
    private double _chance;
    private double _chanceInGroup;

    public RewardData(final int itemId) {
        _item = ItemTemplateHolder.getInstance().getTemplate(itemId);
        setNotRate(_item.isArrow() || (Config.NO_RATE_EQUIPMENT && _item.isEquipment()) || (Config.NO_RATE_KEY_MATERIAL && _item.isKeyMatherial()) || (Config.NO_RATE_RECIPES && _item.isRecipe()), ArrayUtils.contains(Config.NO_RATE_ITEMS, itemId));
    }

    public RewardData(final int itemId, final long min, final long max, final double chance) {
        this(itemId);
        _mindrop = min;
        _maxdrop = max;
        _chance = chance;
    }

    public boolean notRate() {
        return _notRate;
    }

    public boolean onePassOnly() {
        return _onePassOnly;
    }

    public void setNotRate(final boolean notRate, final boolean onePassOnly) {
        _notRate = (notRate || onePassOnly);
        _onePassOnly = onePassOnly;
    }

    public int getItemId() {
        return _item.getItemId();
    }

    public ItemTemplate getItem() {
        return _item;
    }

    public long getMinDrop() {
        return _mindrop;
    }

    public void setMinDrop(final long mindrop) {
        _mindrop = mindrop;
    }

    public long getMaxDrop() {
        return _maxdrop;
    }

    public void setMaxDrop(final long maxdrop) {
        _maxdrop = maxdrop;
    }

    public double getChance() {
        return _chance;
    }

    public void setChance(final double chance) {
        _chance = chance;
    }

    public double getChanceInGroup() {
        return _chanceInGroup;
    }

    public void setChanceInGroup(final double chance) {
        _chanceInGroup = chance;
    }

    @Override
    public String toString() {
        return "ItemID: " + getItem() + " Min: " + getMinDrop() + " Max: " + getMaxDrop() + " Chance: " + getChance() / 10000.0 + "%";
    }

    @Override
    public RewardData clone() {
        return new RewardData(getItemId(), getMinDrop(), getMaxDrop(), getChance());
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof RewardData) {
            final RewardData drop = (RewardData) o;
            return drop.getItemId() == getItemId();
        }
        return false;
    }

    public List<RewardItem> roll(final Player player, final double mod) {
        double rate;
        if (_item.isAdena()) {
            rate = Config.RATE_DROP_ADENA * player.getRateAdena();
        } else {
            rate = Config.RATE_DROP_ITEMS * ((player != null) ? player.getRateItems() : 1.0);
        }
        return roll(rate * mod);
    }

    public List<RewardItem> roll(final double rate) {
        final double mult = Math.ceil(rate);
        final List<RewardItem> ret = new ArrayList<>(1);
        RewardItem t = null;
        for (int n = 0; n < mult; ++n) {
            if (Rnd.get(RewardList.MAX_CHANCE) <= _chance * Math.min(rate - n, 1.0)) {
                long count;
                if (getMinDrop() >= getMaxDrop()) {
                    count = getMinDrop();
                } else {
                    if (getMaxDrop() < getMinDrop()) {
                        count = Rnd.get(getMaxDrop(), getMinDrop());
                    } else {
                        count = Rnd.get(getMinDrop(), getMaxDrop());
                    }
                }
                if (t == null) {
                    ret.add(t = new RewardItem(_item.getItemId()));
                    t.count = count;
                } else {
                    t.count = SafeMath.addAndLimit(t.count, count);
                }
            }
        }
        return ret;
    }
}
