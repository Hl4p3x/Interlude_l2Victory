package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Summon;
import ru.j2dev.gameserver.stats.Env;

public class EffectBetray extends Effect {
    public EffectBetray(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (_effected != null && _effected.isSummon()) {
            final Summon summon = (Summon) _effected;
            summon.setDepressed(true);
            summon.getAI().Attack(summon.getPlayer(), true, false);
        }
    }

    @Override
    public void onExit() {
        super.onExit();
        if (_effected != null && _effected.isSummon()) {
            final Summon summon = (Summon) _effected;
            summon.setDepressed(false);
            summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
