package ru.j2dev.commons.net.io;

import java.nio.ByteBuffer;

public abstract class ReceivablePacket<IOCli extends IOClient<? extends IOContext<IOCli>>> extends BasePacket<IOCli> implements Runnable {
    private IOCli client;
    private ByteBuffer buffer;
    @Deprecated
    private StringBuilder debugInfo = new StringBuilder();

    protected abstract boolean read();

    @Override
    public abstract void run();

    protected int getAvaliableBytes() {
        return getBuffer().remaining();
    }

    protected void readB(final byte[] dst) {
        getBuffer().get(dst);
    }

    protected void readB(final byte[] dst, final int offset, final int len) {
        getBuffer().get(dst, offset, len);
    }

    protected int readC() {
        return getBuffer().get() & 0xFF;
    }

    protected int readH() {
        return getBuffer().getShort() & 0xFFFF;
    }

    protected int readD() {
        return getBuffer().getInt();
    }

    protected long readQ() {
        return getBuffer().getLong();
    }

    protected double readF() {
        return getBuffer().getDouble();
    }

    //TODO[K]
    @Deprecated
    protected String readS() {
        final StringBuilder sb = new StringBuilder();
        char ch;
        while ((ch = getBuffer().getChar()) != '\0') {
            sb.append(ch);
            debugInfo.append(ch);
        }
        debugInfo = null;
        return sb.toString();
    }

    public IOCli getClient() {
        return client;
    }

    public void setClient(final IOCli client) {
        this.client = client;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Deprecated
    public String getDebugInfo() {
        return debugInfo.toString();
    }
}