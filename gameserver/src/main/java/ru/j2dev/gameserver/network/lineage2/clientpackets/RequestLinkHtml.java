package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;

public class RequestLinkHtml extends L2GameClientPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLinkHtml.class);

    private String _link;

    @Override
    protected void readImpl() {
        _link = readS();
    }

    @Override
    protected void runImpl() {
        final Player actor = getClient().getActiveChar();
        if (actor == null) {
            return;
        }
        if (_link.contains("..") || !_link.endsWith(".htm")) {
            LOGGER.warn("[RequestLinkHtml] hack? link contains prohibited characters: '" + _link + "', skipped");
            return;
        }
        try {
            final NpcHtmlMessage msg = new NpcHtmlMessage(0);
            msg.setFile("" + _link);
            sendPacket(msg);
        } catch (Exception e) {
            LOGGER.warn("Bad RequestLinkHtml: ", e);
        }
    }
}
