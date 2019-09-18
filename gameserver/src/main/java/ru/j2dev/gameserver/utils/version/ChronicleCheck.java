package ru.j2dev.gameserver.utils.version;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: VISTALL
 * Company: J Develop Station
 * Date: 16:15:53/20.05.2010
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ChronicleCheck {
    Chronicle value();
}