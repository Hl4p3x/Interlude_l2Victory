package ru.j2dev.dataparser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.dataparser.parser.*;
import ru.j2dev.dataparser.pch.LinkerFactory;

/**
 * @author : Camelion
 * @date : 25.08.12 12:54
 * <p/>
 * Главный загрузчик, который управляет последовательностью загрузки
 * датапака
 */
public class DataLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    public static void load() {
        try {
            LOGGER.info("Loading PTS scripts data");
            LinkerFactory.getInstance().load();
            //AirshipParser.getInstance().load();
            //AnnounceSphereParser.getInstance().load();
            //AreaDataParser.getInstance().load();
            //ArmorEnchantBonusDataParser.getInstance().load();
            //AuctionDataParser.getInstance().load();
            //BuilderCmdAliasParser.getInstance().load();
            //CastleDataParser.getInstance().load();
            CategoryDataParser.getInstance().load();
            //ConvertDataParser.getInstance().load();
            CubicDataParser.getInstance().load();
            CursedWeaponDataParser.getInstance().load();
            DecoDataParser.getInstance().load();
            DoorDataParser.getInstance().load();
            DyeDataParser.getInstance().load();
            //EnchantOptionParser.getInstance().load();
            //ExpDataParser.getInstance().load();
            //EventDataParser.getInstance().load();
            //FieldCycleParser.getInstance().load();
            FishingDataParser.getInstance().load();
            FormationInfoParser.getInstance().load();
            FreewayInfoParser.getInstance().load();
            FStringParser.getInstance().load();
            //InstantZoneDataParser.getInstance().load();
            ItemDataParser.getInstance().load();
            ManorDataParser.getInstance().load();
            //MinigameParser.getInstance().load();
            //MonraceParser.getInstance().load();
            MultisellParser.getInstance().load();
            NpcDataParser.getInstance().load();
            NpcPosParser.getInstance().load();
            PetDataParser.getInstance().load();
            //SettingParser.getInstance().load();
            //TransformParser.getInstance().load();
            //RestrictAreaDataParser.getInstance().load();
            PCParameterParser.getInstance().load();
            //SkillDataParser.getInstance().load();
            //SkillAcquireParser.getInstance().load();
            //UserBasicActionParser.getInstance().load();
            //VariationParser.getInstance().load();
            Parser.clear();
            LOGGER.info("End loading PTS scripts data");
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }
}