package ru.j2dev.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Request extends MultiValueSet<String> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);
    private static final AtomicInteger _nextId = new AtomicInteger();

    private final int _id;
    private L2RequestType _type;
    private HardReference<Player> _requestor;
    private HardReference<Player> _reciever;
    private boolean _isRequestorConfirmed;
    private boolean _isRecieverConfirmed;
    private boolean _isCancelled;
    private boolean _isDone;
    private long _timeout;
    private Future<?> _timeoutTask;

    public Request(final L2RequestType type, final Player requestor, final Player reciever) {
        _id = _nextId.incrementAndGet();
        _requestor = requestor.getRef();
        _reciever = reciever.getRef();
        _type = type;
        requestor.setRequest(this);
        reciever.setRequest(this);
    }

    public Request setTimeout(final long timeout) {
        _timeout = ((timeout > 0L) ? (System.currentTimeMillis() + timeout) : 0L);
        _timeoutTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                timeout();
            }
        }, timeout);
        return this;
    }

    public int getId() {
        return _id;
    }

    public void cancel() {
        _isCancelled = true;
        if (_timeoutTask != null) {
            _timeoutTask.cancel(false);
        }
        _timeoutTask = null;
        Player player = getRequestor();
        if (player != null && player.getRequest() == this) {
            player.setRequest(null);
        }
        player = getReciever();
        if (player != null && player.getRequest() == this) {
            player.setRequest(null);
        }
    }

    public void done() {
        _isDone = true;
        if (_timeoutTask != null) {
            _timeoutTask.cancel(false);
        }
        _timeoutTask = null;
        Player player = getRequestor();
        if (player != null && player.getRequest() == this) {
            player.setRequest(null);
        }
        player = getReciever();
        if (player != null && player.getRequest() == this) {
            player.setRequest(null);
        }
    }

    public void timeout() {
        final Player player = getReciever();
        if (player != null && player.getRequest() == this) {
            player.sendPacket(Msg.TIME_EXPIRED);
        }
        cancel();
    }

    public Player getOtherPlayer(final Player player) {
        if (player == getRequestor()) {
            return getReciever();
        }
        if (player == getReciever()) {
            return getRequestor();
        }
        return null;
    }

    public Player getRequestor() {
        return _requestor.get();
    }

    public Player getReciever() {
        return _reciever.get();
    }

    public boolean isInProgress() {
        return !_isCancelled && !_isDone && (_timeout == 0L || _timeout > System.currentTimeMillis());
    }

    public boolean isTypeOf(final L2RequestType type) {
        return _type == type;
    }

    public void confirm(final Player player) {
        if (player == getRequestor()) {
            _isRequestorConfirmed = true;
        } else if (player == getReciever()) {
            _isRecieverConfirmed = true;
        }
    }

    public boolean isConfirmed(final Player player) {
        if (player == getRequestor()) {
            return _isRequestorConfirmed;
        }
        return player == getReciever() && _isRecieverConfirmed;
    }

    public enum L2RequestType {
        CUSTOM,
        PARTY,
        PARTY_ROOM,
        CLAN,
        ALLY,
        TRADE,
        TRADE_REQUEST,
        FRIEND,
        CHANNEL,
        DUEL
    }
}
