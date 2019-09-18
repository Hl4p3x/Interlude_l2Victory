package services;

import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChangeSex extends Functions {
    public void changesex_page() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CHANGE_SEX_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/sex_change.htm");
        msg.replace("%item_id%", String.valueOf(Config.SERVICES_CHANGE_SEX_ITEM));
        msg.replace("%item_count%", String.valueOf(Config.SERVICES_CHANGE_SEX_PRICE));
        player.sendPacket(msg);
    }

    public void change_sex() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CHANGE_SEX_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (!player.isInPeaceZone() || !player.getReflection().isDefault()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_peace_zone.htm"));
            return;
        }
        if (ItemFunctions.getItemCount(player, Config.SERVICES_CHANGE_SEX_ITEM) < Config.SERVICES_CHANGE_SEX_PRICE) {
            if (Config.SERVICES_CHANGE_SEX_ITEM == 57) {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            } else {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            }
            return;
        }
        ItemFunctions.removeItem(player, Config.SERVICES_CHANGE_SEX_ITEM, (long) Config.SERVICES_CHANGE_SEX_PRICE, true);
        Connection con = null;
        PreparedStatement offline = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            offline = con.prepareStatement("UPDATE characters SET sex = ? WHERE obj_Id = ?");
            offline.setInt(1, (player.getSex() != 1) ? 1 : 0);
            offline.setInt(2, player.getObjectId());
            offline.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DbUtils.closeQuietly(con, offline);
        }
        player.setHairColor(0);
        player.setHairStyle(0);
        player.setFace(0);
        Log.add("Character " + player + " sex changed to " + ((player.getSex() == 1) ? "male" : "female"), "renames");
        player.logout();
    }
}
