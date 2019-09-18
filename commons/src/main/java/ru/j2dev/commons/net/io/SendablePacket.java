package ru.j2dev.commons.net.io;

import java.nio.ByteBuffer;

public abstract class SendablePacket<IOCli extends IOClient<? extends IOContext<IOCli>>> extends BasePacket<IOCli> {
    protected abstract boolean write(final IOCli p0, final ByteBuffer p1);

    public boolean isEnableEncrypt() {
        return true;
    }

    protected class PacketBody {
        private final IOCli client;
        private final ByteBuffer buffer;

        public PacketBody(final IOCli client, final ByteBuffer buffer) {
            this.client = client;
            this.buffer = buffer;
        }

        public IOCli getClient() {
            return client;
        }

        public void writeC(final boolean val) {
            writeC(val ? 1 : 0);
        }

        public void writeC(final int val) {
            buffer.put((byte) val);
        }

        public void writeF(final double val) {
            buffer.putDouble(val);
        }

        public void writeH(final int val) {
            buffer.putShort((short) val);
        }

        public void writeD(final int val) {
            buffer.putInt(val);
        }

        public void writeD(final boolean val) {
            buffer.putInt(val ? 1 : 0);
        }

        public void writeQ(final long val) {
            buffer.putLong(val);
        }

        public void writeB(final byte[] val) {
            buffer.put(val);
        }

        //TODO[K]
        public void writeS(String value) {
            if (value != null) {
                for (int i = 0; i < value.length(); i++) {
                    buffer.putChar(value.charAt(i));
                }
            }

            buffer.putChar('\0');
        }
    }
}