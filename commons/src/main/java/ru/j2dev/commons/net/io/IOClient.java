package ru.j2dev.commons.net.io;

public class IOClient<IOCtx extends IOContext> {
    private final IOCtx ioContext;

    public IOClient(final IOCtx ioContext) {
        this.ioContext = ioContext;
    }

    protected IOCtx getIOContext() {
        return ioContext;
    }

    protected void onDisconnection() {
    }

    protected void onForcedDisconnection() {
    }
}