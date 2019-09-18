package ru.j2dev.gameserver.taskmanager.tasks.objecttasks;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.model.Creature;

/**
 * Created by JunkyFunky
 * on 09.03.2018 11:53
 * group j2dev
 */
public class NotifyAITask extends RunnableImpl {
    private final CtrlEvent _evt;
    private final Object _agr0;
    private final Object _agr1;
    private final Object _agr2;
    private final HardReference<? extends Creature> _charRef;

    public NotifyAITask(Creature cha, CtrlEvent evt, Object arg0, Object arg1, Object arg2) {
        _charRef = cha.getRef();
        _evt = evt;
        _agr0 = arg0;
        _agr1 = arg1;
        _agr2 = arg2;
    }

    public NotifyAITask(final Creature cha, final CtrlEvent evt, final Object agr0, final Object agr1) {
        _charRef = cha.getRef();
        _evt = evt;
        _agr0 = agr0;
        _agr1 = agr1;
        _agr2 = null;
    }

    public NotifyAITask(final Creature cha, final CtrlEvent evt, final Object arg0) {
        this(cha, evt, arg0, null);
    }

    public NotifyAITask(final Creature cha, final CtrlEvent evt) {
        this(cha, evt, null, null);
    }

    @Override
    public void runImpl() {
        final Creature character = _charRef.get();
        if (character == null || !character.hasAI()) {
            return;
        }
        character.getAI().notifyEvent(_evt, _agr0, _agr1, _agr2);
    }
}
