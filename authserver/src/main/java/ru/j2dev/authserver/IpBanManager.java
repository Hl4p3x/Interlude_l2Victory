package ru.j2dev.authserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IpBanManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpBanManager.class);
    private static final IpBanManager _instance = new IpBanManager();

    private final Map<String, IpSession> ips;
    private final Lock readLock;
    private final Lock writeLock;

    private IpBanManager() {
        ips = new HashMap<>();
        ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
            final long currentMillis = System.currentTimeMillis();
            writeLock.lock();
            try {
                ips.values().removeIf(session -> session.banExpire < currentMillis && session.lastTry < currentMillis - Config.LOGIN_TRY_TIMEOUT);
            } finally {
                writeLock.unlock();
            }
        }, 1000L, 1000L);
    }

    public static IpBanManager getInstance() {
        return _instance;
    }

    public boolean isIpBanned(final String ip) {
        if (Config.WHITE_IPS.contains(ip)) {
            return false;
        }
        readLock.lock();
        try {
            final IpSession ipsession;
            return (ipsession = ips.get(ip)) != null && ipsession.banExpire > System.currentTimeMillis();
        } finally {
            readLock.unlock();
        }
    }

    public boolean tryLogin(final String ip, boolean success) {
        if (Config.WHITE_IPS.contains(ip)) {
            return true;
        }
        writeLock.lock();
        try {
            IpSession ipsession = ips.computeIfAbsent(ip, k -> new IpSession());
            final long currentMillis = System.currentTimeMillis();
            if (currentMillis - ipsession.lastTry < Config.LOGIN_TRY_TIMEOUT) {
                success = false;
            }
            if (success) {
                if (ipsession.tryCount > 0) {
                    ipsession.tryCount--;
                }
            } else if (ipsession.tryCount < Config.LOGIN_TRY_BEFORE_BAN) {
                ipsession.tryCount++;
            }
            ipsession.lastTry = currentMillis;
            if (ipsession.tryCount == Config.LOGIN_TRY_BEFORE_BAN) {
                LOGGER.warn("IpBanManager: " + ip + " banned for " + Config.IP_BAN_TIME / 1000L + " seconds.");
                ipsession.banExpire = currentMillis + Config.IP_BAN_TIME;
                return false;
            }
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    private class IpSession {
        public int tryCount;
        public long lastTry;
        public long banExpire;
    }
}
