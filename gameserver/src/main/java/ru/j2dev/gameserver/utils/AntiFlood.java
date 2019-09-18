package ru.j2dev.gameserver.utils;


import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.hash.TIntLongHashMap;

public class AntiFlood {
    private final TIntLongHashMap _recentReceivers = new TIntLongHashMap();
    private long _lastSent;
    private String _lastText = "";
    private long _lastHeroTime;
    private long _lastTradeTime;
    private long _lastShoutTime;
    private long _lastMailTime;

    public boolean canTrade(final String text) {
        final long currentMillis = System.currentTimeMillis();
        if (currentMillis - _lastTradeTime < 5000L) {
            return false;
        }
        _lastTradeTime = currentMillis;
        return true;
    }

    public boolean canShout(final String text) {
        final long currentMillis = System.currentTimeMillis();
        if (currentMillis - _lastShoutTime < 5000L) {
            return false;
        }
        _lastShoutTime = currentMillis;
        return true;
    }

    public boolean canHero(final String text) {
        final long currentMillis = System.currentTimeMillis();
        if (currentMillis - _lastHeroTime < 10000L) {
            return false;
        }
        _lastHeroTime = currentMillis;
        return true;
    }

    public boolean canMail() {
        final long currentMillis = System.currentTimeMillis();
        if (currentMillis - _lastMailTime < 10000L) {
            return false;
        }
        _lastMailTime = currentMillis;
        return true;
    }

    public boolean canTell(final int charId, final String text) {
        final long currentMillis = System.currentTimeMillis();
        final TIntLongIterator itr = _recentReceivers.iterator();
        int recent = 0;
        while (itr.hasNext()) {
            itr.advance();
            final long lastSent = itr.value();
            if (currentMillis - lastSent < (text.equalsIgnoreCase(_lastText) ? 600000L : 60000L)) {
                ++recent;
            } else {
                itr.remove();
            }
        }
        long lastSent = _recentReceivers.put(charId, currentMillis);
        long delay = 333L;
        if (recent > 3) {
            lastSent = _lastSent;
            delay = (recent - 3) * 3333L;
        }
        _lastText = text;
        _lastSent = currentMillis;
        return currentMillis - lastSent > delay;
    }
}
