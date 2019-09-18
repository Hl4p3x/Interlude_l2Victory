package ru.j2dev.gameserver.network.authcomm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.network.authcomm.gs2as.AuthRequest;
import ru.j2dev.gameserver.network.lineage2.GameClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuthServerCommunication extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServerCommunication.class);
    private static final AuthServerCommunication instance = new AuthServerCommunication();

    private final Map<String, GameClient> waitingClients;
    private final Map<String, GameClient> authedClients;
    private final ReadWriteLock lock;
    private final Lock readLock;
    private final Lock writeLock;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;
    private final Queue<SendablePacket> sendQueue;
    private final Lock sendLock;
    private final AtomicBoolean isPengingWrite;
    private SelectionKey key;
    private Selector selector;
    private boolean shutdown;
    private boolean restart;

    private AuthServerCommunication() {
        waitingClients = new HashMap<>();
        authedClients = new HashMap<>();
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        readBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
        writeBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
        sendQueue = new ArrayDeque<>();
        sendLock = new ReentrantLock();
        isPengingWrite = new AtomicBoolean();
        try {
            selector = Selector.open();
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    public static AuthServerCommunication getInstance() {
        return instance;
    }

    private void connect() throws IOException {
        LOGGER.info("Connecting to authserver on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
        final SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        key = channel.register(selector, SelectionKey.OP_CONNECT);
        channel.connect(new InetSocketAddress(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT));
    }

    public void sendPacket(final SendablePacket packet) {
        if (isShutdown()) {
            return;
        }
        sendLock.lock();
        boolean wakeUp;
        try {
            sendQueue.add(packet);
            wakeUp = enableWriteInterest();
        } catch (CancelledKeyException e) {
            return;
        } finally {
            sendLock.unlock();
        }
        if (wakeUp) {
            selector.wakeup();
        }
    }

    private boolean disableWriteInterest() throws CancelledKeyException {
        if (isPengingWrite.compareAndSet(true, false)) {
            key.interestOps(key.interestOps() & 0xFFFFFFFB);
            return true;
        }
        return false;
    }

    private boolean enableWriteInterest() throws CancelledKeyException {
        if (!isPengingWrite.getAndSet(true)) {
            key.interestOps(key.interestOps() | 0x4);
            return true;
        }
        return false;
    }

    protected ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    protected ByteBuffer getWriteBuffer() {
        return writeBuffer;
    }

    @Override
    public void run() {
        while (!shutdown) {
            restart = false;
            try {
                Label_0250:
                while (!isShutdown()) {
                    connect();
                    long elapsed = System.currentTimeMillis();
                    final int selected = selector.select(5000L);
                    elapsed = System.currentTimeMillis() - elapsed;
                    if (selected == 0 && elapsed < 5000L) {
                        for (final SelectionKey key : selector.keys()) {
                            if (key.isValid()) {
                                final SocketChannel channel = (SocketChannel) key.channel();
                                if (channel != null && (key.interestOps() & 0x8) != 0x0) {
                                    connect(key);
                                    break Label_0250;
                                }
                            }
                        }
                    } else {
                        final Set<SelectionKey> keys = selector.selectedKeys();
                        if (keys.isEmpty()) {
                            throw new IOException("Connection timeout.");
                        }
                        final Iterator<SelectionKey> iterator = keys.iterator();
                        try {
                            while (iterator.hasNext()) {
                                final SelectionKey key = iterator.next();
                                iterator.remove();
                                final int opts = key.readyOps();
                                switch (opts) {
                                    case 8: {
                                        connect(key);
                                        break Label_0250;
                                    }
                                    default: {
                                        continue;
                                    }
                                }
                            }
                        } catch (CancelledKeyException e2) {
                            break;
                        }
                    }
                }
                while (!isShutdown()) {
                    selector.select();
                    final Set<SelectionKey> keys = selector.selectedKeys();
                    final Iterator<SelectionKey> iterator = keys.iterator();
                    try {
                        while (iterator.hasNext()) {
                            final SelectionKey key = iterator.next();
                            iterator.remove();
                            final int opts = key.readyOps();
                            switch (opts) {
                                case 4: {
                                    write(key);
                                    continue;
                                }
                                case 1: {
                                    read(key);
                                    continue;
                                }
                                case 5: {
                                    write(key);
                                    read(key);
                                    continue;
                                }
                            }
                        }
                        continue;
                    } catch (CancelledKeyException ignored) {
                    }
                    break;
                }
            } catch (IOException e) {
                LOGGER.error("AuthServer I/O error: " + e.getMessage());
            }
            close();
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void read(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        final ByteBuffer buf = getReadBuffer();
        final int count = channel.read(buf);
        if (count == -1) {
            throw new IOException("End of stream.");
        }
        if (count == 0) {
            return;
        }
        buf.flip();
        while (tryReadPacket(key, buf)) {
        }
    }

    private boolean tryReadPacket(final SelectionKey key, final ByteBuffer buf) throws IOException {
        final int pos = buf.position();
        if (buf.remaining() > 2) {
            int size = buf.getShort() & 0xFFFF;
            if (size <= 2) {
                throw new IOException("Incorrect packet size: <= 2");
            }
            size -= 2;
            if (size <= buf.remaining()) {
                final int limit = buf.limit();
                buf.limit(pos + size + 2);
                final ReceivablePacket rp = PacketHandler.handlePacket(buf);
                if (rp != null && rp.read()) {
                    ThreadPoolManager.getInstance().execute(rp);
                }
                buf.limit(limit);
                buf.position(pos + size + 2);
                if (!buf.hasRemaining()) {
                    buf.clear();
                    return false;
                }
                return true;
            } else {
                buf.position(pos);
            }
        }
        buf.compact();
        return false;
    }

    private void write(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        final ByteBuffer buf = getWriteBuffer();
        sendLock.lock();
        boolean done;
        try {
            int i = 0;
            SendablePacket sp;
            while (i++ < 64 && (sp = sendQueue.poll()) != null) {
                final int headerPos = buf.position();
                buf.position(headerPos + 2);
                sp.write();
                final int dataSize = buf.position() - headerPos - 2;
                if (dataSize == 0) {
                    buf.position(headerPos);
                } else {
                    buf.position(headerPos);
                    buf.putShort((short) (dataSize + 2));
                    buf.position(headerPos + dataSize + 2);
                }
            }
            done = sendQueue.isEmpty();
            if (done) {
                disableWriteInterest();
            }
        } finally {
            sendLock.unlock();
        }
        buf.flip();
        channel.write(buf);
        if (buf.remaining() > 0) {
            buf.compact();
            done = false;
        } else {
            buf.clear();
        }
        if (!done && enableWriteInterest()) {
            selector.wakeup();
        }
    }

    private void connect(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        channel.finishConnect();
        key.interestOps(key.interestOps() & 0xFFFFFFF7);
        key.interestOps(key.interestOps() | 0x1);
        sendPacket(new AuthRequest());
    }

    private void close() {
        restart = !shutdown;
        sendLock.lock();
        try {
            sendQueue.clear();
        } finally {
            sendLock.unlock();
        }
        readBuffer.clear();
        writeBuffer.clear();
        isPengingWrite.set(false);
        try {
            if (key != null) {
                key.channel().close();
                key.cancel();
            }
        } catch (IOException ignored) {
        }
        writeLock.lock();
        try {
            waitingClients.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public void shutdown() {
        shutdown = true;
        selector.wakeup();
    }

    public boolean isShutdown() {
        return shutdown || restart;
    }

    public void restart() {
        restart = true;
        selector.wakeup();
    }

    public GameClient addWaitingClient(final GameClient client) {
        writeLock.lock();
        try {
            return waitingClients.put(client.getLogin(), client);
        } finally {
            writeLock.unlock();
        }
    }

    public GameClient removeWaitingClient(final String account) {
        writeLock.lock();
        try {
            return waitingClients.remove(account);
        } finally {
            writeLock.unlock();
        }
    }

    public GameClient addAuthedClient(final GameClient client) {
        writeLock.lock();
        try {
            return authedClients.put(client.getLogin(), client);
        } finally {
            writeLock.unlock();
        }
    }

    public GameClient removeAuthedClient(final String login) {
        writeLock.lock();
        try {
            return authedClients.remove(login);
        } finally {
            writeLock.unlock();
        }
    }

    public GameClient getAuthedClient(final String login) {
        readLock.lock();
        try {
            return authedClients.get(login);
        } finally {
            readLock.unlock();
        }
    }

    public GameClient removeClient(final GameClient client) {
        writeLock.lock();
        try {
            if (client.isAuthed()) {
                return authedClients.remove(client.getLogin());
            }
            return waitingClients.remove(client.getSessionKey());
        } finally {
            writeLock.unlock();
        }
    }

    public String[] getAccounts() {
        readLock.lock();
        try {
            return authedClients.keySet().toArray(new String[0]);
        } finally {
            readLock.unlock();
        }
    }
}
