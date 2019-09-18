package ru.j2dev.gameserver.handler.usercommands.impl;

import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.handler.usercommands.IUserCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.utils.HtmlUtils;

import java.text.SimpleDateFormat;

public class ClanPenalty implements IUserCommandHandler {
    private static final int[] COMMAND_IDS = {100, 114};

    @Override
    public boolean useUserCommand(final int id, final Player activeChar) {
        if (COMMAND_IDS[0] != id) {
            return false;
        }
        long leaveClan = 0L;
        if (activeChar.getLeaveClanTime() != 0L) {
            leaveClan = activeChar.getLeaveClanTime() + 86400000L;
        }
        long deleteClan = 0L;
        if (activeChar.getDeleteClanTime() != 0L) {
            deleteClan = activeChar.getDeleteClanTime() + 864000000L;
        }
        final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String html = HtmCache.getInstance().getNotNull("command/penalty.htm", activeChar);
        if (activeChar.getClanId() == 0) {
            if (leaveClan == 0L && deleteClan == 0L) {
                html = html.replaceFirst("%reason%", "No penalty is imposed.");
                html = html.replaceFirst("%expiration%", " ");
            } else if (leaveClan > 0L && deleteClan == 0L) {
                html = html.replaceFirst("%reason%", "Penalty for leaving clan.");
                html = html.replaceFirst("%expiration%", format.format(leaveClan));
            } else if (deleteClan > 0L) {
                html = html.replaceFirst("%reason%", "Penalty for dissolving clan.");
                html = html.replaceFirst("%expiration%", format.format(deleteClan));
            }
        } else if (activeChar.getClan().canInvite()) {
            html = html.replaceFirst("%reason%", "No penalty is imposed.");
            html = html.replaceFirst("%expiration%", " ");
        } else {
            html = html.replaceFirst("%reason%", "Penalty for expelling clan member.");
            html = html.replaceFirst("%expiration%", format.format(activeChar.getClan().getExpelledMemberTime()));
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5);
        msg.setHtml(HtmlUtils.bbParse(html));
        activeChar.sendPacket(msg);
        return true;
    }

    @Override
    public final int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
