package ru.j2dev.commons.net.nio.impl;

import java.nio.ByteOrder;

public class SelectorConfig {
    public int READ_BUFFER_SIZE;
    public int WRITE_BUFFER_SIZE;
    public int MAX_SEND_PER_PASS;
    public long SLEEP_TIME;
    public long INTEREST_DELAY;
    public int HEADER_SIZE;
    public int PACKET_SIZE;
    public int HELPER_BUFFER_COUNT;
    public ByteOrder BYTE_ORDER;

    public SelectorConfig() {
        READ_BUFFER_SIZE = 65536;
        WRITE_BUFFER_SIZE = 131072;
        MAX_SEND_PER_PASS = 32;
        SLEEP_TIME = 10L;
        INTEREST_DELAY = 30L;
        HEADER_SIZE = 2;
        PACKET_SIZE = 32768;
        HELPER_BUFFER_COUNT = 64;
        BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    }
}
