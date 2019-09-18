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
public class ActReadyTask extends RunnableImpl {
    private final HardReference<? extends Creature> _charRef;

    public ActReadyTask(final Creature cha) {
        _charRef = cha.getRef();
    }

    @Override
    public void runImpl() {
        final Creature character = _charRef.get();
        if (character == null) {
            return;
        }
        character.getAI().notifyEvent(CtrlEvent.EVT_READY_TO_ACT);
    }
}
