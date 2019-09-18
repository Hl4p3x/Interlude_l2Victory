package ru.j2dev.commons.net.utils;

public class Net {
    private final int address;
    private final int netmask;

    public Net(final int net, final int mask) {
        address = net;
        netmask = mask;
    }

    public static Net valueOf(final String s) {
        int address = 0;
        int netmask = 0;
        final String[] mask = s.trim().split("\\b\\/\\b");
        if (mask.length < 1 || mask.length > 2) {
            throw new IllegalArgumentException("For input string: \"" + s + "\"");
        }
        if (mask.length == 1) {
            final String[] octets = mask[0].split("\\.");
            if (octets.length < 1 || octets.length > 4) {
                throw new IllegalArgumentException("For input string: \"" + s + "\"");
            }
            for (int i = 1; i <= octets.length; ++i) {
                if (!"*".equals(octets[i - 1])) {
                    address |= Integer.parseInt(octets[i - 1]) << 32 - i * 8;
                    netmask |= 255 << 32 - i * 8;
                }
            }
        } else {
            address = parseAddress(mask[0]);
            netmask = parseNetmask(mask[1]);
        }
        return new Net(address, netmask);
    }

    public static int parseAddress(final String s) throws IllegalArgumentException {
        int ip = 0;
        final String[] octets = s.split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException("For input string: \"" + s + "\"");
        }
        for (int i = 1; i <= octets.length; ++i) {
            ip |= Integer.parseInt(octets[i - 1]) << 32 - i * 8;
        }
        return ip;
    }

    public static int parseNetmask(final String s) throws IllegalArgumentException {
        int mask = 0;
        final String[] octets = s.split("\\.");
        if (octets.length == 1) {
            final int bitmask = Integer.parseInt(octets[0]);
            if (bitmask < 0 || bitmask > 32) {
                throw new IllegalArgumentException("For input string: \"" + s + "\"");
            }
            mask = -1 << 32 - bitmask;
        } else {
            for (int i = 1; i <= octets.length; ++i) {
                mask |= Integer.parseInt(octets[i - 1]) << 32 - i * 8;
            }
        }
        return mask;
    }

    public int address() {
        return address;
    }

    public int netmask() {
        return netmask;
    }

    public boolean isInRange(final int address) {
        return (address & netmask) == this.address;
    }

    public boolean isInRange(final String address) {
        return isInRange(parseAddress(address));
    }

    @Override
    public boolean equals(final Object o) {
        return o == this || (o instanceof Net && ((Net) o).address() == address && ((Net) o).netmask() == netmask);
    }

    @Override
    public String toString() {
        return (address >>> 24) + "." +
                (address << 8 >>> 24) + "." +
                (address << 16 >>> 24) + "." +
                (address << 24 >>> 24) +
                "/" +
                (netmask >>> 24) + "." +
                (netmask << 8 >>> 24) + "." +
                (netmask << 16 >>> 24) + "." +
                (netmask << 24 >>> 24);
    }
}
