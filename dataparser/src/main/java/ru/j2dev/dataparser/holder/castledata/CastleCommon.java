package ru.j2dev.dataparser.holder.castledata;

import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.DateValue;
import ru.j2dev.dataparser.annotations.value.EnumValue;
import ru.j2dev.dataparser.annotations.value.IntValue;

import java.util.Calendar;
import java.util.Date;

/**
 * @author : Camelion
 * @date : 26.08.12 0:56 Содержит в себе основные настройки для замков
 */
public class CastleCommon {
    @DateValue(format = "yyyy / MM / dd / HH / mm")
    public Date base_siege_time;
    @ObjectArray
    public ReservableSiegeTime[] reservable_siege_time; // Время, на которое
    // владелец замка может
    // назначить осаду
    @IntValue
    public int max_concurrent_siege; // Возможно, максимальное кол-во
    // одновременно запущенных осад

    public enum DayOfWeek {
        SUN(Calendar.SUNDAY),
        MON(Calendar.MONDAY),
        TUE(Calendar.TUESDAY),
        WED(Calendar.WEDNESDAY),
        THU(Calendar.THURSDAY),
        FRI(Calendar.FRIDAY),
        SAT(Calendar.SATURDAY);
        public final int day;

        DayOfWeek(int day) {
            this.day = day;
        }
    }

    public static class ReservableSiegeTime {
        @EnumValue(withoutName = true)
        public DayOfWeek day_of_week; // День недели
        @IntValue(withoutName = true)
        public int hour; // Час
    }
}
