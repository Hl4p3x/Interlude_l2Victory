package ru.j2dev.dataparser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Camelion
 * @date : 24.08.12 18:06 Показывает парсеру, что поле класса должно считываться
 * как массив элементов.
 * <p/>
 * Если поле класса имеет тип List<E[]>, то этот список будет заполняться
 * элементами типа E[]
 * <p/>
 * Применяется при подобных структурах: teleport_begin // Начало
 * локального буффера id = expel fuel = 0 tel_pos = {-186373;234000;2500}
 * id = 0 fuel = 50 tel_pos = {-206692;220997;3000} id = 1 fuel = 100
 * tel_pos = {-235693;248843;5100} id = 2 fuel = 150 tel_pos =
 * {-179557;165491;4105} // Конец локального буффера teleport_end
 * <p/>
 * Каждой новой строке соответствует новый элемент ( в данном примере
 * будет массив из 4-х элементов
 */
@Target(ElementType.FIELD)
// Target field
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementArray {
    /**
     * @return - строка - индикатор начала массива элементов
     */
    String start();

    /**
     * @return - строка - индикатор окончания массива элементов
     */
    String end();
}