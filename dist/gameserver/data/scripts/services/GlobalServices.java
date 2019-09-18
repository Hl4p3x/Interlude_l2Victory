package services;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.Config.RateBonusInfo;
import ru.j2dev.gameserver.dao.AccountBonusDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.ItemFunctions;

public class GlobalServices extends Functions {
    public static boolean makeCustomHero(final Player player, final long customHeroDuration) {
        if (player.isHero() || customHeroDuration <= 0L) {
            return false;
        }
        player.setCustomHero(true, customHeroDuration, true);
        player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
        player.broadcastUserInfo(true);
        return true;
    }

    public static boolean makeCustomPremium(final Player player, final int id) {

        RateBonusInfo rateBonusInfo = null;
        for (final RateBonusInfo rbi : Config.SERVICES_RATE_BONUS_INFO_RB) {
            if (rbi.id == id) {
                rateBonusInfo = rbi;
            }
        }
        AccountBonusDAO.getInstance().store(player.getAccountName(), rateBonusInfo.makeBonus());
        player.stopBonusTask();
        player.startBonusTask();
        rateBonusInfo.rewardItem.forEach(rewardPair -> ItemFunctions.addItem(player, rewardPair.getLeft(), rewardPair.getRight(), true));
        if (rateBonusInfo.nameColor != null) {
            player.setNameColor(rateBonusInfo.nameColor);
        }
        if (player.getParty() != null) {
            player.getParty().recalculatePartyData();
        }
        player.broadcastUserInfo(true);
        return true;
    }
}
