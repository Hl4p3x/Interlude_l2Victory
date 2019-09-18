package ru.j2dev.dataparser.holder.buildercmdalias;

import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author : Camelion
 * @date : 25.08.12 22:50
 */
public class Command {
    @StringValue
    public String command;
    @StringValue
    public String alias;
}
