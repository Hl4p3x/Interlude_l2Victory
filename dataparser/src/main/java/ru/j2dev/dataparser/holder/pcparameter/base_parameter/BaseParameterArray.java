package ru.j2dev.dataparser.holder.pcparameter.base_parameter;

import ru.j2dev.dataparser.annotations.array.DoubleArray;
import ru.j2dev.dataparser.holder.setting.common.PlayerRace;
import ru.j2dev.dataparser.holder.setting.common.PlayerSex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KilRoy
 * Используется в качестве Array контейнера для большенства статов из pc_parameter.txt
 */
public class BaseParameterArray {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseParameterArray.class);
    @DoubleArray(canBeNull = false)
    public double[] FFighter;
    @DoubleArray(canBeNull = false)
    public double[] MFighter;
    @DoubleArray(canBeNull = false)
    public double[] FMagic;
    @DoubleArray(canBeNull = false)
    public double[] MMagic;
    @DoubleArray(canBeNull = false)
    public double[] FElfFighter;
    @DoubleArray(canBeNull = false)
    public double[] MElfFighter;
    @DoubleArray(canBeNull = false)
    public double[] FElfMagic;
    @DoubleArray(canBeNull = false)
    public double[] MElfMagic;
    @DoubleArray(canBeNull = false)
    public double[] FDarkelfFighter;
    @DoubleArray(canBeNull = false)
    public double[] MDarkelfFighter;
    @DoubleArray(canBeNull = false)
    public double[] FDarkelfMagic;
    @DoubleArray(canBeNull = false)
    public double[] MDarkelfMagic;
    @DoubleArray(canBeNull = false)
    public double[] FOrcFighter;
    @DoubleArray(canBeNull = false)
    public double[] MOrcFighter;
    @DoubleArray(canBeNull = false)
    public double[] FShaman;
    @DoubleArray(canBeNull = false)
    public double[] MShaman;
    @DoubleArray(canBeNull = false)
    public double[] FDwarfFighter;
    @DoubleArray(canBeNull = false)
    public double[] MDwarfFighter;
    /* GOD
    @DoubleArray(canBeNull = false)
    public double[] FDwarfMage;
    @DoubleArray(canBeNull = false)
    public double[] MDwarfMage;
    */
    @DoubleArray(canBeNull = false)
    public double[] FKamaelSoldier;
    /* GOD
    @DoubleArray(canBeNull = false)
    public double[] FKamaelMage;
    @DoubleArray(canBeNull = false)
    public double[] MKamaelMage;
    */
    @DoubleArray(canBeNull = false)
    public double[] MKamaelSoldier;

    public double[] getFor(final PlayerSex sex, final PlayerRace playerRace, final boolean isMage) {
        switch (playerRace) {
            case human:
                if (sex == PlayerSex.FEMALE)
                    return isMage ? FMagic : FFighter;
                else
                    return isMage ? MMagic : MFighter;
            case elf:
                if (sex == PlayerSex.FEMALE)
                    return isMage ? FElfMagic : FElfFighter;
                else
                    return isMage ? MElfMagic : MElfFighter;
            case darkelf:
                if (sex == PlayerSex.FEMALE)
                    return isMage ? FDarkelfMagic : FDarkelfFighter;
                else
                    return isMage ? MDarkelfMagic : MDarkelfFighter;
            case orc:
                if (sex == PlayerSex.FEMALE)
                    return isMage ? FShaman : FOrcFighter;
                else
                    return isMage ? MShaman : MOrcFighter;
            case dwarf:
                if (sex == PlayerSex.FEMALE)
                    return FDwarfFighter;
                else
                    return MDwarfFighter;
            case kamael:
                if (sex == PlayerSex.FEMALE)
                    return FKamaelSoldier;
                else
                    return MKamaelSoldier;
        }
        LOGGER.warn("Incorect information in request: race="+playerRace+" sex="+sex+" isMage="+isMage);
        return new double[0];
    }
}