package ru.j2dev.dataparser.annotations.class_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Camelion
 * @date : 25.08.12 14:29
 * <p/>
 * Указывает парсеру на то, что сперва необходимо обработать супер-класс
 * объекта Чаще всего применяется вместе с фабриками объектов, когда тип
 * объекта заранее не известен, но объекты объединены общими параметрами
 * Эти параметры и должен инициализировать супер класс.
 * <p/>
 * Применятеся при обработке @ObjectValue, @ObjectArray, @Element, @ElementArray
 */
@Target(ElementType.TYPE)
// Target class
@Retention(RetentionPolicy.RUNTIME)
public @interface ParseSuper {
}
