package ru.j2dev.dataparser.annotations.value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Camelion
 * @date : 25.08.12 0:09 Показывает парсеру, что поле класса должно считываться
 * как элемент типа Double Если поле класса имеет тип List<Double> то этот
 * список будет заполняться элементами типа Double
 */
@Target(ElementType.FIELD)
// Target field
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleValue {
    /**
     * Имя, по которому будет произведен поиск переменной в буфере, если
     * withoutName() == false Если не указано - подставляется имя
     * аннотированного поля
     *
     * @return - имя переменной в буфере
     */
    String name() default "";

    /**
     * Если true - поиск будет произведен по шаблону ([\\d\\.]+) Если false -
     * поиск будет произведен по шаблону name + "\s*=\s*([\\d\\.]+)"
     *
     * @return true - поиск по шаблону, false - поиск по имени
     */
    boolean withoutName() default false;
}
