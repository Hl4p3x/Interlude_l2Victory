package ru.j2dev.gameserver.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtils {
    private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    private static final SimpleDateFormat HERO_RECORD_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");

    public static String toSimpleFormat(final Calendar cal) {
        return SIMPLE_FORMAT.format(cal.getTime());
    }

    public static String toSimpleFormat(final long cal) {
        return SIMPLE_FORMAT.format(cal);
    }

    public static String toHeroRecordFormat(final long cal) {
        return HERO_RECORD_FORMAT.format(cal);
    }
}
