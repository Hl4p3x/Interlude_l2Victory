package ru.j2dev.gameserver.taskmanager.tasks.objecttasks;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.model.Creature;

/**
 * Created by JunkyFunky
 * on 09.03.2018 11:48
 * group j2dev
 */
public class DeleteTask extends RunnableImpl {

    private final HardReference<? extends Creature> _ref;

    public DeleteTask(final Creature c) {
        _ref = c.getRef();
    }

    @Override
    public void runImpl() {
        final Creature c = _ref.get();
        if (c != null) {
            c.deleteMe();
        }
    }
}
