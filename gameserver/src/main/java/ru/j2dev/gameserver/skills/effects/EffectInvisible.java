package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.base.InvisibleType;
import ru.j2dev.gameserver.stats.Env;

public final class EffectInvisible extends Effect {
    private InvisibleType _invisibleType;

    public EffectInvisible(final Env env, final EffectTemplate template) {
        super(env, template);
        _invisibleType = InvisibleType.NONE;
    }

    @Override
    public boolean checkCondition() {
        if (!_effected.isPlayer()) {
            return false;
        }
        final Player player = (Player) _effected;
        return !player.isInvisible() && player.getActiveWeaponFlagAttachment() == null && super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        final Player player = (Player) _effected;
        _invisibleType = player.getInvisibleType();
        player.setInvisibleType(InvisibleType.EFFECT);
        World.removeObjectFromPlayers(player);
    }

    @Override
    public void onExit() {
        super.onExit();
        final Player player = (Player) _effected;
        if (!player.isInvisible()) {
            return;
        }
        player.setInvisibleType(_invisibleType);
        player.broadcastUserInfo(true);
        if (player.getPet() != null) {
            player.getPet().broadcastCharInfo();
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
