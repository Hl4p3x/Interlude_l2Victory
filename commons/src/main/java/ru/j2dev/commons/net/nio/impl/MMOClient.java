package ru.j2dev.commons.net.nio.impl;

import java.nio.ByteBuffer;

public abstract class MMOClient<T extends MMOConnection> {
    private T _connection;
    private boolean isAuthed;

    public MMOClient(final T con) {
        _connection = con;
    }

    public T getConnection() {
        return _connection;
    }

    protected void setConnection(final T con) {
        _connection = con;
    }

    public boolean isAuthed() {
        return isAuthed;
    }

    public void setAuthed(final boolean isAuthed) {
        this.isAuthed = isAuthed;
    }

    public void closeNow(final boolean error) {
        if (isConnected()) {
            _connection.closeNow();
        }
    }

    public void closeLater() {
        if (isConnected()) {
            _connection.closeLater();
        }
    }

    public boolean isConnected() {
        return _connection != null && !_connection.isClosed();
    }

    public abstract boolean decrypt(final ByteBuffer p0, final int p1);

    public abstract boolean encrypt(final ByteBuffer p0, final int p1);

    protected void onDisconnection() {
    }

    protected void onForcedDisconnection() {
    }
}
