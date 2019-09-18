package ru.j2dev.gameserver.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.manager.ServerVariables;

import java.util.concurrent.atomic.AtomicLong;

public class GameStats {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameStats.class);
    private static final AtomicLong _updatePlayerBase = new AtomicLong(0L);
    private static final AtomicLong _playerEnterGameCounter = new AtomicLong(0L);
    private static final AtomicLong _taxSum = new AtomicLong(0L);
    private static final AtomicLong _rouletteSum = new AtomicLong(0L);
    private static final AtomicLong _adenaSum = new AtomicLong(0L);
    private static long _taxLastUpdate;
    private static long _rouletteLastUpdate;

    static {
        _taxSum.set(ServerVariables.getLong("taxsum", 0L));
        _rouletteSum.set(ServerVariables.getLong("rouletteSum", 0L));
    }

    public static void increaseUpdatePlayerBase() {
        _updatePlayerBase.incrementAndGet();
    }

    public static long getUpdatePlayerBase() {
        return _updatePlayerBase.get();
    }

    public static void incrementPlayerEnterGame() {
        _playerEnterGameCounter.incrementAndGet();
    }

    public static long getPlayerEnterGame() {
        return _playerEnterGameCounter.get();
    }

    public static void addTax(final long sum) {
        final long taxSum = _taxSum.addAndGet(sum);
        if (System.currentTimeMillis() - _taxLastUpdate < 10000L) {
            return;
        }
        _taxLastUpdate = System.currentTimeMillis();
        ServerVariables.set("taxsum", taxSum);
    }

    public static void addRoulette(final long sum) {
        final long rouletteSum = _rouletteSum.addAndGet(sum);
        if (System.currentTimeMillis() - _rouletteLastUpdate < 10000L) {
            return;
        }
        _rouletteLastUpdate = System.currentTimeMillis();
        ServerVariables.set("rouletteSum", rouletteSum);
    }

    public static long getTaxSum() {
        return _taxSum.get();
    }

    public static long getRouletteSum() {
        return _rouletteSum.get();
    }

    public static void addAdena(final long sum) {
        _adenaSum.addAndGet(sum);
    }

    public static long getAdena() {
        return _adenaSum.get();
    }
}
