package ru.j2dev.gameserver.model.instances;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

/**
 * Created by JunkyFunky
 * on 20.01.2018 16:36
 * group j2dev
 */
@HideAccess
@StringEncryption
public class DayTraderInstance extends NpcInstance {

    public DayTraderInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile("shadowtrader/mammblack_1.htm");
        player.sendPacket(html);
    }

    @Override
    public void onAction(final Player player, final boolean shift) {
        if (player.getPcBangPoints() >= 50) {
            player.reducePcBangPoints(50);
            showChatWindow(player, 0, null);
            player.setTarget(this);
            player.sendActionFailed();
        } else {
            player.sendMessage(player.isLangRus() ? "У вас нет очков Pc Bang" : "You don have Pc Bang points");
            player.sendActionFailed();
        }

    }
}
