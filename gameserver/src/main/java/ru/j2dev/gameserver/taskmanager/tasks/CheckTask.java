package ru.j2dev.gameserver.taskmanager.tasks;

import com.stringer.annotations.HideAccess;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.utils.PtsUtils;

/**
 * Created by JunkyFunky
 * on 11.02.2018 22:39
 * group j2dev
 */
@HideAccess
public class CheckTask extends RunnableImpl {

    @HideAccess
    @Override
    public void runImpl() {
        PtsUtils.checkLicense();
    }
}
