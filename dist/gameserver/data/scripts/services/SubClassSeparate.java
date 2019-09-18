package services;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.database.mysql;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.SubClass;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.HtmlUtils;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;

import java.util.Map.Entry;

public class SubClassSeparate extends Functions {

    public void separate_page() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_SEPARATE_SUB_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (player.getSubClasses().size() == 1) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err01.htm"));
            return;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate.htm");
        msg.replace("%item_id%", String.valueOf(Config.SERVICES_SEPARATE_SUB_ITEM));
        msg.replace("%item_count%", String.valueOf(Config.SERVICES_SEPARATE_SUB_PRICE));
        msg.replace("%min_level%", String.valueOf(Config.SERVICES_SEPARATE_SUB_MIN_LEVEL));
        final String item = HtmCache.getInstance().getNotNull("scripts/services/subclass_separate_list.htm", player);
        final StringBuilder sb = new StringBuilder();
        for (final SubClass s : player.getSubClasses().values()) {
            if (!s.isBase()) {
                sb.append(item.replace("%class_id%", String.valueOf(s.getClassId())).replace("%class_name%", HtmlUtils.makeClassNameFString(player, s.getClassId())));
            }
        }
        msg.replace("%list%", sb.toString());
        player.sendPacket(msg);
    }

    public void separate(final String[] arg) {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_SEPARATE_SUB_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (arg == null || arg.length < 2) {
            return;
        }
        if (player.getSubClasses().size() == 1) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err01.htm"));
            return;
        }
        if (!player.getActiveClass().isBase()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err03.htm"));
            return;
        }
        if (player.getActiveClass().getLevel() < 75) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err04.htm"));
            return;
        }
        if (player.isHero()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err05.htm"));
            return;
        }
        final int classtomove = Integer.parseInt(arg[0]);
        int newcharid = 0;
        for (final Entry<Integer, String> e : player.getAccountChars().entrySet()) {
            if (e.getValue().equalsIgnoreCase(arg[1])) {
                newcharid = e.getKey();
            }
        }
        if (newcharid == 0) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err06.htm"));
            return;
        }
        if (mysql.simple_get_int("level", "character_subclasses", "char_obj_id=" + newcharid + " AND level > " + Config.SERVICES_SEPARATE_SUB_MIN_LEVEL) > 1) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err07.htm"));
            return;
        }
        if (!player.isInPeaceZone() || !player.getReflection().isDefault()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err08.htm"));
            return;
        }
        if (ItemFunctions.getItemCount(player, Config.SERVICES_SEPARATE_SUB_ITEM) < Config.SERVICES_SEPARATE_SUB_PRICE) {
            if (Config.SERVICES_SEPARATE_SUB_ITEM == 57) {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            } else {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            }
            return;
        }
        ItemFunctions.removeItem(player, Config.SERVICES_SEPARATE_SUB_ITEM, (long) Config.SERVICES_SEPARATE_SUB_PRICE, true);
        mysql.set("DELETE FROM character_subclasses WHERE char_obj_id=" + newcharid);
        mysql.set("DELETE FROM character_skills WHERE char_obj_id=" + newcharid);
        mysql.set("DELETE FROM character_skills_save WHERE char_obj_id=" + newcharid);
        mysql.set("DELETE FROM character_effects_save WHERE object_id=" + newcharid);
        mysql.set("DELETE FROM character_hennas WHERE char_obj_id=" + newcharid);
        mysql.set("DELETE FROM character_shortcuts WHERE object_id=" + newcharid);
        mysql.set("DELETE FROM character_variables WHERE obj_id=" + newcharid);
        mysql.set("UPDATE character_subclasses SET char_obj_id=" + newcharid + ", isBase=1 WHERE char_obj_id=" + player.getObjectId() + " AND class_id=" + classtomove);
        mysql.set("UPDATE character_skills SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
        mysql.set("UPDATE character_skills_save SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
        mysql.set("UPDATE character_effects_save SET object_id=" + newcharid + " WHERE object_id=" + player.getObjectId() + " AND id=" + classtomove);
        mysql.set("UPDATE character_hennas SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
        mysql.set("UPDATE character_shortcuts SET object_id=" + newcharid + " WHERE object_id=" + player.getObjectId() + " AND class_index=" + classtomove);
        player.modifySubClass(classtomove, 0);
        player.logout();
        Log.add("Character " + player + " subclass separated to " + arg[1], "services");
    }
}
