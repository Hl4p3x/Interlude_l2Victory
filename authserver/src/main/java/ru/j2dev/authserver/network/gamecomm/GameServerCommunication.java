package ru.j2dev.authserver.network.gamecomm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.authserver.ThreadPoolManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;

public class GameServerCommunication extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameServerCommunication.class);
    private static final GameServerCommunication instance = new GameServerCommunication();

    private final ByteBuffer writeBuffer;
    private Selector selector;
    private boolean shutdown;

    private GameServerCommunication() {
        writeBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
    }

    public static GameServerCommunication getInstance() {
        return instance;
    }

    public void openServerSocket(final InetAddress address, final int tcpPort) throws IOException {
        selector = Selector.open();
        final ServerSocketChannel selectable = ServerSocketChannel.open();
        selectable.configureBlocking(false);
        selectable.socket().bind((address == null) ? new InetSocketAddress(tcpPort) : new InetSocketAddress(address, tcpPort));
        selectable.register(selector, selectable.validOps());
    }

    @Override
    public void run() {
        SelectionKey key = null;
        while (!isShutdown()) {
            try {
                selector.select();
                final Set<SelectionKey> keys = selector.selectedKeys();
                final Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                    if (!key.isValid()) {
                        close(key);
                    } else {
                        final int opts = key.readyOps();

                        switch (opts) {
                            case SelectionKey.OP_CONNECT:
                                close(key);
                                break;
                            case SelectionKey.OP_ACCEPT:
                                accept(key);
                                break;
                            case SelectionKey.OP_WRITE:
                                write(key);
                                break;
                            case SelectionKey.OP_READ:
                                read(key);
                                break;
                            case SelectionKey.OP_READ | SelectionKey.OP_WRITE:
                                write(key);
                                read(key);
                                break;
                        }
                    }
                }
            } catch (ClosedSelectorException e3) {
                LOGGER.error("Selector " + selector + " closed!");
                return;
            } catch (IOException e) {
                LOGGER.error("Gameserver I/O error: " + e.getMessage());
                close(key);
            } catch (Exception e2) {
                LOGGER.error("", e2);
            }
        }
    }

    public void accept(final SelectionKey key) throws IOException {
        final ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        final SocketChannel sc = ssc.accept();
        sc.configureBlocking(false);
        final SelectionKey clientKey = sc.register(selector, SelectionKey.OP_READ);
        final GameServerConnection conn;
        clientKey.attach(conn = new GameServerConnection(clientKey));
        conn.setGameServer(new GameServer(conn));
    }

    public void read(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        final GameServerConnection conn = (GameServerConnection) key.attachment();
        final GameServer gs = conn.getGameServer();
        final ByteBuffer buf = conn.getReadBuffer();
        final int count = channel.read(buf);
        if (count == -1) {
            close(key);
            return;
        }
        if (count == 0) {
            return;
        }
        buf.flip();
        while (tryReadPacket(key, gs, buf)) {
        }
    }

    protected boolean tryReadPacket(final SelectionKey key, final GameServer gs, final ByteBuffer buf) throws IOException {
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
                final ReceivablePacket rp = PacketHandler.handlePacket(gs, buf);
                if (rp != null) {
                    rp.setByteBuffer(buf);
                    rp.setClient(gs);
                    if (rp.read()) {
                        ThreadPoolManager.getInstance().execute(rp);
                    }
                    rp.setByteBuffer(null);
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

    public void write(final SelectionKey key) throws IOException {
        final GameServerConnection conn = (GameServerConnection) key.attachment();
        final GameServer gs = conn.getGameServer();
        final SocketChannel channel = (SocketChannel) key.channel();
        final ByteBuffer buf = getWriteBuffer();
        conn.disableWriteInterest();
        final Queue<SendablePacket> sendQueue = conn.sendQueue;
        final Lock sendLock = conn.sendLock;
        sendLock.lock();
        boolean done;
        try {
            int i = 0;
            SendablePacket sp;
            while (i++ < 64 && (sp = sendQueue.poll()) != null) {
                final int headerPos = buf.position();
                buf.position(headerPos + 2);
                sp.setByteBuffer(buf);
                sp.setClient(gs);
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
                conn.disableWriteInterest();
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
        if (!done && conn.enableWriteInterest()) {
            selector.wakeup();
        }
    }

    private ByteBuffer getWriteBuffer() {
        return writeBuffer;
    }

    public void close(final SelectionKey key) {
        if (key == null) {
            return;
        }
        try {
            try {
                final GameServerConnection conn = (GameServerConnection) key.attachment();
                if (conn != null) {
                    conn.onDisconnection();
                }
            } finally {
                key.channel().close();
                key.cancel();
            }
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(final boolean shutdown) {
        this.shutdown = shutdown;
    }
}
