package ru.j2dev.dataparser.annotations.array;


import ru.j2dev.dataparser.annotations.factory.DefaultFactory;
import ru.j2dev.dataparser.annotations.factory.IObjectFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Camelion
 * @date : 22.08.12 13:42 Показывает парсеру, что поле класса должно считываться
 * как массив Object типа {@link class()}
 * <p/>
 * Если поле класса имеет тип List<Object[]> то этот список будет
 * заполняться элементами типа Object[]
 * <p/>
 * Для List<Object[]> поля, помеченного этим типом обязательно должно
 * указываться имя. Т.е. в список будут попадать элементы с одинаковыми
 * именами. Пример правильного List списка:
 * human_fighter={{[squire's_sword];1};{[dagger];1}}
 * human_fighter={{[apprentice's_wand];1};{[apprentice's_tunic];1}}
 * <p/>
 * Пример неправильного списка. human_fighter={{[squire
 * 's_sword];1};{[dagger];1};{[apprentice's_wand];1};{[apprentice's_tunic];1}
 * } Такой список должен обрабатываться в другом классе. Пример
 * представлен ниже.
 * <p/>
 * Отличия от аннотаций IntArray, EnumArray, etc. состоит в том, что из
 * массивов типа ObjectArray можно составить многомерные массивы типа
 * Object[][]. Также можно (и нужно) использовать пользовательский тип
 * данных.
 * <p/>
 * После выделения массива(выделение происходит без использования шаблона,
 * массив выделяется по фигурным скобкам {}) управление передается в
 * класс, которым объявлено поле Например для строки: {[aaa]22};{[bbb]333}
 * будет соответствовать следующая запись: *
 * <p>
 * <pre>
 * class A {
 *   &#064;ObjectArray private StringIntPair[] pair;
 * <p/>
 *   public static class StringIntPair {
 * 	  &#064;StringValue
 * 	  private String stringValue;
 * <p/>
 * 	  &#064;IntValue
 * 	  private int intValue;
 *   }
 * }
 * </pre>
 * <p/>
 * Пример многомерного массива: Для строки типа:
 * {{123;123;321};{123;415;-124};{123;-123;412}} будет соответствовать
 * следующая запись: *
 * <p>
 * <pre>
 * class A {
 *   &#064;ObjectArray private MyIntArray[] pair;
 * <p/>
 *   public static class MyIntArray {
 * 	  &#064;IntArray
 * 	  private int[] array;
 * }
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
// Target field
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectArray {
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
     * Могут ли вложенные массивы, либо сам массив принимать значение null true
     * - включает дополнительную проверку перед записью значения.
     *
     * @return true - поле класса может принимать значение null
     */
    boolean canBeNull() default true;

    /**
     * Возвращает класс-фабрику, которая управляет созданием объектов массива.
     * Позволяет указать в качестве типа массива - какой-нибудь супер-класс, а
     * управление объектами повесить на данную фабрику.
     * <p/>
     * Жизненно необходима для обхода строк, где тип объекта можно выделить
     * только по его содержимому, например (airship.txt): path =
     * {{move;{-153414;
     * 255385;221}};{airport;1};{tel;{-167672;256936;-594};32768}
     * ;{move;{-149548;258172;221}};}
     *
     * @return класс-фабрика, создающая объекты нужного типа для массива.
     */
    Class<? extends IObjectFactory> objectFactory() default DefaultFactory.class;
}
