package ru.j2dev.dataparser.holder.convertdata;

import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.pch.LinkerFactory;

/**
 * @author : Camelion
 * @date : 26.08.12 12:46
 */
public class ConvertData {
    @StringValue
    public String input_item; // Что конвртируем
    @StringValue
    public String output_item; // Во что конвертируем

    public int getInput() {
        return LinkerFactory.getInstance().findClearValue(input_item);
    }

    public int getOutput() {
        return LinkerFactory.getInstance().findClearValue(output_item);
    }
}
