package ru.j2dev.dataparser.holder.setting.common;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.dataparser.common.ItemName_Count;
import ru.j2dev.dataparser.holder.SettingHolder;
import ru.j2dev.dataparser.holder.setting.InitialStartPoint.PlayerClasses;
import ru.j2dev.dataparser.holder.setting.InitialStartPoint.StartPoint;
import org.apache.commons.lang3.ArrayUtils;

public class SettingUtils {
    public static ItemName_Count[] calculateItemEquip(final int classId, final boolean custom) {
        switch (classId) {
            case 0:
                if (custom)
                    return SettingHolder.getInstance().getInitialCustomEquipment().human_fighter;
                return SettingHolder.getInstance().getInitialEquipment().human_fighter;
            case 10:
                if (custom)
                    return SettingHolder.getInstance().getInitialCustomEquipment().human_magician;
                return SettingHolder.getInstance().getInitialEquipment().human_magician;
            case 18:
                if (custom)
                    return SettingHolder.getInstance().getInitialCustomEquipment().elf_fighter;
                return SettingHolder.getInstance().getInitialEquipment().elf_fighter;
            case 25:
                if (custom)
                    return SettingHolder.getInstance().getInitialCustomEquipment().elf_magician;
                return SettingHolder.getInstance().getInitialEquipment().elf_magician;
            case 31:
                if (custom)
                    return SettingHolder.getInstance().getInitialCustomEquipment().darkelf_fighter;
                return SettingHolder.getInstance().getInitialEquipment().darkelf_fighter;
            case 38:
                if (custom)
                    return SettingHolder.getInstance().getInitialCustomEquipment().darkelf_magician;
                return SettingHolder.getInstance().getInitialEquipment().darkelf_magician;
            case 44:
                if (custom)
                    return SettingHolder.getInstance().getInitialCustomEquipment().orc_fighter;
                return SettingHolder.getInstance().getInitialEquipment().orc_fighter;
            case 49:
                if (custom)
                    return SettingHolder.getInstance().getInitialCustomEquipment().orc_shaman;
                return SettingHolder.getInstance().getInitialEquipment().orc_shaman;
            case 53:
                if (custom)
                    return SettingHolder.getInstance().getInitialCustomEquipment().dwarf_apprentice;
                return SettingHolder.getInstance().getInitialEquipment().dwarf_apprentice;
            case 123:
                if (custom)
                    return SettingHolder.getInstance().getInitialCustomEquipment().kamael_m_soldier;
                return SettingHolder.getInstance().getInitialEquipment().kamael_m_soldier;
            case 124:
                if (custom)
                    return SettingHolder.getInstance().getInitialCustomEquipment().kamael_f_soldier;
                return SettingHolder.getInstance().getInitialEquipment().kamael_f_soldier;
        }
        return SettingHolder.getInstance().getInitialCustomEquipment().human_fighter;
    }

    public static int[] calculateStartPoint(final PlayerClasses playerClasses) {
        for (final StartPoint startPoints : SettingHolder.getInstance().getInitialStartPoint().getPoints()) {
            if (ArrayUtils.contains(startPoints.getClasses(), playerClasses))
                return Rnd.get(startPoints.getPoints());
        }
        return Rnd.get(SettingHolder.getInstance().getInitialStartPoint().points.get(0).getPoints());
    }
}