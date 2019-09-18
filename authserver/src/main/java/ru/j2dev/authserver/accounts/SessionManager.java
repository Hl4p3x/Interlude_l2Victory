package ru.j2dev.authserver.accounts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.authserver.ThreadPoolManager;
import ru.j2dev.authserver.network.l2.SessionKey;
import ru.j2dev.commons.threading.RunnableImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SessionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);
    private static final SessionManager _instance = new SessionManager();

    private final Map<SessionKey, Session> sessions;
    private final Lock lock;

    private SessionManager() {
        sessions = new HashMap<>();
        lock = new ReentrantLock();
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl() {
            @Override
            public void runImpl() {
                lock.lock();
                try {
                    final long currentMillis = System.currentTimeMillis();
                    sessions.values().removeIf(session -> session.getExpireTime() < currentMillis);
                } finally {
                    lock.unlock();
                }
            }
        }, 30000L, 30000L);
    }

    public static SessionManager getInstance() {
        return _instance;
    }

    public Session openSession(final Account account) {
        lock.lock();
        try {
            final Session session = new Session(account);
            sessions.put(session.getSessionKey(), session);
            return session;
        } finally {
            lock.unlock();
        }
    }

    public Session closeSession(final SessionKey skey) {
        lock.lock();
        try {
            return sessions.remove(skey);
        } finally {
            lock.unlock();
        }
    }

    public Session getSessionByName(final String name) {
        return sessions.values().stream().filter(session -> session.account.getLogin().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public final class Session {
        private final Account account;
        private final SessionKey skey;
        private final long expireTime;

        private Session(final Account account) {
            this.account = account;
            skey = SessionKey.create();
            expireTime = System.currentTimeMillis() + 60000L;
        }

        public SessionKey getSessionKey() {
            return skey;
        }

        public Account getAccount() {
            return account;
        }

        public long getExpireTime() {
            return expireTime;
        }
    }
}
