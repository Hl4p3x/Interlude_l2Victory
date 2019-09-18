package ru.j2dev.dataparser.annotations.array;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Camelion
 * @date : 25.08.12 12:57 Показывает парсеру, что поле класса должно считываться
 * как массив типа int При обработке массива подставляются значения из
 */
@Target(ElementType.FIELD)
// Target field
@Retention(RetentionPolicy.RUNTIME)
public @interface LinkedArray {
    /**
     * Имя, по которому будет произведен поиск переменной в буфере, если
     * withoutName() == false Если не указано - подставляется имя
     * аннотированного поля
     *
     * @return - имя переменной в буфере
     */
    String name() default "";

    /**
     * Если true - поиск будет произведен по шаблону \{([@\d\w_;]+)} Если false
     * - поиск будет произведен по шаблону name + "\s*?=\s*?\{([@\d\w_;]+)}"
     *
     * @return true - поиск по шаблону, false - поиск по имени
     */
    boolean withoutName() default false;

    /**
     * @return Строка - разделитель для элементов массива
     */
    String splitter() default ";";

    /**
     * Может ли поле принимать значение null true - включает дополнительную
     * проверку перед записью значения.
     *
     * @return true - поле класса может принимать значение null
     */
    boolean canBeNull() default true;
}
