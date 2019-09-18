package ru.j2dev.commons.net.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class IOContext<IOCli extends IOClient<? extends IOContext<IOCli>>> {
    private final Logger LOGGER = LoggerFactory.getLogger(IOContext.class);
    private final InetSocketAddress socketAddress;
    private final IOServer<IOCli> ioServer;
    private final AsynchronousSocketChannel socketChannel;
    private final IIOSettings ioSettings;
    private final Queue<SendablePacket<IOCli>> sendQueue;
    private final Queue<ReceivablePacket<IOCli>> recvQueue;
    private final ByteBuffer recvBuffer;
    private final ByteBuffer sendBuffer;
    private final AtomicReference<EWriteState> pendingWriteState;
    private final AtomicBoolean isPendingClose;
    private IOCli ioClient;
    private IIOCipher ioCipher;
    private boolean isClosed;
    private StackTraceElement[] stackTraceClose;
    private String loginNameClose;

    public IOContext(final InetSocketAddress socketAddress, final IOServer<IOCli> ioServer, final AsynchronousSocketChannel socketChannel) {
        this.socketAddress = socketAddress;
        this.ioServer = ioServer;
        this.socketChannel = socketChannel;
        ioSettings = ioServer.getSettings();
        if (ioSettings.useDirectBuffer()) {
            recvBuffer = ByteBuffer.allocateDirect(65536).order(ByteOrder.LITTLE_ENDIAN);
            sendBuffer = ByteBuffer.allocateDirect(131072).order(ByteOrder.LITTLE_ENDIAN);
        } else {
            recvBuffer = ByteBuffer.wrap(new byte[131072]).order(ByteOrder.LITTLE_ENDIAN);
            sendBuffer = ByteBuffer.wrap(new byte[131072]).order(ByteOrder.LITTLE_ENDIAN);
        }
        sendQueue = new ArrayDeque<>();
        recvQueue = new IOExecQueue<>(ioServer.getSettings().getIOExecutor());
        pendingWriteState = new AtomicReference<>(EWriteState.IDLE);
        isPendingClose = new AtomicBoolean(false);
        isClosed = false;
    }

    public AsynchronousSocketChannel getSocketChannel() {
        return socketChannel;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public IOServer getIOServer() {
        return ioServer;
    }

    protected IIOCipher getIOCipher() {
        return ioCipher;
    }

    public void setIOCipher(final IIOCipher ioCipher) {
        this.ioCipher = ioCipher;
    }

    public IOCli getIOClient() {
        return ioClient;
    }

    public void setIOClient(final IOCli ioClient) {
        this.ioClient = ioClient;
    }

    protected void init(final IOCli ioClient) {
        try {
            if (ioSettings.useNoDelay()) {
                socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, false);
            }
            socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 4096);
            socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4096);
        } catch (final IOException ie) {
            LOGGER.warn("Cant set socket options", ie);
        }
        setIOClient(ioClient);
        submitReceiveHandler();
    }

    public boolean isClosed() {
        return (isPendingClose.get()) || (isClosed);
    }

    protected void closeChannel() {
        try {
            getSocketChannel().close();
        } catch (Exception ignored) {
        }
    }

    private void doClose(final boolean isForced) {
        doClose(isForced, true);
    }

    private void doClose() {
        doClose(true);
    }

    private void execCloseHanler(final boolean isForced) {
        if (getIOClient() != null) {
            if (isForced) {
                getIOClient().onForcedDisconnection();
            } else {
                getIOClient().onDisconnection();
            }
        }
    }

    private void doClose(final boolean isForced, final boolean execHandler) {
        try {
            if (isClosed) {
                return;
            }
            execCloseHanler(isForced);
        } catch (final Exception ex) {
            LOGGER.error(getIOClient().toString(), ex);
        } finally {
            isClosed = true;
            ioServer.closeChannelSilent(getSocketChannel());
            sendQueue.clear();
            sendBuffer.clear();
            recvBuffer.clear();
            if (getIOClient() != null) {
                loginNameClose = getIOClient().toString();
            }
            setIOClient(null);
            stackTraceClose = Thread.currentThread().getStackTrace();
        }
    }

    public void close(final SendablePacket<IOCli> lastPacket) {
        if (isClosed) {
            return;
        }
        if (lastPacket == null) {
            doClose(false, false);
        } else if (isPendingClose.compareAndSet(false, true)) {
            execCloseHanler(false);
            synchronized (sendQueue) {
                sendQueue.clear();
            }
            sendPacket(lastPacket);
        }
    }

    private int getRecvBufferPacketSize() {
        return 0x7FFF & recvBuffer.getShort(recvBuffer.position());
    }

    @SuppressWarnings("unchecked")
    private void readPacket(final int packetSize) {
        int packetHeaderPos = recvBuffer.position();
        int origBuffLimit = recvBuffer.limit();
        int packetBodyPos = packetHeaderPos + 2;
        int packetBodySize = packetSize - 2;
        if (packetSize <= 2) {
            LOGGER.warn("Received packet with empty body");
            recvBuffer.position(packetHeaderPos + 2);
            return;
        }
        try {
            recvBuffer.position(packetBodyPos);
            recvBuffer.limit(packetBodyPos + packetBodySize);
            boolean createPacket = true;
            if (getIOCipher() != null) {
                createPacket = getIOCipher().decrypt(recvBuffer, packetBodyPos, packetBodySize);
                recvBuffer.position(packetBodyPos);
                recvBuffer.limit(packetBodyPos + packetBodySize);
            }
            if (createPacket) {
                if (getIOClient() == null) {
                    final StringBuilder builder = new StringBuilder();
                    if (stackTraceClose != null) {
                        for (StackTraceElement stackTraceElement : stackTraceClose) {
                            builder.append(stackTraceElement.toString()).append("\n");
                        }
                    }
                    LOGGER.error("IOClient is null, old data: {}, trace: {}", loginNameClose, builder);
                }
                final ReceivablePacket<IOCli> receivedPacket = ioSettings.getPacketHandler().handle(recvBuffer, getIOClient());
                if (receivedPacket != null) {
                    receivedPacket.setClient(getIOClient());
                    receivedPacket.setBuffer(recvBuffer);
                    boolean doExecPacket = receivedPacket.read();
                    receivedPacket.setClient(getIOClient());
                    receivedPacket.setBuffer(null);
                    if (doExecPacket) {
                        ioSettings.getIOExecutor().execute(receivedPacket);
                    } else {
                        LOGGER.warn("{}: The created packet '{}[debug info: {}, protection: {}]' has not been read.", new Object[]{getIOClient(), receivedPacket.getClass().getSimpleName(), receivedPacket.getDebugInfo(), getIOCipher() != null ? getIOCipher().getClass().getSimpleName() : "null"});
                    }
                }
            } else {
                LOGGER.warn("The received packet has not been created.");
            }
        } catch (final Exception ex) {
            LOGGER.error("{} packed read failed", getIOClient(), ex);
        }
        recvBuffer.limit(origBuffLimit);
        recvBuffer.position(packetBodyPos + packetBodySize);
    }

    private void submitReceiveHandler() {
        getSocketChannel().read(recvBuffer, this, new CompletionHandler<Integer, IOContext<IOCli>>() {
            @Override
            public void completed(final Integer received, final IOContext<IOCli> context) {
                context.onReceived(received);
            }

            @Override
            public void failed(final Throwable reason, final IOContext<IOCli> context) {
                context.onReceiveFailed(reason);
            }
        });
    }

    protected void onReceived(final int received) {
        try {
            if (received < 0) {
                if (getIOServer().getSettings().closeOnReceiveEOS()) {
                    doClose(false, true);
                }
                return;
            }
            recvBuffer.flip();
            int packetSize;
            while ((recvBuffer.remaining() > 2) && (recvBuffer.remaining() >= (packetSize = getRecvBufferPacketSize()))) {
                readPacket(packetSize);
            }
            if (recvBuffer.hasRemaining()) {
                recvBuffer.compact();
            } else {
                recvBuffer.clear();
            }
            submitReceiveHandler();
        } catch (final Exception ex) {
            LOGGER.error("Exception while processing received packets at buffer position " + recvBuffer.position() + " and limit " + recvBuffer.limit(), ex);
            doClose();
        }
    }

    protected void onReceiveFailed(final Throwable reason) {
        doClose(!(reason instanceof IOException), true);
    }

    private void submitSendHandler() {
        getSocketChannel().write(sendBuffer, this, new CompletionHandler<Integer, IOContext<IOCli>>() {
            @Override
            public void completed(final Integer sended, final IOContext<IOCli> context) {
                context.onSended(sended);
            }

            @Override
            public void failed(final Throwable reason, final IOContext<IOCli> context) {
                context.onSendFailed(reason);
            }
        });
        getIOServer().pendingWriteTasks().getAndIncrement();
    }

    private boolean writePacket(final SendablePacket<IOCli> packet) {
        final int origBuffLimit = sendBuffer.limit();
        final int writeHeaderPos = sendBuffer.position();
        final int writeBodyPos = writeHeaderPos + 2;
        sendBuffer.position(writeBodyPos);
        sendBuffer.limit(sendBuffer.capacity());
        boolean succeed = false;
        try {
            succeed = packet.write(getIOClient(), sendBuffer);
        } catch (Exception ex) {
            LOGGER.warn("Cant write packet", ex);
        }
        if (!succeed) {
            LOGGER.warn("The packet write was unsuccessful.");
            sendBuffer.position(writeHeaderPos);
            sendBuffer.limit(origBuffLimit);
            return false;
        }
        int writeBodyEnd = sendBuffer.position();
        int packetBodySize = writeBodyEnd - writeBodyPos;
        sendBuffer.putShort(writeHeaderPos, (short) (packetBodySize + 2));
        if ((getIOCipher() != null) && (packet.isEnableEncrypt())) {
            sendBuffer.position(writeBodyPos);
            succeed = getIOCipher().encrypt(sendBuffer, writeBodyPos, packetBodySize);
            if (succeed) {
                sendBuffer.position(writeBodyEnd);
            } else {
                sendBuffer.position(writeHeaderPos);
                LOGGER.warn("Write packet encrypt was unsuccessful.");
            }
        }
        return succeed;
    }

    private void sendSendBuffer() {
        sendBuffer.flip();
        if (pendingWriteState.compareAndSet(EWriteState.WRITE, EWriteState.PENDING)) {
            submitSendHandler();
        } else {
            throw new IllegalStateException("Attempt to send not in WRITE state");
        }
    }

    private void sendEnqueuedPackets() {
        sendEnqueuedPackets(true);
    }

    private void sendEnqueuedPackets(final boolean sendWrited) {
        final int writeBeginPos = sendBuffer.position();
        sendBuffer.limit(sendBuffer.capacity());
        SendablePacket<IOCli> enqueuedPacket;
        while (sendBuffer.remaining() > 16384) {
            synchronized (sendQueue) {
                enqueuedPacket = sendQueue.peek();
            }
            if (enqueuedPacket == null) {
                break;
            }
            if (!writePacket(enqueuedPacket)) {
                LOGGER.warn("Packet {} wont be send.", enqueuedPacket.getClass().getSimpleName());
            }
            synchronized (sendQueue) {
                final Object topRemove = sendQueue.remove();
                if ((!isClosed) && (enqueuedPacket != topRemove)) {
                    throw new AssertionError("Send queue head != dequeued packet, last packet: " + enqueuedPacket.getClass().getSimpleName() + " remove: " + (topRemove != null ? topRemove.getClass().getSimpleName() : "null") + " client: " + getIOClient());
                }
            }
        }
        if (sendWrited) {
            sendSendBuffer();
        }
    }

    public void sendPacket(final SendablePacket<IOCli> sendPacket) {
        if (isClosed) {
            return;
        }
        synchronized (sendQueue) {
            sendQueue.offer(sendPacket);
        }
        if (pendingWriteState.compareAndSet(EWriteState.IDLE, EWriteState.WRITE)) {
            try {
                sendEnqueuedPackets();
            } catch (final Exception ex) {
                LOGGER.error("Exception while sending packets", ex);
                doClose();
            }
        }
    }

    private void onSended(final int sended) {
        if (isClosed) {
            return;
        }
        if (sended < 0) {
            doClose(false, true);
            return;
        }
        try {
            if (pendingWriteState.compareAndSet(EWriteState.PENDING, EWriteState.COMPLETION)) {
                boolean haveMoreData = false;
                boolean haveMorePackets = false;
                if (sendBuffer.hasRemaining()) {
                    sendBuffer.compact();
                    haveMoreData = true;
                } else {
                    sendBuffer.clear();
                    if (isPendingClose.get()) {
                        pendingWriteState.set(EWriteState.IDLE);
                        doClose(false);
                        return;
                    }
                }
                synchronized (sendQueue) {
                    haveMorePackets = !sendQueue.isEmpty();
                }
                if (haveMorePackets) {
                    if (pendingWriteState.compareAndSet(EWriteState.COMPLETION, EWriteState.WRITE)) {
                        sendEnqueuedPackets(false);
                        sendSendBuffer();
                    } else {
                        throw new IllegalStateException("Attempt to send packets not in COMPLETION state");
                    }
                } else if (haveMoreData) {
                    if (pendingWriteState.compareAndSet(EWriteState.COMPLETION, EWriteState.WRITE)) {
                        sendSendBuffer();
                    } else {
                        throw new IllegalStateException("Attempt to send data not in COMPLETION state");
                    }
                } else if (!pendingWriteState.compareAndSet(EWriteState.COMPLETION, EWriteState.IDLE)) {
                    throw new IllegalStateException("Attempt to idle not in COMPLETION state");
                }
            } else {
                throw new IllegalStateException("Attempt to completion not in PENDING state");
            }
        } catch (final Exception ex) {
            LOGGER.error("Exception while send completion", ex);
            doClose();
        } finally {
            getIOServer().pendingWriteTasks().decrementAndGet();
        }
    }

    private void onSendFailed(final Throwable reason) {
        getIOServer().pendingWriteTasks().decrementAndGet();
        doClose(!(reason instanceof IOException), true);
    }

    private enum EWriteState {
        IDLE,
        WRITE,
        PENDING,
        COMPLETION
    }
}