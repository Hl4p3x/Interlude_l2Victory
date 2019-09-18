package ru.j2dev.dataparser.holder.categorydata;

import ru.j2dev.dataparser.annotations.array.LinkedArray;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author : Camelion
 * @date : 26.08.12 12:12
 */
public class CategoryDefine {
    @StringValue
    public String name; // Название категории, совпадает с category_pch.txt
    @LinkedArray
    public int[] category;// Список классов, попадающих в категорию ( совпадает
    // с manual_pch.txt)
}
