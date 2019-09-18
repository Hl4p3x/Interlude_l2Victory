package ru.j2dev.commons.net.nio;

public abstract class SendablePacket<T> extends AbstractPacket<T> {
    protected void writeC(final int data) {
        getByteBuffer().put((byte) data);
    }

    protected void writeF(final double value) {
        getByteBuffer().putDouble(value);
    }

    protected void writeH(final int value) {
        getByteBuffer().putShort((short) value);
    }

    protected void writeD(final int value) {
        getByteBuffer().putInt(value);
    }

    protected void writeQ(final long value) {
        getByteBuffer().putLong(value);
    }

    protected void writeB(final byte[] data) {
        getByteBuffer().put(data);
    }

    protected void writeS(final CharSequence charSequence) {
        if (charSequence != null) {
            for (int length = charSequence.length(), i = 0; i < length; ++i) {
                getByteBuffer().putChar(charSequence.charAt(i));
            }
        }
        getByteBuffer().putChar('\0');
    }

    protected abstract boolean write();
}
