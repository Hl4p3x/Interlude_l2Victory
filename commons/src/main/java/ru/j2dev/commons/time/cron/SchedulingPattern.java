package ru.j2dev.commons.time.cron;

import java.util.*;

public class SchedulingPattern implements NextTime {
    private static final int MINUTE_MIN_VALUE = 0;
    private static final int MINUTE_MAX_VALUE = 59;
    private static final int HOUR_MIN_VALUE = 0;
    private static final int HOUR_MAX_VALUE = 23;
    private static final int DAY_OF_MONTH_MIN_VALUE = 1;
    private static final int DAY_OF_MONTH_MAX_VALUE = 31;
    private static final int MONTH_MIN_VALUE = 1;
    private static final int MONTH_MAX_VALUE = 12;
    private static final int DAY_OF_WEEK_MIN_VALUE = 0;
    private static final int DAY_OF_WEEK_MAX_VALUE = 7;
    private static final ValueParser MINUTE_VALUE_PARSER = new MinuteValueParser();
    private static final ValueParser HOUR_VALUE_PARSER = new HourValueParser();
    private static final ValueParser DAY_OF_MONTH_VALUE_PARSER = new DayOfMonthValueParser();
    private static final ValueParser MONTH_VALUE_PARSER = new MonthValueParser();
    private static final ValueParser DAY_OF_WEEK_VALUE_PARSER = new DayOfWeekValueParser();

    protected List<ValueMatcher> minuteMatchers;
    protected List<ValueMatcher> hourMatchers;
    protected List<ValueMatcher> dayOfMonthMatchers;
    protected List<ValueMatcher> monthMatchers;
    protected List<ValueMatcher> dayOfWeekMatchers;
    protected int matcherSize;
    protected Map<Integer, Integer> hourAdder;
    protected Map<Integer, Integer> hourAdderRnd;
    protected Map<Integer, Integer> dayOfYearAdder;
    protected Map<Integer, Integer> minuteAdderRnd;
    protected Map<Integer, Integer> weekOfYearAdder;
    private String asString;

    public SchedulingPattern(final String pattern) throws InvalidPatternException {
        minuteMatchers = new ArrayList<>();
        hourMatchers = new ArrayList<>();
        dayOfMonthMatchers = new ArrayList<>();
        monthMatchers = new ArrayList<>();
        dayOfWeekMatchers = new ArrayList<>();
        matcherSize = 0;
        hourAdder = new TreeMap<>();
        hourAdderRnd = new TreeMap<>();
        dayOfYearAdder = new TreeMap<>();
        minuteAdderRnd = new TreeMap<>();
        weekOfYearAdder = new TreeMap<>();
        asString = pattern;
        final StringTokenizer st1 = new StringTokenizer(pattern, "|");
        if (st1.countTokens() < 1) {
            throw new InvalidPatternException("invalid pattern: \"" + pattern + "\"");
        }
        while (st1.hasMoreTokens()) {
            final String localPattern = st1.nextToken();
            final StringTokenizer st2 = new StringTokenizer(localPattern, " \t");
            final int tokCnt = st2.countTokens();
            if (tokCnt < 5 || tokCnt > 6) {
                throw new InvalidPatternException("invalid pattern: \"" + localPattern + "\"");
            }
            try {
                String minutePattern = st2.nextToken();
                final String[] minutePatternParts = minutePattern.split(":");
                if (minutePatternParts.length > 1) {
                    for (int i = 0; i < minutePatternParts.length - 1; ++i) {
                        if (minutePatternParts[i].length() > 1) {
                            if (!minutePatternParts[i].startsWith("~")) {
                                throw new InvalidPatternException("Unknown hour modifier \"" + minutePatternParts[i] + "\"");
                            }
                            minuteAdderRnd.put(matcherSize, Integer.parseInt(minutePatternParts[i].substring(1)));
                        }
                    }
                    minutePattern = minutePatternParts[minutePatternParts.length - 1];
                }
                minuteMatchers.add(buildValueMatcher(minutePattern, MINUTE_VALUE_PARSER));
            } catch (Exception e) {
                throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing minutes field: " + e.getMessage() + ".");
            }
            try {
                String hourPattern = st2.nextToken();
                final String[] hourPatternParts = hourPattern.split(":");
                if (hourPatternParts.length > 1) {
                    for (int i = 0; i < hourPatternParts.length - 1; ++i) {
                        if (hourPatternParts[i].length() > 1) {
                            if (hourPatternParts[i].startsWith("+")) {
                                hourAdder.put(matcherSize, Integer.parseInt(hourPatternParts[i].substring(1)));
                            } else {
                                if (!hourPatternParts[i].startsWith("~")) {
                                    throw new InvalidPatternException("Unknown hour modifier \"" + hourPatternParts[i] + "\"");
                                }
                                hourAdderRnd.put(matcherSize, Integer.parseInt(hourPatternParts[i].substring(1)));
                            }
                        }
                    }
                    hourPattern = hourPatternParts[hourPatternParts.length - 1];
                }
                hourMatchers.add(buildValueMatcher(hourPattern, HOUR_VALUE_PARSER));
            } catch (Exception e) {
                throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing hours field: " + e.getMessage() + ".");
            }
            try {
                String dayOfMonthPattern = st2.nextToken();
                final String[] dayOfMonthPatternParts = dayOfMonthPattern.split(":");
                if (dayOfMonthPatternParts.length > 1) {
                    for (int i = 0; i < dayOfMonthPatternParts.length - 1; ++i) {
                        if (dayOfMonthPatternParts[i].length() > 1) {
                            if (!dayOfMonthPatternParts[i].startsWith("+")) {
                                throw new InvalidPatternException("Unknown day modifier \"" + dayOfMonthPatternParts[i] + "\"");
                            }
                            dayOfYearAdder.put(matcherSize, Integer.parseInt(dayOfMonthPatternParts[i].substring(1)));
                        }
                    }
                    dayOfMonthPattern = dayOfMonthPatternParts[dayOfMonthPatternParts.length - 1];
                }
                dayOfMonthMatchers.add(buildValueMatcher(dayOfMonthPattern, DAY_OF_MONTH_VALUE_PARSER));
            } catch (Exception e) {
                throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing days of month field: " + e.getMessage() + ".");
            }
            try {
                monthMatchers.add(buildValueMatcher(st2.nextToken(), MONTH_VALUE_PARSER));
            } catch (Exception e) {
                throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing months field: " + e.getMessage() + ".");
            }
            try {
                dayOfWeekMatchers.add(buildValueMatcher(st2.nextToken(), DAY_OF_WEEK_VALUE_PARSER));
            } catch (Exception e) {
                throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing days of week field: " + e.getMessage() + ".");
            }
            if (st2.hasMoreTokens()) {
                try {
                    String weekOfYearAdderText = st2.nextToken();
                    if (weekOfYearAdderText.charAt(0) != '+') {
                        throw new InvalidPatternException("Unknown week of year addition in pattern \"" + localPattern + "\".");
                    }
                    weekOfYearAdderText = weekOfYearAdderText.substring(1);
                    weekOfYearAdder.put(matcherSize, Integer.parseInt(weekOfYearAdderText));
                } catch (Exception e) {
                    throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing week of year addition: " + e + ".");
                }
            }
            ++matcherSize;
        }
    }

    public static boolean validate(final String schedulingPattern) {
        try {
            new SchedulingPattern(schedulingPattern);
        } catch (InvalidPatternException e) {
            return false;
        }
        return true;
    }

    private static int parseAlias(final String value, final String[] aliases, final int offset) throws Exception {
        for (int i = 0; i < aliases.length; ++i) {
            if (aliases[i].equalsIgnoreCase(value)) {
                return offset + i;
            }
        }
        throw new Exception("invalid alias \"" + value + "\"");
    }

    private ValueMatcher buildValueMatcher(final String str, final ValueParser parser) throws Exception {
        if (str.length() == 1 && "*".equals(str)) {
            return new AlwaysTrueValueMatcher();
        }
        final List<Integer> values = new ArrayList<>();
        final StringTokenizer st = new StringTokenizer(str, ",");
        while (st.hasMoreTokens()) {
            final String element = st.nextToken();
            List<Integer> local;
            try {
                local = parseListElement(element, parser);
            } catch (Exception e) {
                throw new Exception("invalid field \"" + str + "\", invalid element \"" + element + "\", " + e.getMessage());
            }
            for (final Integer value : local) {
                if (!values.contains(value)) {
                    values.add(value);
                }
            }
        }
        if (values.size() == 0) {
            throw new Exception("invalid field \"" + str + "\"");
        }
        if (parser == DAY_OF_MONTH_VALUE_PARSER) {
            return new DayOfMonthValueMatcher(values);
        }
        return new IntArrayValueMatcher(values);
    }

    private List<Integer> parseListElement(final String str, final ValueParser parser) throws Exception {
        final StringTokenizer st = new StringTokenizer(str, "/");
        final int size = st.countTokens();
        if (size < 1 || size > 2) {
            throw new Exception("syntax error");
        }
        List<Integer> values;
        try {
            values = parseRange(st.nextToken(), parser);
        } catch (Exception e) {
            throw new Exception("invalid range, " + e.getMessage());
        }
        if (size != 2) {
            return values;
        }
        final String dStr = st.nextToken();
        int div;
        try {
            div = Integer.parseInt(dStr);
        } catch (NumberFormatException e2) {
            throw new Exception("invalid divisor \"" + dStr + "\"");
        }
        if (div < 1) {
            throw new Exception("non positive divisor \"" + div + "\"");
        }
        final List<Integer> values2 = new ArrayList<>();
        for (int i = 0; i < values.size(); i += div) {
            values2.add(values.get(i));
        }
        return values2;
    }

    private List<Integer> parseRange(final String str, final ValueParser parser) throws Exception {
        if ("*".equals(str)) {
            final int min = parser.getMinValue();
            final int max = parser.getMaxValue();
            final List<Integer> values = new ArrayList<>();
            for (int i = min; i <= max; ++i) {
                values.add(i);
            }
            return values;
        }
        final StringTokenizer st = new StringTokenizer(str, "-");
        final int size = st.countTokens();
        if (size < 1 || size > 2) {
            throw new Exception("syntax error");
        }
        final String v1Str = st.nextToken();
        int v1;
        try {
            v1 = parser.parse(v1Str);
        } catch (Exception e) {
            throw new Exception("invalid value \"" + v1Str + "\", " + e.getMessage());
        }
        if (size == 1) {
            final List<Integer> values2 = new ArrayList<>();
            values2.add(v1);
            return values2;
        }
        final String v2Str = st.nextToken();
        int v2;
        try {
            v2 = parser.parse(v2Str);
        } catch (Exception e2) {
            throw new Exception("invalid value \"" + v2Str + "\", " + e2.getMessage());
        }
        final List<Integer> values3 = new ArrayList<>();
        if (v1 < v2) {
            for (int j = v1; j <= v2; ++j) {
                values3.add(j);
            }
        } else if (v1 > v2) {
            final int min2 = parser.getMinValue();
            for (int max2 = parser.getMaxValue(), k = v1; k <= max2; ++k) {
                values3.add(k);
            }
            for (int k = min2; k <= v2; ++k) {
                values3.add(k);
            }
        } else {
            values3.add(v1);
        }
        return values3;
    }

    /**
     * This methods returns true if the given timestamp (expressed as a UNIX-era
     * millis value) matches the pattern, according to the given time zone.
     * <p>
     *
     * @param timezone A time zone.
     * @param millis   The timestamp, as a UNIX-era millis value.
     *                 <p>
     * @return true if the given timestamp matches the pattern.
     */
    public boolean match(final TimeZone timezone, final long millis) {
        final GregorianCalendar gc = new GregorianCalendar(timezone);
        gc.setTimeInMillis(millis);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);

        final int minute = gc.get(Calendar.MINUTE);
        final int hour = gc.get(Calendar.HOUR_OF_DAY);
        final int dayOfMonth = gc.get(Calendar.DAY_OF_MONTH);
        final int month = gc.get(Calendar.MONTH) + 1;
        final int dayOfWeek = gc.get(Calendar.DAY_OF_WEEK) - 1;
        final int year = gc.get(Calendar.YEAR);

        for (int i = 0; i < matcherSize; i++) {
            final ValueMatcher minuteMatcher = minuteMatchers.get(i);
            final ValueMatcher hourMatcher = hourMatchers.get(i);
            final ValueMatcher dayOfMonthMatcher = dayOfMonthMatchers.get(i);
            final ValueMatcher monthMatcher = monthMatchers.get(i);
            final ValueMatcher dayOfWeekMatcher = dayOfWeekMatchers.get(i);
            final boolean eval = minuteMatcher.match(minute) && hourMatcher.match(hour) && ((dayOfMonthMatcher instanceof DayOfMonthValueMatcher) ? ((DayOfMonthValueMatcher) dayOfMonthMatcher).match(dayOfMonth, month, gc.isLeapYear(year)) : dayOfMonthMatcher.match(dayOfMonth)) && monthMatcher.match(month) && dayOfWeekMatcher.match(dayOfWeek);
            if (eval) {
                return true;
            }
        }
        return false;
    }

    /**
     * This methods returns true if the given timestamp (expressed as a UNIX-era
     * millis value) matches the pattern, according to the system default time
     * zone.
     * <p>
     *
     * @param millis The timestamp, as a UNIX-era millis value.
     *               <p>
     * @return true if the given timestamp matches the pattern.
     */
    public boolean match(final long millis) {
        return match(TimeZone.getDefault(), millis);
    }

    /**
     * This methods returns next matching timestamp (expressed as a UNIX-era
     * millis value) according the pattern and given timestamp.
     * <p>
     *
     * @param timezone A time zone.
     * @param millis   The timestamp, as a UNIX-era millis value.
     *                 <p>
     * @return next matching timestamp after given timestamp, according pattern
     */
    public long next(final TimeZone timezone, final long millis) {
        long next = -1L;

        final GregorianCalendar gc = new GregorianCalendar(timezone);

        for (int i = 0; i < matcherSize; i++) {
            gc.setTimeInMillis(millis);
            gc.add(Calendar.MINUTE, 1);
            gc.set(Calendar.SECOND, 0);
            gc.set(Calendar.MILLISECOND, 0);

            final ValueMatcher minuteMatcher = minuteMatchers.get(i);
            final ValueMatcher hourMatcher = hourMatchers.get(i);
            final ValueMatcher dayOfMonthMatcher = dayOfMonthMatchers.get(i);
            final ValueMatcher monthMatcher = monthMatchers.get(i);
            final ValueMatcher dayOfWeekMatcher = dayOfWeekMatchers.get(i);

            loop:
            while (true) {
                final int year = gc.get(Calendar.YEAR);
                final boolean isLeapYear = gc.isLeapYear(year);

                for (int month = gc.get(Calendar.MONTH) + 1; month <= MONTH_MAX_VALUE; month++) {
                    if (monthMatcher.match(month)) {
                        gc.set(Calendar.MONTH, month - 1);
                        final int maxDayOfMonth = DayOfMonthValueMatcher.getLastDayOfMonth(month, isLeapYear);
                        for (int dayOfMonth = gc.get(Calendar.DAY_OF_MONTH); dayOfMonth <= maxDayOfMonth; dayOfMonth++) {
                            if ((dayOfMonthMatcher instanceof DayOfMonthValueMatcher) ? ((DayOfMonthValueMatcher) dayOfMonthMatcher).match(dayOfMonth, month, isLeapYear) : dayOfMonthMatcher.match(dayOfMonth)) {
                                gc.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                final int dayOfWeek = gc.get(Calendar.DAY_OF_WEEK) - 1;
                                if (dayOfWeekMatcher.match(dayOfWeek)) {
                                    for (int hour = gc.get(Calendar.HOUR_OF_DAY); hour <= HOUR_MAX_VALUE; hour++) {
                                        if (hourMatcher.match(hour)) {
                                            gc.set(Calendar.HOUR_OF_DAY, hour);
                                            for (int minute = gc.get(Calendar.MINUTE); minute <= MINUTE_MAX_VALUE; minute++) {
                                                if (minuteMatcher.match(minute)) {
                                                    gc.set(Calendar.MINUTE, minute);
                                                    gc.set(Calendar.SECOND, 0);
                                                    final long next0 = gc.getTimeInMillis();
                                                    if (next == -1L || next0 < next) {
                                                        next = next0;
                                                    }
                                                    break loop;
                                                }
                                            }
                                        }
                                        gc.set(Calendar.MINUTE, MINUTE_MIN_VALUE);
                                    }
                                }
                            }
                            gc.set(Calendar.HOUR_OF_DAY, HOUR_MIN_VALUE);
                            gc.set(Calendar.MINUTE, MINUTE_MIN_VALUE);
                        }
                    }
                    gc.set(Calendar.DAY_OF_MONTH, DAY_OF_MONTH_MIN_VALUE);
                    gc.set(Calendar.HOUR_OF_DAY, HOUR_MIN_VALUE);
                    gc.set(Calendar.MINUTE, MINUTE_MIN_VALUE);
                }
                gc.set(Calendar.MONTH, MONTH_MIN_VALUE - 1);
                gc.set(Calendar.HOUR_OF_DAY, HOUR_MIN_VALUE);
                gc.set(Calendar.MINUTE, MINUTE_MIN_VALUE);
                gc.roll(Calendar.YEAR, true);
            }
        }

        return next;
    }

    @Override
    public long next(final long millis) {
        return next(TimeZone.getDefault(), millis);
    }

    @Override
    public String toString() {
        return asString;
    }

    private interface ValueMatcher {
        boolean match(final int p0);
    }

    private interface ValueParser {
        int parse(final String p0) throws Exception;

        int getMinValue();

        int getMaxValue();
    }

    private static class SimpleValueParser implements ValueParser {
        protected int minValue;
        protected int maxValue;

        public SimpleValueParser(final int minValue, final int maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public int parse(final String value) throws Exception {
            int i;
            try {
                i = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new Exception("invalid integer value");
            }
            if (i < minValue || i > maxValue) {
                throw new Exception("value out of range");
            }
            return i;
        }

        @Override
        public int getMinValue() {
            return minValue;
        }

        @Override
        public int getMaxValue() {
            return maxValue;
        }
    }

    private static class MinuteValueParser extends SimpleValueParser {
        public MinuteValueParser() {
            super(0, 59);
        }
    }

    private static class HourValueParser extends SimpleValueParser {
        public HourValueParser() {
            super(0, 23);
        }
    }

    private static class DayOfMonthValueParser extends SimpleValueParser {
        public DayOfMonthValueParser() {
            super(1, 31);
        }

        @Override
        public int parse(final String value) throws Exception {
            if ("L".equalsIgnoreCase(value)) {
                return 32;
            }
            return super.parse(value);
        }
    }

    private static class MonthValueParser extends SimpleValueParser {
        private static String[] ALIASES = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

        public MonthValueParser() {
            super(1, 12);
        }

        @Override
        public int parse(final String value) throws Exception {
            try {
                return super.parse(value);
            } catch (Exception e) {
                return parseAlias(value, ALIASES, 1);
            }
        }
    }

    private static class DayOfWeekValueParser extends SimpleValueParser {
        private static String[] ALIASES = {"sun", "mon", "tue", "wed", "thu", "fri", "sat"};

        public DayOfWeekValueParser() {
            super(0, 7);
        }

        @Override
        public int parse(final String value) throws Exception {
            try {
                return super.parse(value) % 7;
            } catch (Exception e) {
                return parseAlias(value, ALIASES, 0);
            }
        }
    }

    private static class AlwaysTrueValueMatcher implements ValueMatcher {
        @Override
        public boolean match(final int value) {
            return true;
        }
    }

    private static class IntArrayValueMatcher implements ValueMatcher {
        private int[] values;

        public IntArrayValueMatcher(final List<Integer> integers) {
            final int size = integers.size();
            values = new int[size];
            for (int i = 0; i < size; ++i) {
                try {
                    values[i] = integers.get(i);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
        }

        @Override
        public boolean match(final int value) {
            for (int value1 : values) {
                if (value1 == value) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class DayOfMonthValueMatcher extends IntArrayValueMatcher {
        private static final int[] lastDays = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        public DayOfMonthValueMatcher(final List<Integer> integers) {
            super(integers);
        }

        public static int getLastDayOfMonth(final int month, final boolean isLeapYear) {
            if (isLeapYear && month == 2) {
                return 29;
            }
            return lastDays[month - 1];
        }

        public static boolean isLastDayOfMonth(final int value, final int month, final boolean isLeapYear) {
            return value == getLastDayOfMonth(month, isLeapYear);
        }

        public boolean match(final int value, final int month, final boolean isLeapYear) {
            return super.match(value) || (value > 27 && match(32) && isLastDayOfMonth(value, month, isLeapYear));
        }
    }

    public class InvalidPatternException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        InvalidPatternException() {
        }

        InvalidPatternException(final String message) {
            super(message);
        }
    }
}
