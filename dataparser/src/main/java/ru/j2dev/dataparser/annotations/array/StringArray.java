package ru.j2dev.dataparser.annotations.array;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Camelion
 * @date : 23.08.12 0:17 Показывает парсеру, что поле класса должно считываться
 * как массив String[]
 */
@Target(ElementType.FIELD)
// Target field
@Retention(RetentionPolicy.RUNTIME)
public @interface StringArray {
    /**
     * Имя, по которому будет произведен поиск переменной в буфере, если
     * withoutName() == false Если не указано - подставляется имя
     * аннотированного поля
     *
     * @return - имя переменной в буфере
     */
    String name() default "";

    /**
     * Если true - поиск будет произведен по шаблону \{([\S; ]*?)} Если false -
     * поиск будет произведен по шаблону name + "\s*?=\s*?\{([\S; ]*?)}"
     *
     * @return true - поиск по шаблону, false - поиск по имени
     */
    boolean withoutName() default false;

    /**
     * Границы массива, работает только при withoutName = true!
     *
     * @return
     */
    String[] bounds() default {"\\{", "}"};

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
