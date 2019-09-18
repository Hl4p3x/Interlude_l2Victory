package ru.j2dev.dataparser.holder.auctiondata;

import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author : Camelion
 * @date : 25.08.12 18:59
 */
public class AuctionItem {
    @StringValue
    public String item; // Название предмета
    @IntValue
    public int amount; // Кол-во
    @IntValue
    public int price; // Вероятно, стартовая цена
    @IntValue
    private int registration; // Неизвестно. Принимает значения 33-34
}
