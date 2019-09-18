package ru.j2dev.commons.time.cron;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class AddPattern implements NextTime {
    private int monthInc;
    private int monthSet;
    private int dayOfMonthInc;
    private int dayOfMonthSet;
    private int hourOfDayInc;
    private int hourOfDaySet;
    private int minuteInc;
    private int minuteSet;

    public AddPattern(final String pattern) {
        monthInc = -1;
        monthSet = -1;
        dayOfMonthInc = -1;
        dayOfMonthSet = -1;
        hourOfDayInc = -1;
        hourOfDaySet = -1;
        minuteInc = -1;
        minuteSet = -1;
        final String[] parts = pattern.split("\\s+");
        if (parts.length == 2) {
            final String datepartsstr = parts[0];
            final String[] dateparts = datepartsstr.split(":");
            if (dateparts.length == 2) {
                if (dateparts[0].startsWith("+")) {
                    monthInc = Integer.parseInt(dateparts[0].substring(1));
                } else {
                    monthSet = Integer.parseInt(dateparts[0]) - 1;
                }
            }
            final String datemodstr = dateparts[dateparts.length - 1];
            if (datemodstr.startsWith("+")) {
                dayOfMonthInc = Integer.parseInt(datemodstr.substring(1));
            } else {
                dayOfMonthSet = Integer.parseInt(datemodstr);
            }
        }
        final String[] timeparts = parts[parts.length - 1].split(":");
        if (timeparts[0].startsWith("+")) {
            hourOfDayInc = Integer.parseInt(timeparts[0].substring(1));
        } else {
            hourOfDaySet = Integer.parseInt(timeparts[0]);
        }
        if (timeparts[1].startsWith("+")) {
            minuteInc = Integer.parseInt(timeparts[1].substring(1));
        } else {
            minuteSet = Integer.parseInt(timeparts[1]);
        }
    }

    @Override
    public long next(final long millis) {
        final GregorianCalendar gc = new GregorianCalendar(TimeZone.getDefault());
        gc.setTimeInMillis(millis);
        if (monthInc >= 0) {
            gc.add(Calendar.MONTH, monthInc);
        }
        if (monthSet >= 0) {
            gc.set(Calendar.MONTH, monthSet);
        }
        if (dayOfMonthInc >= 0) {
            gc.add(Calendar.DATE, dayOfMonthInc);
        }
        if (dayOfMonthSet >= 0) {
            gc.set(Calendar.DATE, dayOfMonthSet);
        }
        if (hourOfDayInc >= 0) {
            gc.add(Calendar.HOUR_OF_DAY, hourOfDayInc);
        }
        if (hourOfDaySet >= 0) {
            gc.set(Calendar.HOUR_OF_DAY, hourOfDaySet);
        }
        if (minuteInc >= 0) {
            gc.add(Calendar.MINUTE, minuteInc);
        }
        if (minuteSet >= 0) {
            gc.set(Calendar.MINUTE, minuteSet);
        }
        return gc.getTimeInMillis();
    }
}
