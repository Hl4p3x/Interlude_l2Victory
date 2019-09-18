package ru.j2dev.gameserver.network.lineage2.serverpackets;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.handler.npcdialog.INpcDialogAppender;
import ru.j2dev.gameserver.handler.npcdialog.NpcDialogAppenderHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.components.NpcString;
import ru.j2dev.gameserver.utils.HtmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NpcHtmlMessage extends L2GameServerPacket {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NpcHtmlMessage.class);
    protected static final Pattern objectId = Pattern.compile("%objectId%");
    protected static final Pattern playername = Pattern.compile("%playername%");

    private int _npcObjId;
    private String _html;
    private String _file;
    private List<String> _replaces;
    private boolean have_appends;

    public NpcHtmlMessage(final Player player, final int npcId, final String filename, final int val) {
        _file = null;
        _replaces = new ArrayList<>();
        have_appends = false;
        setFile(filename);
    }

    public NpcHtmlMessage(final Player player, final NpcInstance npc, final String filename, final int val) {
        this(player, npc.getNpcId(), filename, val);
        _npcObjId = npc.getObjectId();
        player.setLastNpc(npc);

        final List<INpcDialogAppender> appends = NpcDialogAppenderHolder.getInstance().getAppenders(npc.getNpcId());
        if (appends != null && !appends.isEmpty()) {
            have_appends = true;
            for (INpcDialogAppender append : appends) {
                String returnVal = append.getAppend(player, npc, val);
                if (returnVal != null) {
                    replace("</body>", "<br>" + HtmlUtils.bbParse(returnVal) + "</body>");
                }
            }
        }
        replace("%npcId%", String.valueOf(npc.getNpcId()));
        replace("%npcname%", npc.getName());
        replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
    }

    public NpcHtmlMessage(final Player player, final NpcInstance npc) {
        _file = null;
        _replaces = new ArrayList<>();
        have_appends = false;
        if (npc == null) {
            _npcObjId = 5;
            player.setLastNpc(null);
        } else {
            _npcObjId = npc.getObjectId();
            player.setLastNpc(npc);
        }
    }

    public NpcHtmlMessage(final int npcObjId) {
        _file = null;
        _replaces = new ArrayList<>();
        have_appends = false;
        _npcObjId = npcObjId;
    }

    public final NpcHtmlMessage setHtml(String text) {
        if (!text.contains("<html>")) {
            text = "<html><body>" + text + "</body></html>";
        }
        _html = text;
        return this;
    }

    public final NpcHtmlMessage setFile(final String file) {
        _file = file;
        if (_file.startsWith("data/html/")) {
            LOGGER.info("NpcHtmlMessage: need fix : " + file, new Exception());
            _file = _file.replace("data/html/", "");
        }
        return this;
    }

    public NpcHtmlMessage replace(final String pattern, final String value) {
        if (pattern == null || value == null) {
            return this;
        }
        _replaces.add(pattern);
        _replaces.add(value);
        return this;
    }

    public NpcHtmlMessage replaceNpcString(final String pattern, final NpcString npcString, final Object... arg) {
        if (pattern == null) {
            return this;
        }
        if (npcString.getSize() != arg.length) {
            throw new IllegalArgumentException("Not valid size of parameters: " + npcString);
        }
        _replaces.add(pattern);
        _replaces.add(HtmlUtils.htmlNpcString(npcString, arg));
        return this;
    }

    public void processHtml(final GameClient client) {
        final Player player = client.getActiveChar();
        if (_file != null) {
            if (player != null && player.isGM()) {
                final String str = _file.lastIndexOf("/") > 0 ? _file.substring(_file.lastIndexOf("/")).replace("/", "") : _file;
                if (!StringUtils.EMPTY.equals(str))
                    player.sendHTMLMessage(str);
            }
            final String content = HtmCache.getInstance().getNotNull(_file, player);
            final String content2 = HtmCache.getInstance().getNullable(_file, player);
            if (content2 == null) {
                setHtml((have_appends && _file.endsWith(".htm")) ? "" : content);
            } else {
                setHtml(content);
            }
        }
        for (int i = 0; i < _replaces.size(); i += 2) {
            _html = _html.replace(_replaces.get(i), _replaces.get(i + 1));
        }
        if (_html == null) {
            return;
        }
        final Matcher m = objectId.matcher(_html);
        _html = m.replaceAll(String.valueOf(_npcObjId));
        if (player != null) {
            _html = playername.matcher(_html).replaceAll(player.getName());
        }
        client.cleanBypasses(false);
        _html = client.encodeBypasses(_html, false);
    }

    @Override
    protected void writeImpl() {
        if (_html != null) {
            writeC(0xf);
            writeD(_npcObjId);
            writeS(_html);
            writeD(0);
        }
    }
}
