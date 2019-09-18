package ru.j2dev.gameserver.manager;

import ru.j2dev.gameserver.handler.bbs.CommunityBoardManager;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.utils.Log;

import java.util.Arrays;
import java.util.List;

public class BypassManager {
    private static final String[] SIMBLE_BEGININGS = {"_mrsl", "_diary", "_match", "manor_menu_select", "_match", "_olympiad"};
    private static final String[] SIMBLE_BBS_BEGININGS = {"_bbshome", "_bbsgetfav", "_bbslink", "_bbsloc", "_bbsclan", "_bbsmemo", "_maillist_0_1_0_", "_friendlist_0_", "_bbsaddfav"};

    private static boolean isSimpleBypass(final String bypass, final boolean bbs) {
        final String[] beginings = (bbs ? SIMBLE_BBS_BEGININGS : SIMBLE_BEGININGS);
        return Arrays.stream(beginings).anyMatch(bypass::startsWith);
    }

    public static BypassType getBypassType(final String bypass) {
        switch (bypass.charAt(0)) {
            case '0': {
                return BypassType.ENCODED;
            }
            case '1': {
                return BypassType.ENCODED_BBS;
            }
            default: {
                if (isSimpleBypass(bypass, false)) {
                    return BypassType.SIMPLE;
                }
                if (isSimpleBypass(bypass, true) && CommunityBoardManager.getInstance().getCommunityHandler(bypass) != null) {
                    return BypassType.SIMPLE_BBS;
                }
                return BypassType.SIMPLE_DIRECT;
            }
        }
    }

    public static String encode(final String html_, final List<String> bypassStorage, final boolean bbs) {
        final StringBuilder sb = new StringBuilder();
        final char[] html = html_.toCharArray();
        int nextAppendIdx = 0;
        for (int i = 0; i + 7 < html.length; ++i) {
            int bypassPos = 0;
            int bypassLen = 0;
            if (html[i] == '\"' && Character.toLowerCase(html[i + 1]) == 'b' && Character.toLowerCase(html[i + 2]) == 'y' && Character.toLowerCase(html[i + 3]) == 'p' && Character.toLowerCase(html[i + 4]) == 'a' && Character.toLowerCase(html[i + 5]) == 's' && Character.toLowerCase(html[i + 6]) == 's' && Character.isWhitespace(html[i + 7])) {
                int len;
                for (len = 8; len + i < html.length && html[len + i] != '\"'; ++len) {
                }
                if (len + i == html.length) {
                    bypassPos = 0;
                    bypassLen = 0;
                } else {
                    bypassPos = i + 1;
                    bypassLen = len - 1;
                }
            }
            if (bypassLen > 0) {
                int j;
                for (j = 7; j < bypassLen && Character.isWhitespace(html[bypassPos + j]); ++j) {
                }
                final boolean haveMinusH = html[bypassPos + j] == '-' && (html[bypassPos + j + 1] == 'h' || html[bypassPos + j + 1] == 'H');
                if (haveMinusH) {
                    for (j += 2; j < bypassLen && (html[bypassPos + j] == ' ' || html[bypassPos + j] == '\t'); ++j) {
                    }
                }
                String code;
                final String bypass = code = new String(html, bypassPos + j, bypassLen - j);
                String params = "";
                final int k = bypass.indexOf(" $");
                final boolean use_params = k >= 0;
                if (use_params) {
                    code = bypass.substring(0, k);
                    params = bypass.substring(k);
                }
                sb.append(html, nextAppendIdx, bypassPos - nextAppendIdx);
                nextAppendIdx = bypassPos + bypassLen;
                sb.append("bypass ");
                if (haveMinusH) {
                    sb.append("-h ");
                }
                sb.append(bbs ? '1' : '0');
                synchronized (bypassStorage) {
                    sb.append(Integer.toHexString(bypassStorage.size()));
                    sb.append(params);
                    bypassStorage.add(code);
                }
            }
        }
        sb.append(html, nextAppendIdx, html.length - nextAppendIdx);
        return sb.toString();
    }

    public static DecodedBypass decode(final String bypass, final List<String> bypassStorage, final boolean bbs, final GameClient client) {
        synchronized (bypassStorage) {
            final String[] bypass_parsed = bypass.split(" ");
            final int idx = Integer.parseInt(bypass_parsed[0].substring(1), 16);
            String bp;
            try {
                bp = bypassStorage.get(idx);
            } catch (Exception e) {
                bp = null;
            }
            if (bp == null) {
                Log.add("Can't decode bypass (bypass not exists): " + (bbs ? "[bbs] " : "") + bypass + " / Client: " + client + " / Npc: " + ((client.getActiveChar() == null || client.getActiveChar().getLastNpc() == null) ? "null" : client.getActiveChar().getLastNpc().getName()), "debug_bypass");
                return null;
            }
            DecodedBypass result;
            result = new DecodedBypass(bp, bbs);
            for (int i = 1; i < bypass_parsed.length; ++i) {
                final StringBuilder sb = new StringBuilder();
                result.bypass = sb.append(result.bypass).append(" ").append(bypass_parsed[i]).toString();
            }
            result.trim();
            return result;
        }
    }

    public enum BypassType {
        ENCODED,
        ENCODED_BBS,
        SIMPLE,
        SIMPLE_BBS,
        SIMPLE_DIRECT
    }

    public static class DecodedBypass {
        public final boolean bbs;
        public String bypass;

        public DecodedBypass(final String _bypass, final boolean _bbs) {
            bypass = _bypass;
            bbs = _bbs;
        }

        public DecodedBypass trim() {
            bypass = bypass.trim();
            return this;
        }
    }
}
