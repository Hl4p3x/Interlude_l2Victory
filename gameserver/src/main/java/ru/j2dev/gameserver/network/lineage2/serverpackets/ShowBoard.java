package ru.j2dev.gameserver.network.lineage2.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.model.Player;

import java.nio.charset.Charset;
import java.util.List;

public class ShowBoard extends L2GameServerPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowBoard.class);
    private static final Charset BBS_CHARSET = Charset.forName("UTF-16LE");
    private static final String[] DIRECT_BYPASS = {"bypass _bbshome", "bypass _bbsgetfav", "bypass _bbsloc", "bypass _bbsclan", "bypass _bbsmemo", "bypass _maillist_0_1_0_", "bypass _friendlist_0_"};
    public static L2GameServerPacket CLOSE = new ShowBoard();

    private String _htmlCode;
    private String _id;
    private List<String> _arg;
    private String _addFav;
    private int _showBoard = 1; // 1 show, 0 hide

    private ShowBoard(final String htmlCode, final String id, final Player player, final boolean encodeBypasses) {
        _addFav = "";
        if (htmlCode != null && htmlCode.length() > 8192) {
            LOGGER.warn("Html '" + htmlCode + "' is too long! this will crash the client!");
            _htmlCode = "<html><body>Html was too long</body></html>";
            return;
        }
        _id = id;
        if (player.getSessionVar("add_fav") != null) {
            _addFav = "bypass _bbsaddfav_List";
        }
        if (htmlCode != null) {
            if (encodeBypasses) {
                _htmlCode = player.getNetConnection().encodeBypasses(htmlCode, true);
            } else {
                _htmlCode = htmlCode;
            }
        } else {
            _htmlCode = null;
        }
    }

    public ShowBoard(final String htmlCode, final String id, final Player player) {
        this(htmlCode, id, player, true);
    }

    public ShowBoard(final List<String> arg) {
        _addFav = "";
        _id = "1002";
        _htmlCode = null;
        _arg = arg;
    }

    /**
     * Hides the community board
     */
    public ShowBoard() {
        _showBoard = 0;
        _htmlCode = null;
    }

    public static void separateAndSend(String html, final Player player) {
        html = player.getNetConnection().encodeBypasses(html, true);
        final byte[] htmlBytes = html.getBytes(BBS_CHARSET);
        if (htmlBytes.length < 8180) {
            player.sendPacket(new ShowBoard(html, "101", player, false));
            player.sendPacket(new ShowBoard(null, "102", player, false));
            player.sendPacket(new ShowBoard(null, "103", player, false));
        } else if (htmlBytes.length < 8180 * 2) {
            player.sendPacket(new ShowBoard(new String(htmlBytes, 0, 8180, BBS_CHARSET), "101", player, false));
            player.sendPacket(new ShowBoard(new String(htmlBytes, 8180, htmlBytes.length - 8180, BBS_CHARSET), "102", player, false));
            player.sendPacket(new ShowBoard(null, "103", player, false));
        } else if (html.length() < 8180 * 3) {
            player.sendPacket(new ShowBoard(new String(htmlBytes, 0, 8180, BBS_CHARSET), "101", player, false));
            player.sendPacket(new ShowBoard(new String(htmlBytes, 8180, htmlBytes.length - 8180, BBS_CHARSET), "102", player, false));
            player.sendPacket(new ShowBoard(new String(htmlBytes, 16360, htmlBytes.length - 16360, BBS_CHARSET), "103", player, false));
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0x6e);
        writeC(_showBoard);
        if(_showBoard > 0) {
            for (final String bbsBypass : DIRECT_BYPASS) {
                writeS(bbsBypass);
            }
            writeS(_addFav);
            StringBuilder str = new StringBuilder(_id + "\b");
            if (!"1002".equals(_id)) {
                if (_htmlCode != null) {
                    str.append(_htmlCode);
                }
            } else {
                _arg.forEach(arg -> str.append(arg).append(" \b"));
            }
            writeS(str.toString());
        }
    }
}
