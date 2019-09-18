package npc.model.residences.clanhall;

import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.TimeUtils;

public class BrakelInstance extends NpcInstance {
    public BrakelInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        final ClanHall clanhall = ResidenceHolder.getInstance().getResidence(ClanHall.class, 21);
        if (clanhall == null) {
            return;
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile("residence2/clanhall/partisan_ordery_brakel001.htm");
        html.replace("%next_siege%", TimeUtils.toSimpleFormat(clanhall.getSiegeDate().getTimeInMillis()));
        player.sendPacket(html);
    }
}
