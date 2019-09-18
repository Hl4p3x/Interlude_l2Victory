package zones;


import ru.j2dev.commons.configuration.PropertiesParser;
import ru.j2dev.gameserver.Config;

import java.io.File;

/**
 * Created by JunkyFunky
 * on 09.03.2018 20:37
 * group j2dev
 */
public class CustomRewardZoneConfig {
    public static boolean allowCustomRewardZones;
    public static String[] pvpRewardItems;
    public static String[] pkRewardItems;
    public static String[] rewardZones;
    public static boolean hwidCheck;
    public static boolean ipCheck;
    private static String CONFIG_FILE = "config/custom_zone.ini";

    public static void load() {
        final PropertiesParser properties = Config.load(new File(CONFIG_FILE));
        allowCustomRewardZones = properties.getProperty("allowCustomRewardZone", false);
        pvpRewardItems = properties.getProperty("pvpRewardItems", new String[0]);
        pkRewardItems = properties.getProperty("pkRewardItems", new String[0]);
        rewardZones = properties.getProperty("rewardZones", new String[0]);
        hwidCheck = properties.getProperty("hwidCheck", false);
        ipCheck = properties.getProperty("ipCheck", false);
    }
}
