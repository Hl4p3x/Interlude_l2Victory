package services;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.time.cron.SchedulingPattern;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.scripts.Functions;

import java.util.concurrent.ScheduledFuture;

public class DoorTools extends Functions implements OnInitScriptListener {
    private static long getMillsRemainingToPattern(final String pattern) {
        final long now = System.currentTimeMillis();
        return new SchedulingPattern(pattern).next(now) - now;
    }

    private static Mutable<ScheduledFuture<?>> scheduleAtCron(final RunnableImpl r, final String p) {
        final MutableObject<ScheduledFuture<?>> futureRef = new MutableObject<>();
        futureRef.setValue(ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                try {
                    r.run();
                } finally {
                    futureRef.setValue(ThreadPoolManager.getInstance().schedule(this, getMillsRemainingToPattern(p)));
                }
            }
        }, getMillsRemainingToPattern(p)));
        return futureRef;
    }

    @Override
    public void onInit() {
        if (Config.ENABLE_ON_TIME_DOOR_OPEN) {
            scheduleAtCron(new RunnableImpl() {
                @Override
                public void runImpl() {
                    final DoorInstance theRoor = ReflectionManager.DEFAULT.getDoor(Config.ON_TIME_OPEN_DOOR_ID);
                    if (theRoor != null) {
                        theRoor.openMe(null, true);
                    }
                }
            }, Config.ON_TIME_OPEN_PATTERN);
        }
    }
}
