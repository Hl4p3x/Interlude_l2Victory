package ru.j2dev.gameserver.utils;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class Strings {
    private static final Logger LOGGER = LoggerFactory.getLogger(Strings.class);
    private static String[] tr;
    private static String[] trb;
    private static String[] trcode;

    public static String stripSlashes(String s) {
        if (s == null) {
            return "";
        }
        s = s.replace("\\'", "'");
        s = s.replace("\\\\", "\\");
        return s;
    }

    public static Boolean parseBoolean(final Object x) {
        if (x == null) {
            return false;
        }
        if (x instanceof Number) {
            return ((Number) x).intValue() > 0;
        }
        if (x instanceof Boolean) {
            return (Boolean) x;
        }
        if (x instanceof Double) {
            return Math.abs((double) x) < 1.0E-5;
        }
        return !String.valueOf(x).isEmpty();
    }

    public static void reload() {
        try {
            String[] pairs = FileUtils.readFileToString(new File(Config.DATAPACK_ROOT, "data/translit.txt")).split("\n");
            tr = new String[pairs.length * 2];
            for (int i = 0; i < pairs.length; ++i) {
                final String[] ss = pairs[i].split(" +");
                tr[i * 2] = ss[0];
                tr[i * 2 + 1] = ss[1];
            }
            pairs = FileUtils.readFileToString(new File(Config.DATAPACK_ROOT, "data/translit_back.txt")).split("\n");
            trb = new String[pairs.length * 2];
            for (int i = 0; i < pairs.length; ++i) {
                final String[] ss = pairs[i].split(" +");
                trb[i * 2] = ss[0];
                trb[i * 2 + 1] = ss[1];
            }
            pairs = FileUtils.readFileToString(new File(Config.DATAPACK_ROOT, "data/transcode.txt")).split("\n");
            trcode = new String[pairs.length * 2];
            for (int i = 0; i < pairs.length; ++i) {
                final String[] ss = pairs[i].split(" +");
                trcode[i * 2] = ss[0];
                trcode[i * 2 + 1] = ss[1];
            }
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        LOGGER.info("Loaded " + (tr.length + tr.length + trcode.length) + " translit entries.");
    }

    public static String translit(String s) {
        for (int i = 0; i < tr.length; i += 2) {
            s = s.replace(tr[i], tr[i + 1]);
        }
        return s;
    }

    public static String fromTranslit(String s, final int type) {
        if (type == 1) {
            for (int i = 0; i < trb.length; i += 2) {
                s = s.replace(trb[i], trb[i + 1]);
            }
        } else if (type == 2) {
            for (int i = 0; i < trcode.length; i += 2) {
                s = s.replace(trcode[i], trcode[i + 1]);
            }
        }
        return s;
    }

    public static String replace(final String str, final String regex, final int flags, final String replace) {
        return Pattern.compile(regex, flags).matcher(str).replaceAll(replace);
    }

    public static boolean matches(final String str, final String regex, final int flags) {
        return Pattern.compile(regex, flags).matcher(str).matches();
    }

    public static String joinStrings(final String glueStr, final String[] strings, int startIdx, int maxCount) {
        StringBuilder result = new StringBuilder();
        if (startIdx < 0) {
            startIdx += strings.length;
            if (startIdx < 0) {
                return result.toString();
            }
        }
        while (startIdx < strings.length && maxCount != 0) {
            if ((result.length() > 0) && glueStr != null && !glueStr.isEmpty()) {
                result.append(glueStr);
            }
            result.append(strings[startIdx++]);
            --maxCount;
        }
        return result.toString();
    }

    public static String joinStrings(final String glueStr, final String[] strings, final int startIdx) {
        return joinStrings(glueStr, strings, startIdx, -1);
    }

    public static String joinStrings(final String glueStr, final String[] strings) {
        return joinStrings(glueStr, strings, 0);
    }

    public static String stripToSingleLine(String s) {
        if (s.isEmpty()) {
            return s;
        }
        s = s.replaceAll("\\\\n", "\n");
        final int i = s.indexOf("\n");
        if (i > -1) {
            s = s.substring(0, i);
        }
        return s;
    }
}
