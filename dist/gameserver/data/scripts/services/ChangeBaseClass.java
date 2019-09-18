package services;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.SubClass;
import ru.j2dev.gameserver.model.base.PlayerClass;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.HtmlUtils;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;

import java.util.ArrayList;
import java.util.List;

public class ChangeBaseClass extends Functions {
    public void changebase_page() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CHANGE_BASE_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (player.getSubClasses().size() == 1) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/changebase_err01.htm"));
            return;
        }
        if (!player.getActiveClass().isBase()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/changebase_err02.htm"));
            return;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/changebase.htm");
        msg.replace("%item_id%", String.valueOf(Config.SERVICES_CHANGE_BASE_ITEM));
        msg.replace("%item_count%", String.valueOf(Config.SERVICES_CHANGE_BASE_PRICE));
        final List<SubClass> possible = new ArrayList<>();
        if (player.getActiveClass().isBase()) {
            possible.addAll(player.getSubClasses().values());
            possible.remove(player.getSubClasses().get(player.getBaseClassId()));
            for (final SubClass s : player.getSubClasses().values()) {
                for (final SubClass s2 : player.getSubClasses().values()) {
                    if (s == s2) {
                        continue;
                    }
                    if (Config.SERVICES_CHANGE_BASE_LIST_UNCOMPATABLE) {
                        continue;
                    }
                    if (PlayerClass.areClassesComportable(PlayerClass.values()[s.getClassId()], PlayerClass.values()[s2.getClassId()])) {
                        continue;
                    }
                    if (s2.getLevel() < 75) {
                        continue;
                    }
                    possible.remove(s2);
                }
            }
        }
        final StringBuilder sb = new StringBuilder();
        if (!possible.isEmpty()) {
            final String item = HtmCache.getInstance().getNotNull("scripts/services/changebase_list.htm", player);
            for (final SubClass s3 : possible) {
                sb.append(item.replace("%class_id%", String.valueOf(s3.getClassId())).replace("%class_name%", HtmlUtils.makeClassNameFString(player, s3.getClassId())));
            }
        }
        msg.replace("%list%", sb.toString());
        player.sendPacket(msg);
    }

    public void changebase(final String[] arg) {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (arg == null || arg.length < 1) {
            return;
        }
        if (!Config.SERVICES_CHANGE_BASE_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (player.getSubClasses().size() == 1) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/changebase_err01.htm"));
            return;
        }
        if (!player.getActiveClass().isBase()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/changebase_err02.htm"));
            return;
        }
        if (!player.isInPeaceZone() || !player.getReflection().isDefault()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/changebase_err03.htm"));
            return;
        }
        if (player.isHero()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/changebase_err04.htm"));
            return;
        }
        if (ItemFunctions.getItemCount(player, Config.SERVICES_CHANGE_BASE_ITEM) < Config.SERVICES_CHANGE_BASE_PRICE) {
            if (Config.SERVICES_CHANGE_BASE_ITEM == 57) {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            } else {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            }
            return;
        }
        ItemFunctions.removeItem(player, Config.SERVICES_CHANGE_BASE_ITEM, (long) Config.SERVICES_CHANGE_BASE_PRICE, true);
        final int target = Integer.parseInt(arg[0]);
        final SubClass newBase = player.getSubClasses().get(target);
        player.getActiveClass().setBase(false);
        player.getActiveClass().setExp(player.getExp());
        player.checkSkills();
        newBase.setBase(true);
        player.setBaseClass(target);
        player.setHairColor(0);
        player.setHairStyle(0);
        player.setFace(0);
        if (player.isNoble()) {
            player.setNoble(false);
            NoblessManager.getInstance().removeNoble(player);
            NoblessManager.getInstance().addNoble(player);
            player.setNoble(true);
        }
        player.logout();
        Log.add("Character " + player + " base changed to " + target, "services");
    }
}
