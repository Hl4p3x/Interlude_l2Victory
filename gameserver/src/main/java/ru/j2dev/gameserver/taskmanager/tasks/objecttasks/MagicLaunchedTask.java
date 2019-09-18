package ru.j2dev.gameserver.taskmanager.tasks.objecttasks;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillLaunched;

import java.util.List;

/**
 * Created by JunkyFunky
 * on 09.03.2018 11:53
 * group j2dev
 */
public class MagicLaunchedTask extends RunnableImpl {
    public final boolean _forceUse;
    private final HardReference<? extends Creature> _charRef;

    public MagicLaunchedTask(final Creature cha, final boolean forceUse) {
        _charRef = cha.getRef();
        _forceUse = forceUse;
    }

    @Override
    public void runImpl() {
        final Creature character = _charRef.get();
        if (character == null) {
            return;
        }
        final Skill castingSkill = character.getCastingSkill();
        final Creature castingTarget = character.getCastingTarget();
        if (castingSkill == null || castingTarget == null) {
            character.clearCastVars();
            return;
        }
        if (!castingSkill.checkCondition(character, castingTarget, _forceUse, false, false)) {
            character.abortCast(true, false);
            return;
        }
        final List<Creature> targets = castingSkill.getTargets(character, castingTarget, _forceUse);
        character.broadcastPacket(new MagicSkillLaunched(character, castingSkill, targets.stream().mapToInt(GameObject::getObjectId).toArray()));
    }
}
