package ru.j2dev.commons.net.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class IOServer<IOCli extends IOClient<? extends IOContext<IOCli>>> {
    private final Logger LOGGER = LoggerFactory.getLogger(IOServer.class);

    private final IIOSettings<?> settings;
    private final AtomicInteger pendingWriteTasks;
    private boolean isShutdown;
    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverSocketChannel;

    public IOServer(final IIOSettings<?> settings) {
        this.settings = settings;
        pendingWriteTasks = new AtomicInteger(0);
        isShutdown = false;
    }

    IIOSettings getSettings() {
        return settings;
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    AtomicInteger pendingWriteTasks() {
        return pendingWriteTasks;
    }

    public void startup() throws IOException {
        getSettings().getIOExecutor().startup();
        if (channelGroup == null) {
            channelGroup = AsynchronousChannelGroup.withThreadPool(getSettings().getIOExecutor());
        }
        if (serverSocketChannel == null) {
            serverSocketChannel = AsynchronousServerSocketChannel.open(channelGroup);
        }
        settings.getAcceptFilter();
        settings.getIOExecutor();
        settings.getPacketHandler();
        settings.getClientFactory();
        serverSocketChannel = serverSocketChannel.bind(getSettings().getBindAddress(), getSettings().getBindBacklog());
        submitAcceptHandler();
        LOGGER.info("Waiting for connections at {}:{}", ((InetSocketAddress) serverSocketChannel.getLocalAddress()).getHostString(), ((InetSocketAddress) serverSocketChannel.getLocalAddress()).getPort());
    }

    public void shutdown() throws IOException {
        isShutdown = true;
        if (serverSocketChannel != null) {
            LOGGER.info("Shutdown network server of {}:{}", ((InetSocketAddress) serverSocketChannel.getLocalAddress()).getHostString(), ((InetSocketAddress) serverSocketChannel.getLocalAddress()).getPort());
            serverSocketChannel.close();
            serverSocketChannel = null;
        }
        try {
            if (channelGroup != null) {
                channelGroup.shutdown();
                channelGroup.awaitTermination(5000L, TimeUnit.MILLISECONDS);
                channelGroup = null;
            }
            getSettings().getIOExecutor().shutdown();
            getSettings().getIOExecutor().awaitTermination(5000L, TimeUnit.MILLISECONDS);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    void closeChannelSilent(final AsynchronousSocketChannel channel) {
        try {
            channel.shutdownInput();
            channel.shutdownOutput();
        } catch (final IOException ignored) {
        }
        try {
            channel.close();
        } catch (final IOException ignored) {
        }
    }

    private void submitAcceptHandler() {
        if (isShutdown()) {
            return;
        }
        try {
            serverSocketChannel.accept(this, new CompletionHandler<AsynchronousSocketChannel, IOServer<IOCli>>() {
                @Override
                public void completed(final AsynchronousSocketChannel socketChannel, final IOServer server) {
                    if (server.isShutdown()) {
                        return;
                    }
                    server.onAccept(socketChannel);
                }

                @Override
                public void failed(final Throwable reason, final IOServer server) {
                    if (server.isShutdown()) {
                        return;
                    }
                    server.onAcceptFailed(reason);
                }
            });
        } catch (final Exception ex) {
            LOGGER.error("Exception while submit accept handler", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void onAccept(final AsynchronousSocketChannel socketChannel) {
        submitAcceptHandler();
        try {
            final InetSocketAddress socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
            if (settings.getAcceptFilter() == null || settings.getAcceptFilter().accept(socketAddress)) {
                final IOContext<IOCli> ioContext = new IOContext<>(socketAddress, this, socketChannel);
                final IOCli client = (IOCli) getSettings().getClientFactory().create(ioContext);
                ioContext.init(client);
            } else {
                closeChannelSilent(socketChannel);
            }
        } catch (final Exception ex) {
            LOGGER.error("Exception while processing accepted connection", ex);
        }
    }

    private void onAcceptFailed(final Throwable reason) {
        LOGGER.error("Exception while while accepting connection", reason);
        submitAcceptHandler();
    }
}