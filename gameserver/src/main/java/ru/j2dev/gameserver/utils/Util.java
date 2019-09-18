package ru.j2dev.gameserver.utils;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Util {

    public static final SimpleDateFormat datetimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    static final String PATTERN = "0.0000000000E00";
    static final DecimalFormat df;
    private static final NumberFormat adenaFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE);
    private static final Pattern _pattern = Pattern.compile("<!--TEMPLET(\\d+)(.*?)TEMPLET-->", Pattern.DOTALL);

    static {
        (df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH)).applyPattern("0.0000000000E00");
        df.setPositivePrefix("+");
    }

    public static boolean isMatchingRegexp(final String text, final String template) {
        Pattern pattern = null;
        try {
            pattern = Pattern.compile(template);
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }
        if (pattern == null) {
            return false;
        }
        final Matcher regexp = pattern.matcher(text);
        return regexp.matches();
    }

    public static String formatDouble(final double x, final String nanString, final boolean forceExponents) {
        if (Double.isNaN(x)) {
            return nanString;
        }
        if (forceExponents) {
            return df.format(x);
        }
        if ((long) x == x) {
            return String.valueOf((long) x);
        }
        return String.valueOf(x);
    }

    public static String formatAdena(final long amount) {
        return adenaFormatter.format(amount);
    }

    public static String formatTime(int time) {
        if (time == 0) {
            return "now";
        }
        time = Math.abs(time);
        String ret = "";
        final long numDays = time / 86400;
        time -= (int) (numDays * 86400L);
        final long numHours = time / 3600;
        time -= (int) (numHours * 3600L);
        final long numMins = time / 60;
        time -= (int) (numMins * 60L);
        final long numSeconds = time;
        if (numDays > 0L) {
            ret = ret + numDays + "d ";
        }
        if (numHours > 0L) {
            ret = ret + numHours + "h ";
        }
        if (numMins > 0L) {
            ret = ret + numMins + "m ";
        }
        if (numSeconds > 0L) {
            ret = ret + numSeconds + "s";
        }
        return ret.trim();
    }

    public static String getCfgDirect() {
        final StringBuilder result = new StringBuilder();
        result.append("Auth: ").append(Config.GAME_SERVER_LOGIN_HOST).append('\n');
        result.append("Game:\n");
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface iface = interfaces.nextElement();
                if (!iface.isLoopback()) {
                    if (!iface.isUp()) {
                        continue;
                    }
                    final Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        final InetAddress addr = addresses.nextElement();
                        final String tmp = addr.getHostAddress();
                        result.append(" ").append(tmp).append('\n');
                    }
                }
            }
        } catch (SocketException e) {
            return "none";
        }
        return result.toString();
    }

    public static long rollDrop(final long min, final long max, double calcChance, final boolean rate) {
        if (calcChance <= 0.0 || min <= 0L || max <= 0L) {
            return 0L;
        }
        int dropmult = 1;
        if (rate) {
            calcChance *= Config.RATE_DROP_ITEMS;
        }
        if (calcChance > 1000000.0) {
            if (calcChance % 1000000.0 == 0.0) {
                dropmult = (int) (calcChance / 1000000.0);
            } else {
                dropmult = (int) Math.ceil(calcChance / 1000000.0);
                calcChance /= dropmult;
            }
        }
        return Rnd.chance(calcChance / 10000.0) ? Rnd.get(min * dropmult, max * dropmult) : 0L;
    }

    public static int packInt(final int[] a, final int bits) throws Exception {
        final int m = 32 / bits;
        if (a.length > m) {
            throw new Exception("Overflow");
        }
        int result = 0;
        final int mval = (int) Math.pow(2.0, bits);
        for (int i = 0; i < m; ++i) {
            result <<= bits;
            int next;
            if (a.length > i) {
                next = a[i];
                if (next >= mval || next < 0) {
                    throw new Exception("Overload, value is out of range");
                }
            } else {
                next = 0;
            }
            result += next;
        }
        return result;
    }

    public static long packLong(final int[] a, final int bits) throws Exception {
        final int m = 64 / bits;
        if (a.length > m) {
            throw new Exception("Overflow");
        }
        long result = 0L;
        final int mval = (int) Math.pow(2.0, bits);
        for (int i = 0; i < m; ++i) {
            result <<= bits;
            int next;
            if (a.length > i) {
                next = a[i];
                if (next >= mval || next < 0) {
                    throw new Exception("Overload, value is out of range");
                }
            } else {
                next = 0;
            }
            result += next;
        }
        return result;
    }

    public static int[] unpackInt(int a, final int bits) {
        final int m = 32 / bits;
        final int mval = (int) Math.pow(2.0, bits);
        final int[] result = new int[m];
        for (int i = m; i > 0; --i) {
            final int next = a;
            a >>= bits;
            result[i - 1] = next - a * mval;
        }
        return result;
    }

    public static int[] unpackLong(long a, final int bits) {
        final int m = 64 / bits;
        final int mval = (int) Math.pow(2.0, bits);
        final int[] result = new int[m];
        for (int i = m; i > 0; --i) {
            final long next = a;
            a >>= bits;
            result[i - 1] = (int) (next - a * mval);
        }
        return result;
    }

    public static String joinStrings(final String glueStr, final String[] strings, final int startIdx, final int maxCount) {
        return Strings.joinStrings(glueStr, strings, startIdx, maxCount);
    }

    public static String joinStrings(final String glueStr, final String[] strings, final int startIdx) {
        return Strings.joinStrings(glueStr, strings, startIdx, -1);
    }

    public static boolean isNumber(final String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static String dumpObject(final Object o, final boolean simpleTypes, final boolean parentFields, final boolean ignoreStatics) {
        Class<?> cls = o.getClass();
        StringBuilder result = new StringBuilder("[" + (simpleTypes ? cls.getSimpleName() : cls.getName()) + "\n");
        final List<Field> fields = new ArrayList<>();
        while (cls != null) {
            for (final Field fld : cls.getDeclaredFields()) {
                if (!fields.contains(fld)) {
                    if (!ignoreStatics || !Modifier.isStatic(fld.getModifiers())) {
                        fields.add(fld);
                    }
                }
            }
            cls = cls.getSuperclass();
            if (!parentFields) {
                break;
            }
        }
        for (final Field fld2 : fields) {
            fld2.setAccessible(true);
            String val;
            try {
                final Object fldObj = fld2.get(o);
                if (fldObj == null) {
                    val = "NULL";
                } else {
                    val = fldObj.toString();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                val = "<ERROR>";
            }
            final String type = simpleTypes ? fld2.getType().getSimpleName() : fld2.getType().toString();
            result.append(String.format("\t%s [%s] = %s;\n", fld2.getName(), type, val));
        }
        result.append("]\n");
        return result.toString();
    }

    public static HashMap<Integer, String> parseTemplate(String html) {
        final Matcher m = _pattern.matcher(html);
        final HashMap<Integer, String> tpls = new HashMap<>();
        while (m.find()) {
            tpls.put(Integer.parseInt(m.group(1)), m.group(2));
            html = html.replace(m.group(0), "");
        }
        tpls.put(0, html);
        return tpls;
    }

    public static int fibonacci(final int n) {
        int x = 0;
        int y = 1;
        for (int i = 0; i < n; ++i) {
            final int z = x;
            x = y;
            y += z;
        }
        return x;
    }

    public static double padovan(final int n) {
        if (n == 0 || n == 1 || n == 2) {
            return 1.0;
        }
        return padovan(n - 2) + padovan(n - 3);
    }

    public static Player[] GetPlayersFromStoredIds(final int[] sids) {
        return Arrays.stream(sids).mapToObj(GameObjectsStorage::getPlayer).filter(Objects::nonNull).toArray(Player[]::new);
    }
}
