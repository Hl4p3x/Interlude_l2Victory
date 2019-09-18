package ru.j2dev.dataparser.annotations.value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Camelion
 * @date : 22.08.12 14:36
 * <p/>
 * Показывает парсеру, что поле класса должно считываться как элемент типа
 * String Все String поля в PTS датапаке заключены в [] скобки
 */
@Target(ElementType.FIELD)
// Target field
@Retention(RetentionPolicy.RUNTIME)
public @interface StringValue {
    /**
     * Имя, по которому будет произведен поиск переменной в буфере, если
     * withoutName() == false Если не указано - подставляется имя
     * аннотированного поля
     *
     * @return - имя переменной в буфере
     */
    String name() default "";

    /**
     * Если true - поиск будет произведен по шаблону \[(\S*?)] Если false -
     * поиск будет произведен по шаблону name + "\s*=\s*\[(\S*?)]"
     *
     * @return true - поиск по шаблону, false - поиск по имени
     */
    boolean withoutName() default false;

    /**
     * <p/>
     * Если true - в поиск строкового значения не будут включены символы [] Если
     * false - поиск будет произведен по стандартному шаблону
     *
     * @return true - поиск без [], false - стандартный поиск
     */
    boolean withoutBounds() default false;
}
