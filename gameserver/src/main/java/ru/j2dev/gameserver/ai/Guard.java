package ru.j2dev.gameserver.ai;

import ru.j2dev.gameserver.model.AggroList.AggroInfo;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class Guard extends Fighter {
    public Guard(final NpcInstance actor) {
        super(actor);
    }

    @Override
    public boolean canAttackCharacter(final Creature target) {
        final NpcInstance actor = getActor();
        if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK) {
            final AggroInfo ai = actor.getAggroList().get(target);
            return ai != null && ai.hate > 0;
        }
        return target.isMonster() || target.isPlayable();
    }

    @Override
    public boolean checkAggression(final Creature target) {
        final NpcInstance actor = getActor();
        return getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && isGlobalAggro() && (!target.isPlayable() || (target.getKarma() != 0 && (!actor.getParameter("evilGuard", false) || target.getPvpFlag() <= 0))) && !target.isMonster() && super.checkAggression(target);
    }

    @Override
    public int getMaxAttackTimeout() {
        return 0;
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }
}
