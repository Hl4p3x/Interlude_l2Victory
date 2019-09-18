package ru.j2dev.dataparser.holder.setting;

import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.common.ItemName_Count;

/**
 * @author : Camelion
 * @date : 21.08.12 14:13
 * <p/>
 * Содержит в себе информацию о преметах, выдаваемых игрокам.
 * (Используется на китайских серверах взамен InitialEquipment)
 */
public class InitialCustomEquipment {
    // Список предметов для каждого класса
    @ObjectArray
    public ItemName_Count[] human_fighter;
    @ObjectArray
    public ItemName_Count[] human_magician;
    @ObjectArray
    public ItemName_Count[] elf_fighter;
    @ObjectArray
    public ItemName_Count[] elf_magician;
    @ObjectArray
    public ItemName_Count[] darkelf_fighter;
    @ObjectArray
    public ItemName_Count[] darkelf_magician;
    @ObjectArray
    public ItemName_Count[] orc_fighter;
    @ObjectArray
    public ItemName_Count[] orc_shaman;
    @ObjectArray
    public ItemName_Count[] dwarf_apprentice;
    @ObjectArray
    public ItemName_Count[] kamael_m_soldier;
    @ObjectArray
    public ItemName_Count[] kamael_f_soldier;
}
