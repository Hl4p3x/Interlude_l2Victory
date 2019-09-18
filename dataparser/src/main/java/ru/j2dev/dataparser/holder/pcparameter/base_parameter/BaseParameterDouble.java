package ru.j2dev.dataparser.holder.pcparameter.base_parameter;

import ru.j2dev.dataparser.annotations.value.DoubleValue;
import ru.j2dev.dataparser.holder.setting.common.PlayerRace;
import ru.j2dev.dataparser.holder.setting.common.PlayerSex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KilRoy
 * Используется в качестве DOUBLE контейнера для большенства статов из pc_parameter.txt
 */
public class BaseParameterDouble {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseParameterDouble.class);
    @DoubleValue
    public double FFighter;
    @DoubleValue
    public double MFighter;
    @DoubleValue
    public double FMagic;
    @DoubleValue
    public double MMagic;
    @DoubleValue
    public double FElfFighter;
    @DoubleValue
    public double MElfFighter;
    @DoubleValue
    public double FElfMagic;
    @DoubleValue
    public double MElfMagic;
    @DoubleValue
    public double FDarkelfFighter;
    @DoubleValue
    public double MDarkelfFighter;
    @DoubleValue
    public double FDarkelfMagic;
    @DoubleValue
    public double MDarkelfMagic;
    @DoubleValue
    public double FOrcFighter;
    @DoubleValue
    public double MOrcFighter;
    @DoubleValue
    public double FShaman;
    @DoubleValue
    public double MShaman;
    @DoubleValue
    public double FDwarfFighter;
    @DoubleValue
    public double MDwarfFighter;
    @DoubleValue
    public double FKamaelSoldier;
    @DoubleValue
    public double MKamaelSoldier;

    public double getFor(final PlayerSex sex, final PlayerRace playerRace, final boolean isMage) {
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
        return 0;
    }
}