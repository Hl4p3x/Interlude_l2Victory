package handler.admincommands;

import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

public class AdminTeleportBookmark extends ScriptAdminCommand {
    private static final int BOOKBARKS_PER_PAGE = 12;
    private static final String BOOKBARKS_VAR_PREFIX = "gmbk_";
    private static final Comparator<Pair<String, Location>> BOOKMARKS_SORT_COMPARATOR = Comparator.comparing(Pair::getLeft);

    @Override
    public boolean useAdminCommand(final Enum comm, final String[] wordList, final String fullString, final Player player) {
        final Commands c = (Commands) comm;
        if (!player.getPlayerAccess().CanUseGMCommand) {
            return false;
        }
        switch (c) {
            case admin_bkpage: {
                final int page = (wordList.length > 1) ? Integer.parseInt(wordList[1]) : 1;
                final NpcHtmlMessage reply = new NpcHtmlMessage(5);
                reply.setHtml(formatBookmarkListHtml(getBookmarksList(player), page));
                player.sendPacket(reply);
                return true;
            }
            case admin_bkgo: {
                if (wordList.length > 1) {
                    final String name = wordList[1].replace("<", "&lt;").replace(">", "&gt;");
                    final String xyz = player.getVar(AdminTeleportBookmark.BOOKBARKS_VAR_PREFIX + name);
                    if (xyz != null) {
                        player.teleToLocation(Location.parseLoc(xyz));
                    }
                }
                return true;
            }
            case admin_bk: {
                if (wordList.length > 1) {
                    final String name = wordList[1].replace("<", "&lt;").replace(">", "&gt;");
                    player.setVar(AdminTeleportBookmark.BOOKBARKS_VAR_PREFIX + name, player.getLoc().toXYZString(), -1L);
                }
                final NpcHtmlMessage reply2 = new NpcHtmlMessage(5);
                reply2.setHtml(formatBookmarkListHtml(getBookmarksList(player), 1));
                player.sendPacket(reply2);
                return true;
            }
            case admin_bkdel: {
                if (wordList.length > 1) {
                    final String name = wordList[1].replace("<", "&lt;").replace(">", "&gt;");
                    player.unsetVar(AdminTeleportBookmark.BOOKBARKS_VAR_PREFIX + name);
                }
                final NpcHtmlMessage reply2 = new NpcHtmlMessage(5);
                reply2.setHtml(formatBookmarkListHtml(getBookmarksList(player), 1));
                player.sendPacket(reply2);
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private List<Pair<String, Location>> getBookmarksList(final Player player) {
        final List<Pair<String, Location>> result = new ArrayList<>();
        final MultiValueSet<String> userVars = player.getVars();
        for (final Entry<String, Object> e : userVars.entrySet()) {
            if (e != null && e.getKey() != null) {
                if (!e.getKey().startsWith(AdminTeleportBookmark.BOOKBARKS_VAR_PREFIX)) {
                    continue;
                }
                result.add(Pair.of(e.getKey().substring(AdminTeleportBookmark.BOOKBARKS_VAR_PREFIX.length()), Location.parseLoc(e.getValue().toString())));
            }
        }
        result.sort(AdminTeleportBookmark.BOOKMARKS_SORT_COMPARATOR);
        return result;
    }

    private String formatBookmarkListHtml(final List<Pair<String, Location>> bookmarksList, int page) {
        final StringBuilder htmlText = new StringBuilder();
        htmlText.append("<html><body>");
        htmlText.append("<center><br>Bookmark list<br>");
        htmlText.append("<table width=270 border=0>");
        final int bkMinIdx = Math.max(0, (page - 1) * AdminTeleportBookmark.BOOKBARKS_PER_PAGE);
        for (int bkMaxIdx = Math.min(bookmarksList.size(), page * AdminTeleportBookmark.BOOKBARKS_PER_PAGE), bkIdx = bkMinIdx; bkIdx < bkMaxIdx; ++bkIdx) {
            final Pair<String, Location> bk = bookmarksList.get(bkIdx);
            htmlText.append("<tr>");
            htmlText.append("<td> ");
            htmlText.append("<a action=\"bypass admin_bkgo ").append(bk.getKey()).append("\"> [");
            htmlText.append(bk.getKey()).append(", ").append(bk.getValue().toXYZString());
            htmlText.append("] </a> </td><td> ");
            htmlText.append("<a action=\"bypass admin_bkdel ").append(bk.getKey()).append("\">del");
            htmlText.append(" </td>");
            htmlText.append(" </tr>");
        }
        htmlText.append("<tr><td align=center>");
        htmlText.append("<table border=0 cellspacing=2 cellpadding=2><tr>");
        page = Math.max(1, page);
        if (page > 1) {
            htmlText.append("<td align=right> ").append("<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">".replace("%prev_bypass%", "admin_bkpage " + (page - 1))).append(" </td>");
        }
        htmlText.append("<td> ").append(page).append(" </td>");
        if (page <= bookmarksList.size() / AdminTeleportBookmark.BOOKBARKS_PER_PAGE) {
            htmlText.append("<td> ").append("<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">".replace("%next_bypass%", "admin_bkpage " + (page + 1))).append(" </td>");
        }
        htmlText.append("</tr></table>");
        htmlText.append("</td><td></td></tr>");
        htmlText.append("</table>");
        htmlText.append("</center>");
        htmlText.append("</body></html>");
        return htmlText.toString();
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    enum Commands {
        admin_bk,
        admin_bkgo,
        admin_bkdel,
        admin_bkpage
    }
}
