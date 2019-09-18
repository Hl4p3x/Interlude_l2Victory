package ru.j2dev.dataparser.annotations.value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Camelion
 * @date : 25.08.12 23:47 Показывает парсеру, что поле класса должно считываться
 * как элемент типа Date (с преобразованием по времени) Не группируется в
 * список List!
 */
@Target(ElementType.FIELD)
// Target field
@Retention(RetentionPolicy.RUNTIME)
public @interface DateValue {
    /**
     * Имя, по которому будет произведен поиск переменной в буфере, если
     * withoutName() == false Если не указано - подставляется имя
     * аннотированного поля
     *
     * @return - имя переменной в буфере
     */
    String name() default "";

    /**
     * Формат, по которому считывается дата
     *
     * @return - формат
     */
    String format() default "yyyy/MM/dd/HH/mm";
}
