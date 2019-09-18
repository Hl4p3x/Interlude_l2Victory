package ru.j2dev.dataparser.annotations.value;

import ru.j2dev.dataparser.annotations.factory.DefaultFactory;
import ru.j2dev.dataparser.annotations.factory.IObjectFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Camelion
 * @date : 22.08.12 18:47 Показывает парсеру, что поле класса должно считываться
 * как элемент типа Object
 * <p/>
 * Отличие от типо IntValue, StringValue, состоит в том, что данная
 * аннотация не производит прямых преобразований к типу объекту. Вместо
 * этого, она передает управление в класс объекта, в какой то степени
 * поступая также, как и аннотация @Element
 * <p/>
 * Текстовый буффер для аннотации выделяется также, как и для @ObjectArray
 * аннотаций - в него включается всё, что находится между {...} скобок
 */
@Target(ElementType.FIELD)
// Target field
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectValue {
    /**
     * Имя, по которому будет произведен поиск переменной в буфере, если
     * withoutName() == false Если не указано - подставляется имя
     * аннотированного поля
     *
     * @return - имя переменной в буфере
     */
    String name() default "";

    /**
     * Если true - поиск будет произведен начиная с первых {...} скобок. Если
     * false - по имени будет найдено первое вхождение строки, дальнейший поиск
     * {...} скобок будет продолжаться с последнего найденного индекса имени
     *
     * @return true - поиск по шаблону, false - поиск по имени
     */
    boolean withoutName() default false;

    /**
     * Может ли поле принимать значение null true - включает дополнительную
     * проверку перед записью значения.
     *
     * @return true - поле класса может принимать значение null
     */
    boolean canBeNull() default true;

    /**
     * Возвращает класс-фабрику, которая управляет созданием
     * ObjectValue-объектов. Позволяет указать в качестве типа поля -
     * какой-нибудь супер-класс, а управление объектами повесить на данную
     * фабрику.
     * <p/>
     * Необходима при создании сложных структур
     *
     * @return класс-фабрика, создающая объекты нужного типа.
     */
    Class<? extends IObjectFactory> objectFactory() default DefaultFactory.class;

    /**
     * Вести поиск на одной линии в буфере, либо по всему буферу. Не работает
     * при withoutName = true
     *
     * @return - true - поиск по всему буферу, false - до первого символа
     * переноса строки
     */
    boolean dotAll() default true;
}
