package ru.j2dev.commons.util;

import java.io.File;
import java.util.Comparator;

public class NaturalOrderComparator implements Comparator {
    public static final Comparator<String> STRING_COMPARATOR = NaturalOrderComparator::compare0;
    public static final Comparator<File> FILE_NAME_COMPARATOR = (o1, o2) -> compare0(o1.getName(), o2.getName());
    public static final Comparator<File> ABSOLUTE_FILE_NAME_COMPARATOR = (o1, o2) -> compare0(o1.getAbsolutePath(), o2.getAbsolutePath());

    private static int compareRight(final String a, final String b) {
        int bias = 0;
        int ia = 0;
        int ib = 0;
        while (true) {
            final char ca = charAt(a, ia);
            final char cb = charAt(b, ib);
            if (!Character.isDigit(ca) && !Character.isDigit(cb)) {
                return bias;
            }
            if (!Character.isDigit(ca)) {
                return -1;
            }
            if (!Character.isDigit(cb)) {
                return 1;
            }
            if (ca < cb) {
                if (bias == 0) {
                    bias = -1;
                }
            } else if (ca > cb) {
                if (bias == 0) {
                    bias = 1;
                }
            } else if (ca == '\0') {
                return bias;
            }
            ia++;
            ib++;
        }
    }

    private static int compare0(final String a, final String b) {
        int ia = 0;
        int ib = 0;
        int nza = 0;
        int nzb = 0;
        while (true) {
            nzb = (nza = 0);
            char ca = charAt(a, ia);
            char cb = charAt(b, ib);
            while (Character.isSpaceChar(ca) || ca == '0') {
                if (ca == '0') {
                    ++nza;
                } else {
                    nza = 0;
                }
                ca = charAt(a, ia++);
            }
            while (Character.isSpaceChar(cb) || cb == '0') {
                if (cb == '0') {
                    ++nzb;
                } else {
                    nzb = 0;
                }
                cb = charAt(b, ib++);
            }
            final int result;
            if (Character.isDigit(ca) && Character.isDigit(cb) && (result = compareRight(a.substring(ia), b.substring(ib))) != 0) {
                return result;
            }
            if (ca == '\0' && cb == '\0') {
                return nza - nzb;
            }
            if (ca < cb) {
                return -1;
            }
            if (ca > cb) {
                return 1;
            }
            ia++;
            ib++;
        }
    }

    private static char charAt(final String s, final int i) {
        if (i >= s.length()) {
            return '\0';
        }
        return s.charAt(i);
    }

    @Override
    public int compare(final Object o1, final Object o2) {
        final String a = o1.toString();
        final String b = o2.toString();
        return compare0(a, b);
    }
}
