package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.LockType;
import ru.j2dev.gameserver.stats.Env;

public class EffectLockInventory extends Effect {
    private final LockType _lockType;
    private final int[] _lockItems;

    public EffectLockInventory(final Env env, final EffectTemplate template) {
        super(env, template);
        _lockType = template.getParam().getEnum("lockType", LockType.class);
        _lockItems = template.getParam().getIntegerArray("lockItems");
    }

    @Override
    public void onStart() {
        super.onStart();
        final Player player = _effector.getPlayer();
        player.getInventory().lockItems(_lockType, _lockItems);
    }

    @Override
    public void onExit() {
        super.onExit();
        final Player player = _effector.getPlayer();
        player.getInventory().unlock();
    }

    @Override
    protected boolean onActionTime() {
        return false;
    }
}
