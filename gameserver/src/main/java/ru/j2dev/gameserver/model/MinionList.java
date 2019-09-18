package ru.j2dev.gameserver.model;

import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.instances.MinionInstance;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.templates.npc.MinionData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MinionList {
    private Set<MinionData> _minionData;
    private final Set<MinionInstance> _minions;
    private final Lock lock;
    private final MonsterInstance _master;

    public MinionList(final MonsterInstance master) {
        _master = master;
        _minions = new HashSet<>();
        (_minionData = new HashSet<>()).addAll(_master.getTemplate().getMinionData());
        lock = new ReentrantLock();
    }

    public boolean addMinion(final MinionData m) {
        lock.lock();
        try {
            return _minionData.add(m);
        } finally {
            lock.unlock();
        }
    }

    public void setMinionData(final Set<MinionData> minionData) {
        _minionData = minionData;
    }

    public boolean addMinion(final MinionInstance m) {
        lock.lock();
        try {
            return _minions.add(m);
        } finally {
            lock.unlock();
        }
    }

    public boolean hasAliveMinions() {
        lock.lock();
        try {
            for (final MinionInstance m : _minions) {
                if (m.isVisible() && !m.isDead()) {
                    return true;
                }
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    public boolean hasMinions() {
        return _minionData.size() > 0;
    }

    public List<MinionInstance> getAliveMinions() {
        final List<MinionInstance> result;
        lock.lock();
        try {
            result = _minions.stream().filter(m -> m.isVisible() && !m.isDead()).collect(Collectors.toCollection(() -> new ArrayList<>(_minions.size())));
        } finally {
            lock.unlock();
        }
        return result;
    }

    public void spawnMinions() {
        lock.lock();
        try {
            for (final MinionData minion : _minionData) {
                final int minionId = minion.getMinionId();
                int minionCount = minion.getAmount();
                for (final MinionInstance m : _minions) {
                    if (m.getNpcId() == minionId) {
                        minionCount--;
                    }
                    if (m.isDead() || !m.isVisible()) {
                        m.refreshID();
                        m.stopDecay();
                        _master.spawnMinion(m);
                    }
                }
                IntStream.range(0, minionCount).mapToObj(i -> new MinionInstance(IdFactory.getInstance().getNextId(), NpcTemplateHolder.getInstance().getTemplate(minionId))).forEach(m -> {
                    m.setLeader(_master);
                    _master.spawnMinion(m);
                    _minions.add(m);
                });
            }
        } finally {
            lock.unlock();
        }
    }

    public void unspawnMinions() {
        lock.lock();
        try {
            _minions.forEach(GameObject::decayMe);
        } finally {
            lock.unlock();
        }
    }

    public void deleteMinions() {
        lock.lock();
        try {
            _minions.forEach(GameObject::deleteMe);
            _minions.clear();
        } finally {
            lock.unlock();
        }
    }
}
