package ru.j2dev.gameserver.taskmanager.tasks.objecttasks;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.model.Creature;

/**
 * Created by JunkyFunky
 * on 09.03.2018 11:52
 * group j2dev
 */
public class HitTask extends RunnableImpl {
    final boolean _crit;
    final boolean _miss;
    final boolean _shld;
    final boolean _soulshot;
    final boolean _unchargeSS;
    final boolean _notify;
    final int _damage;
    final long _actDelay;
    private final HardReference<? extends Creature> _charRef;
    private final HardReference<? extends Creature> _targetRef;

    public HitTask(final Creature cha, final Creature target, final int damage, final boolean crit, final boolean miss, final boolean soulshot, final boolean shld, final boolean unchargeSS, final boolean notify) {
        _charRef = cha.getRef();
        _targetRef = target.getRef();
        _damage = damage;
        _crit = crit;
        _shld = shld;
        _miss = miss;
        _soulshot = soulshot;
        _unchargeSS = unchargeSS;
        _notify = notify;
        _actDelay = 0L;
    }

    public HitTask(final Creature cha, final Creature target, final int damage, final boolean crit, final boolean miss, final boolean soulshot, final boolean shld, final boolean unchargeSS, final boolean notify, final long actDelay) {
        _charRef = cha.getRef();
        _targetRef = target.getRef();
        _damage = damage;
        _crit = crit;
        _shld = shld;
        _miss = miss;
        _soulshot = soulshot;
        _unchargeSS = unchargeSS;
        _notify = notify;
        _actDelay = actDelay;
    }

    @Override
    public void runImpl() {
        final Creature character;
        final Creature target;
        if ((character = _charRef.get()) == null || (target = _targetRef.get()) == null) {
            return;
        }
        if (character.isAttackAborted()) {
            return;
        }
        character.onHitTimer(target, _damage, _crit, _miss, _soulshot, _shld, _unchargeSS);
        if (_notify) {
            if (_actDelay > 0L) {
                ThreadPoolManager.getInstance().schedule(new ActReadyTask(character), _actDelay);
            } else {
                character.getAI().notifyEvent(CtrlEvent.EVT_READY_TO_ACT);
            }
        }
    }
}
