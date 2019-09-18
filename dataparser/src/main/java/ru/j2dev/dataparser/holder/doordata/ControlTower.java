package ru.j2dev.dataparser.holder.doordata;

import ru.j2dev.dataparser.annotations.array.StringArray;
import ru.j2dev.dataparser.annotations.value.EnumValue;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.common.Point3;

/**
 * @author : Camelion
 * @date : 27.08.12 1:39
 */
public class ControlTower {
    @StringValue(withoutName = true)
    public String name;
    @EnumValue
    public ControlTowerType controltower_type; // Тип контрольной башни
    @EnumValue
    public ControlTowerToggle toggle; // Неизвестно
    @ObjectValue
    public Point3 pos; // позиция башни
    @IntValue
    public int hp; // HP башни
    @IntValue
    public int physical_defence; // P.Def башни
    @IntValue
    public int magical_defence; // M.Def башни
    @StringValue
    public String display_npc_working; // NPC, который отображается на месте
    // работающей башни
    @StringValue
    public String display_npc_not_working; // NPC, который отображается на месте
    // не работающей башни
    @StringArray
    public String[] control_area; // Список каких-то зон, отсутвует для башен
    // типа life_control

    public enum ControlTowerType {
        support_control,
        life_control,
        trap_control
    }

    public enum ControlTowerToggle {
        flase
    }
}
