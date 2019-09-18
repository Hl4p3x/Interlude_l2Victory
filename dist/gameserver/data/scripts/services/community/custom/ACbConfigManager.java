package services.community.custom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.configuration.PropertiesParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.handler.bbs.ICommunityBoardHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class ACbConfigManager implements OnInitScriptListener, ICommunityBoardHandler {
    public static final String PVPCB_FILE = "config/communityboard.ini";
    private static final Logger LOGGER = LoggerFactory.getLogger(ACbConfigManager.class);
    public static int FIRST_CLASS_ID;
    public static int FIRST_CLASS_PRICE;
    public static int SECOND_CLASS_ID;
    public static int SECOND_CLASS_PRICE;
    public static int THRID_CLASS_ID;
    public static int THRID_CLASS_PRICE;
    public static boolean ALLOW_PVPCB_ABNORMAL;
    public static boolean ALLOW_PVPCB_SHOP;
    public static boolean ALLOW_PVPCB_ECHANT;
    public static boolean ALLOW_PVPCB_CLASSMASTER;
    public static boolean ALLOW_PVPCB_SUBMANAGER;
    public static boolean ALLOW_PVPCB_SUBMANAGER_PIACE;
    public static boolean ALLOW_PVPCB_TELEPORT;
    public static boolean ALLOW_PVPCB_SELL;
    public static boolean ALLOW_BBS_NEWS;
    public static int PVPCB_NEWS_PER_PAGE;
    public static ArrayList<Integer> ALLOW_PVPCB_MULTISELL_LIST = new ArrayList<>();
    public static int[] COMMUNITY_CLASS_MASTERS_REWARD_ITEM;
    public static int[] COMMUNITY_MASTERS_REWARD_AMOUNT;
    public static StringTokenizer st;
    public static int ALT_CB_TELE_POINT_PRICE;
    public static int ALT_CB_TELE_POINT_MAX_COUNT;
    public static int ALT_CB_DELVLV_ITEM_ID;
    public static long ALT_CB_DELVL_ITEM_COUNT;
    public static int ALT_CB_NOBLES_ITEM_ID;
    public static long ALT_CB_NOBLES_ITEM_COUNT;
    public static int ALT_CB_CHANGESEX_ITEM_ID;
    public static long ALT_CB_CHANGESEX_ITEM_COUNT;
    public static int ALT_CB_CHANGENAME_ITEM_ID;
    public static long ALT_CB_CHANGENAME_ITEM_COUNT;
    public static int ALT_CB_CLANUP_ITEM_ID;
    public static long ALT_CB_CLANUP_ITEM_COUNT;
    public static int ALT_CB_CLAN_PENALTY_ITEM_ID;
    public static long ALT_CB_CLAN_PENALTY_ITEM_COUNT;

    public static void loadPvPCBSettings() {
        final PropertiesParser communityboardpvpSettings = Config.load(PVPCB_FILE);
        ALLOW_PVPCB_ABNORMAL = communityboardpvpSettings.getProperty("AllowBBSAbnormal", false);
        ALLOW_PVPCB_SHOP = communityboardpvpSettings.getProperty("AllowBBSShop", true);
        ALLOW_PVPCB_ECHANT = communityboardpvpSettings.getProperty("AllowBBSEnchant", true);
        ALLOW_PVPCB_CLASSMASTER = communityboardpvpSettings.getProperty("AllowBBSClassMaster", true);
        ALLOW_PVPCB_SUBMANAGER = communityboardpvpSettings.getProperty("AllowBBSSubManager", true);
        ALLOW_PVPCB_SUBMANAGER_PIACE = communityboardpvpSettings.getProperty("AllowBBSSubManagerPiace", true);
        ALLOW_PVPCB_TELEPORT = communityboardpvpSettings.getProperty("AllowBBSTeleport", true);
        ALLOW_BBS_NEWS = communityboardpvpSettings.getProperty("AllowBBSNews", false);
        PVPCB_NEWS_PER_PAGE = communityboardpvpSettings.getProperty("AltBBSNewsNewsPerPage", 6);
        ALLOW_PVPCB_SELL = communityboardpvpSettings.getProperty("AllowBBSSell", true);
        for (final int id : communityboardpvpSettings.getProperty("AllowMultisellList", new int[0])) {
            ALLOW_PVPCB_MULTISELL_LIST.add(id);
        }
        FIRST_CLASS_ID = communityboardpvpSettings.getProperty("FirstProffesionId", 57);
        FIRST_CLASS_PRICE = communityboardpvpSettings.getProperty("FirstProffesionCount", 10000000);
        SECOND_CLASS_ID = communityboardpvpSettings.getProperty("SecondProffesionId", 57);
        SECOND_CLASS_PRICE = communityboardpvpSettings.getProperty("SecondProffesionCount", 20000000);
        THRID_CLASS_ID = communityboardpvpSettings.getProperty("ThridProffesionId", 57);
        THRID_CLASS_PRICE = communityboardpvpSettings.getProperty("ThridProffesionCount", 30000000);
        COMMUNITY_CLASS_MASTERS_REWARD_ITEM = communityboardpvpSettings.getProperty("CommunityProffReward", new int[]{0, 0, 0});
        COMMUNITY_MASTERS_REWARD_AMOUNT = communityboardpvpSettings.getProperty("CommunityProffAmount", new int[]{0, 0, 0});
        ALT_CB_TELE_POINT_PRICE = communityboardpvpSettings.getProperty("CommunityTeleporterPointPrice", 100);
        ALT_CB_TELE_POINT_MAX_COUNT = communityboardpvpSettings.getProperty("CommunityTeleporterPointCount", 10);
        ALT_CB_DELVLV_ITEM_ID = communityboardpvpSettings.getProperty("CommunityDeLevelItemId", 57);
        ALT_CB_DELVL_ITEM_COUNT = communityboardpvpSettings.getProperty("CommunityDeLevelItemCount", 100L);
        ALT_CB_NOBLES_ITEM_ID = communityboardpvpSettings.getProperty("CommunityNobleItemId", 57);
        ALT_CB_NOBLES_ITEM_COUNT = communityboardpvpSettings.getProperty("CommunityNobleItemCount", 100L);
        ALT_CB_CHANGESEX_ITEM_ID = communityboardpvpSettings.getProperty("CommunityChangeSexItemId", 57);
        ALT_CB_CHANGESEX_ITEM_COUNT = communityboardpvpSettings.getProperty("CommunityChangeSexItemCount", 100L);
        ALT_CB_CHANGENAME_ITEM_ID = communityboardpvpSettings.getProperty("CommunityChangeNameItemId", 57);
        ALT_CB_CHANGENAME_ITEM_COUNT = communityboardpvpSettings.getProperty("CommunityChangeNameItemCount", 100L);
        ALT_CB_CLANUP_ITEM_ID = communityboardpvpSettings.getProperty("CommunityClanupNameItemId", 57);
        ALT_CB_CLANUP_ITEM_COUNT = communityboardpvpSettings.getProperty("CommunityClanupItemCount", 100000000L);
        ALT_CB_CLAN_PENALTY_ITEM_ID = communityboardpvpSettings.getProperty("ClanPenaltyClearItem", 4037);
        ALT_CB_CLAN_PENALTY_ITEM_COUNT = communityboardpvpSettings.getProperty("ClanPenaltyClearCount", 1L);
    }

    @Override
    public String[] getBypassCommands() {
        return null;
    }

    @Override
    public void onBypassCommand(final Player player, final String bypass) {
    }

    @Override
    public void onWriteCommand(final Player player, final String bypass, final String arg1, final String arg2, final String arg3, final String arg4, final String arg5) {
    }

    @Override
    public void onInit() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            loadPvPCBSettings();
            LOGGER.info("CommunityBoard: Custom Community Config loaded.");
        }
    }

}
