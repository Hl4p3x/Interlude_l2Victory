package ru.j2dev.dataparser.holder.setting;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.holder.setting.common.PlayerRace;

/**
 * @author : Camelion
 * @date : 22.08.12 23:37
 * <p/>
 * Класс, содержащий в себе максимальные базовые
 * статы(INT/STR/CON/MEN/DEX/WIT) персонажей
 */
public class MaximumStat {
    // Список предметов для каждого класса
    @IntArray(canBeNull = false)
    public int[] human_fighter;
    @IntArray(canBeNull = false)
    public int[] human_magician;
    @IntArray(canBeNull = false)
    public int[] elf_fighter;
    @IntArray(canBeNull = false)
    public int[] elf_magician;
    @IntArray(canBeNull = false)
    public int[] darkelf_fighter;
    @IntArray(canBeNull = false)
    public int[] darkelf_magician;
    @IntArray(canBeNull = false)
    public int[] orc_fighter;
    @IntArray(canBeNull = false)
    public int[] orc_shaman;
    @IntArray(canBeNull = false)
    public int[] dwarf_apprentice;
    /* GOD
    @IntArray(canBeNull = false)
    public int[] dwarf_mage;
    */
    @IntArray(canBeNull = false)
    public int[] kamael_m_soldier;
    @IntArray(canBeNull = false)
    public int[] kamael_f_soldier;

    public int[] getFor(PlayerRace race, boolean mageClass) {
        int[] stat = new int[6];
        switch (race) {
            case human:
                if (mageClass)
                    return human_magician;
                return human_fighter;
            case elf:
                if (mageClass)
                    return elf_magician;
                return elf_fighter;
            case darkelf:
                if (mageClass)
                    return darkelf_magician;
                return darkelf_fighter;
            case orc:
                if (mageClass)
                    return orc_shaman;
                return orc_fighter;
            case dwarf:
				/* GOD
				if(mageClass)
					return dwarf_mage;
				*/
                return dwarf_apprentice;
            case kamael:
                if (mageClass)
                    return kamael_f_soldier;
                return kamael_f_soldier;
        }
        return stat;
    }
}
