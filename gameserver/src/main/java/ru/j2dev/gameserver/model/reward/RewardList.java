package ru.j2dev.gameserver.model.reward;

import ru.j2dev.gameserver.model.Player;

import java.util.ArrayList;
import java.util.List;

public class RewardList extends ArrayList<RewardGroup> {
    public static final int MAX_CHANCE = 1000000;
    private final RewardType _type;
    private final boolean _autoLoot;

    public RewardList(final RewardType rewardType, final boolean a) {
        super(5);
        _type = rewardType;
        _autoLoot = a;
    }

    public List<RewardItem> roll(final Player player) {
        return roll(player, 1.0, false, false);
    }

    public List<RewardItem> roll(final Player player, final double mod) {
        return roll(player, mod, false, false);
    }

    public List<RewardItem> roll(final Player player, final double mod, final boolean isRaid) {
        return roll(player, mod, isRaid, false);
    }

    public List<RewardItem> roll(final Player player, final double mod, final boolean isRaid, final boolean isSiegeGuard) {
        final List<RewardItem> temp = new ArrayList<>(size());
        stream().map(g -> g.roll(_type, player, mod, isRaid, isSiegeGuard)).filter(tdl -> !tdl.isEmpty()).forEach(temp::addAll);
        return temp;
    }

    public boolean validate() {
        for (final RewardGroup g : this) {
            int chanceSum = g.getItems().stream().mapToInt(d -> (int) d.getChance()).sum();
            if (chanceSum <= MAX_CHANCE) {
                return true;
            }
            final double mod = MAX_CHANCE / chanceSum;
            g.getItems().forEach(d2 -> {
                final double chance = d2.getChance() * mod;
                d2.setChance(chance);
                g.setChance(MAX_CHANCE);
            });
        }
        return false;
    }

    public boolean isAutoLoot() {
        return _autoLoot;
    }

    public RewardType getType() {
        return _type;
    }
}
