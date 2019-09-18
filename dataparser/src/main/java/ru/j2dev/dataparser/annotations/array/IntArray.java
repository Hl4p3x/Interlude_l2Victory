package ru.j2dev.dataparser.annotations.array;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Camelion
 * @date : 22.08.12 3:28
 * <p/>
 * Показывает парсеру, что поле класса должно считываться как массив типа
 * int
 * <p/>
 * Если поле класса имеет тип List<int[]> или List<Integer[]>, то этот
 * список будет заполняться элементами типа int[]
 */
@Target(ElementType.FIELD)
// Target field
@Retention(RetentionPolicy.RUNTIME)
public @interface IntArray {
    /**
     * Имя, по которому будет произведен поиск переменной в буфере, если
     * withoutName() == false Если не указано - подставляется имя
     * аннотированного поля
     *
     * @return - имя переменной в буфере
     */
    String name() default "";

    /**
     * Если true - поиск будет произведен по шаблону \{([\d+-; ]*?)} Если false
     * - поиск будет произведен по шаблону name + "\s*?=\s*?\{([\d+-; ]*?)}"
     *
     * @return true - поиск по шаблону, false - поиск по имени
     */
    boolean withoutName() default false;

    /**
     * @return Строка - разделитель для элементов массива
     */
    String splitter() default ";";

    /**
     * Границы массива
     *
     * @return
     */
    String[] bounds() default {"\\{", "}"};

    /**
     * Может ли поле принимать значение null true - включает дополнительную
     * проверку перед записью значения.
     *
     * @return true - поле класса может принимать значение null
     */
    boolean canBeNull() default true;
}
