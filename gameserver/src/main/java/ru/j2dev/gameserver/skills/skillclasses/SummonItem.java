package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.List;

public class SummonItem extends Skill {
    private final int _itemId;
    private final int _minId;
    private final int _maxId;
    private final long _minCount;
    private final long _maxCount;

    public SummonItem(final StatsSet set) {
        super(set);
        _itemId = set.getInteger("SummonItemId", 0);
        _minId = set.getInteger("SummonMinId", 0);
        _maxId = set.getInteger("SummonMaxId", _minId);
        _minCount = set.getLong("SummonMinCount");
        _maxCount = set.getLong("SummonMaxCount", _minCount);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        if (!activeChar.isPlayable()) {
            return;
        }
        for (final Creature target : targets) {
            if (target != null) {
                final int itemId = (_minId > 0) ? Rnd.get(_minId, _maxId) : _itemId;
                final long count = Rnd.get(_minCount, _maxCount);
                ItemFunctions.addItem((Playable) activeChar, itemId, count, true);
                getEffects(activeChar, target, getActivateRate() > 0, false);
            }
        }
    }
}
