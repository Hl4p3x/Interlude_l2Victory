package ru.j2dev.authserver.crypt;

import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordHash {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordHash.class);

    private final String name;

    public PasswordHash(final String name) {
        this.name = name;
    }

    public boolean compare(final String password, final String expected) {
        try {
            return encrypt(password).equalsIgnoreCase(expected);
        } catch (Exception e) {
            LOGGER.error(name + ": encryption error!", e);
            return false;
        }
    }

    public String encrypt(final String password) throws Exception {
        final AbstractChecksum checksum = JacksumAPI.getChecksumInstance(name);
        checksum.setEncoding("BASE64");
        checksum.update(password.getBytes());
        return checksum.format("#CHECKSUM");
    }
}
