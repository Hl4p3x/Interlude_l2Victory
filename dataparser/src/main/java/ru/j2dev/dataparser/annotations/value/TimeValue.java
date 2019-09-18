package ru.j2dev.dataparser.annotations.value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Camelion
 * @date : 25.08.12 1:57 Показывает парсеру, что поле класса должно считываться
 * как элемент типа Long (с преобразованием по времени (к секундам)) Поле
 * типа TimeValue соответствует одному из шаблонов (\d+)sec, (\d+)min,
 * (\d+)hour. Они и используются при поиске значения
 * <p/>
 * Не группируется в список List!
 */
@Target(ElementType.FIELD)
// Target field
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeValue {
    /**
     * Имя, по которому будет произведен поиск переменной в буфере, если
     * withoutName() == false Если не указано - подставляется имя
     * аннотированного поля
     *
     * @return - имя переменной в буфере
     */
    String name() default "";

    /**
     * Если true - поиск будет произведен по одному из шаблонов (\d+)sec,
     * (\d+)min, (\d+)time Если false - поиск будет произведен по одному из
     * шаблонов: name +
     * "\s*=\s*(\d+)sec", name + "\s*=\s*(\d+)min", name + "\s*=\s*(\d+)time"
     *
     * @return true - поиск по шаблону, false - поиск по имени
     */
    boolean withoutName() default false;
}
