package ru.j2dev.gameserver.model.instances;

import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public final class AgathionInstance extends FeedableBeastInstance {
    public AgathionInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _playerRef = HardReferences.emptyRef();
        _remainingTime = MAX_DURATION;
        _durationCheckTask = null;
        _buffTask = null;
        _skills = Skill.EMPTY_ARRAY;
        _hasRandomWalk = false;
        _hasChatWindow = false;
        _hasRandomAnimation = false;
    }
}
