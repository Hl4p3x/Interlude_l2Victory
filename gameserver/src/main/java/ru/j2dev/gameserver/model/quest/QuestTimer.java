package ru.j2dev.gameserver.model.quest;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.instances.NpcInstance;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class QuestTimer extends RunnableImpl {
    private final String _name;
    private final NpcInstance _npc;
    private long _time;
    private QuestState _qs;
    private ScheduledFuture<?> _schedule;

    public QuestTimer(final String name, final long time, final NpcInstance npc) {
        _name = name;
        _time = time;
        _npc = npc;
    }

    QuestState getQuestState() {
        return _qs;
    }

    void setQuestState(final QuestState qs) {
        _qs = qs;
    }

    void start() {
        _schedule = ThreadPoolManager.getInstance().schedule(this, _time);
    }

    @Override
    public void runImpl() {
        final QuestState qs = getQuestState();
        if (qs != null) {
            qs.removeQuestTimer(getName());
            qs.getQuest().notifyEvent(getName(), qs, getNpc());
        }
    }

    void pause() {
        if (_schedule != null) {
            _time = _schedule.getDelay(TimeUnit.SECONDS);
            _schedule.cancel(false);
        }
    }

    void stop() {
        if (_schedule != null) {
            _schedule.cancel(false);
        }
    }

    public boolean isActive() {
        return _schedule != null && !_schedule.isDone();
    }

    public String getName() {
        return _name;
    }

    public long getTime() {
        return _time;
    }

    public NpcInstance getNpc() {
        return _npc;
    }

    @Override
    public final String toString() {
        return _name;
    }

    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == getClass() && ((QuestTimer) o).getName().equals(getName()));
    }
}
