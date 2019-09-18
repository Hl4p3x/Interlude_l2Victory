package ru.j2dev.dataparser.holder.pcparameter.base_parameter;

import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.holder.setting.common.PlayerRace;
import ru.j2dev.dataparser.holder.setting.common.PlayerSex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KilRoy
 * Используется в качестве INT контейнера для большенства статов из pc_parameter.txt
 */
public class BaseParameterInt {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseParameterInt.class);
    @IntValue
    public int FFighter;
    @IntValue
    public int MFighter;
    @IntValue
    public int FMagic;
    @IntValue
    public int MMagic;
    @IntValue
    public int FElfFighter;
    @IntValue
    public int MElfFighter;
    @IntValue
    public int FElfMagic;
    @IntValue
    public int MElfMagic;
    @IntValue
    public int FDarkelfFighter;
    @IntValue
    public int MDarkelfFighter;
    @IntValue
    public int FDarkelfMagic;
    @IntValue
    public int MDarkelfMagic;
    @IntValue
    public int FOrcFighter;
    @IntValue
    public int MOrcFighter;
    @IntValue
    public int FShaman;
    @IntValue
    public int MShaman;
    @IntValue
    public int FDwarfFighter;
    @IntValue
    public int MDwarfFighter;
    /* GOD
    @IntValue
    public int FDwarfMage;
    @IntValue
    public int MDwarfMage;
    */
    @IntValue
    public int FKamaelSoldier;
    /* GOD
    @IntValue
    public int FKamaelMage;
    @IntValue
    public int MKamaelMage;
    */
    @IntValue
    public int MKamaelSoldier;

    public int getFor(final PlayerSex sex, final PlayerRace playerRace, final boolean isMage) {
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
                    return /*isMage ? FDwarfMage :*/ FDwarfFighter;
                else
                    return /*isMage ? MDwarfMage :*/ MDwarfFighter;
            case kamael:
                if (sex == PlayerSex.FEMALE)
                    return /*isMage ? FKamaelMage :*/ FKamaelSoldier;
                else
                    return /*isMage ? MKamaelMage :*/ MKamaelSoldier;
        }
        LOGGER.warn("Incorect information in request: race="+playerRace+" sex="+sex+" isMage="+isMage);
        return 0;
    }
}