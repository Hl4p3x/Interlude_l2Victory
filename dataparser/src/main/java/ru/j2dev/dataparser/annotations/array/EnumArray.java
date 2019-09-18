package ru.j2dev.dataparser.annotations.array;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Camelion
 * @date : 22.08.12 12:47 Показывает парсеру, что поле класса должно считываться
 * как массив типа Enum[]
 * <p/>
 * Если поле класса имеет тип List<Enum[]> то этот список будет
 * заполняться элементами типа Enum[]
 */
@Target(ElementType.FIELD)
// Target field
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumArray {
    /**
     * Имя, по которому будет произведен поиск переменной в буфере, если
     * withoutName() == false Если не указано - подставляется имя
     * аннотированного поля
     *
     * @return - имя переменной в буфере
     */
    String name() default "";

    /**
     * Если true - поиск будет произведен по шаблону \{([\\w0-9_]*?)} Если false
     * - поиск будет произведен по шаблону name + "\s*?=\s*?\{([\\w0-9_]*?)}"
     *
     * @return true - поиск по шаблону, false - поиск по имени
     */
    boolean withoutName() default false;

    /**
     * @return Строка - разделитель для элементов массива
     */
    String splitter() default ";";
}
