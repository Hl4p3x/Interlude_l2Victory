package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.AggroList.AggroInfo;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.stats.Env;

import java.util.List;

public class EffectRandomHate extends Effect {
    public EffectRandomHate(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        return getEffected().isMonster() && Rnd.chance(_template.chance(100));
    }

    @Override
    public void onStart() {
        final MonsterInstance monster = (MonsterInstance) getEffected();
        final Creature mostHated = monster.getAggroList().getMostHated();
        if (mostHated == null) {
            return;
        }
        final AggroInfo mostAggroInfo = monster.getAggroList().get(mostHated);
        final List<Creature> hateList = monster.getAggroList().getHateList(monster.getAggroRange());
        hateList.remove(mostHated);
        if (!hateList.isEmpty()) {
            final AggroInfo newAggroInfo = monster.getAggroList().get(hateList.get(Rnd.get(hateList.size())));
            final int oldHate = newAggroInfo.hate;
            newAggroInfo.hate = mostAggroInfo.hate;
            mostAggroInfo.hate = oldHate;
        }
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    protected boolean onActionTime() {
        return false;
    }
}
