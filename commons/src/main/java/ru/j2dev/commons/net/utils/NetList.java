package ru.j2dev.commons.net.utils;

import java.util.ArrayList;
import java.util.Iterator;

public final class NetList extends ArrayList<Net> {
    private static final long serialVersionUID = 4266033257195615387L;

    public boolean isInRange(final String address) {
        for (final Net net : this) {
            if (net.isInRange(address)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<Net> itr = iterator();
        while (itr.hasNext()) {
            sb.append(itr.next());
            if (itr.hasNext()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }
}
