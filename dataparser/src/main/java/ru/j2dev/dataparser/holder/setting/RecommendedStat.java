package ru.j2dev.dataparser.holder.setting;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.holder.setting.common.ClassID;
import ru.j2dev.dataparser.holder.setting.common.PlayerRace;

/**
 * @author : Camelion
 * @date : 22.08.12 23:44
 * <p/>
 * Класс, содержащий в себе рекомендуемые(стандартные) статы персонажей
 * (INT/STR/CON/MEN/DEX/WIT)
 */
public class RecommendedStat {
    // Список статов для каждого начального класса
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

    public int[] getFor(PlayerRace race, ClassID classID) {
        int[] stat = new int[6];
        switch (race) {
            case human:
                if (classID == ClassID.fighter)
                    return human_fighter;
                else if (classID == ClassID.mage)
                    return human_magician;
                break;
            case elf:
                if (classID == ClassID.elven_fighter)
                    return elf_fighter;
                else if (classID == ClassID.elven_mage)
                    return elf_magician;
                break;
            case darkelf:
                if (classID == ClassID.dark_fighter)
                    return darkelf_fighter;
                else if (classID == ClassID.dark_mage)
                    return darkelf_magician;
                break;
            case orc:
                if (classID == ClassID.orc_fighter)
                    return orc_fighter;
                else if (classID == ClassID.orc_mage)
                    return orc_shaman;
                break;
            case dwarf:
				/* GOD
				if(classID != ClassID.dwarven_fighter)
					return dwarf_mage;
				*/
                if (classID == ClassID.dwarven_fighter)
                    return dwarf_apprentice;
                break;
            case kamael:
                if (classID == ClassID.kamael_f_soldier)
                    return kamael_f_soldier;
                else if (classID == ClassID.kamael_m_soldier)
                    return kamael_m_soldier;
                break;
        }
        return stat;
    }
}
