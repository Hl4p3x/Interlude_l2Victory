package ru.j2dev.gameserver.utils;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.NpcString;
import ru.j2dev.gameserver.network.lineage2.components.SysString;

import java.util.regex.Pattern;

public class HtmlUtils {
    public static final String PREV_BUTTON = "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">";
    public static final String NEXT_BUTTON = "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">";


    private static final Pattern PATTERN_1 = Pattern.compile("(\\s|\"|\'|\\(|^|\n)\\*(.*?)\\*(\\s|\"|\'|\\)|\\?|\\.|!|:|;|,|$|\n)");
    private static final Pattern PATTERN_2 = Pattern.compile("(\\s|\"|\'|\\(|^|\n)\\$(.*?)\\$(\\s|\"|\'|\\)|\\?|\\.|!|:|;|,|$|\n)");
    private static final Pattern PATTERN_3 = Pattern.compile("%%\\s*\n");
    private static final Pattern PATTERN_4 = Pattern.compile("\n\n+");
    private static final Pattern PATTERN_5 = Pattern.compile(" @");

    public static String htmlResidenceName(final int id) {
        return "&%" + id + ";";
    }

    public static String htmlNpcName(final int npcId) {
        return "&@" + npcId + ";";
    }

    public static String htmlSysString(final SysString sysString) {
        return htmlSysString(sysString.getId());
    }

    public static String htmlSysString(final int id) {
        return "&$" + id + ";";
    }

    public static String htmlItemName(final int itemId) {
        return "&#" + itemId + ";";
    }

    public static String htmlNpcString(final NpcString id, final Object... params) {
        return htmlNpcString(id.getId(), params);
    }

    public static String htmlNpcString(final int id, final Object... params) {
        StringBuilder replace = new StringBuilder("<fstring");
        if (params.length > 0) {
            for (int i = 0; i < params.length; ++i) {
                replace.append(" p").append(i + 1).append("=\"").append(params[i]).append("\"");
            }
        }
        replace.append(">").append(id).append("</fstring>");
        return replace.toString();
    }

    public static String makeClassNameFString(final Player player, final int classId) {
        return PtsUtils.MakeFString(player, 1811000 + classId, "");
    }

    public static String htmlButton(final String value, final String action, final int width) {
        return htmlButton(value, action, width, 22);
    }

    public static String htmlButton(final String value, final String action, final int width, final int height) {
        return String.format("<button value=\"%s\" action=\"%s\" back=\"L2UI_CH3.bigbutton2_down\" width=%d height=%d fore=\"L2UI_CH3.bigbutton2\">", value, action, width, height);
    }

    public static String bbParse(String s) {
        if (s == null) {
            return null;
        }

        s = s.replace("\r", "");
        s = PATTERN_1.matcher(s).replaceAll("$1<font color=\"LEVEL\">$2</font>$3"); // *S1*
        s = PATTERN_2.matcher(s).replaceAll("$1<font color=\"00FFFF\">$2</font>$3");// $S1$
        s = Strings.replace(s, "^!(.*?)$", Pattern.MULTILINE, "<font color=\"FFFFFF\">$1</font>\n\n");
        s = PATTERN_3.matcher(s).replaceAll("<br1>");
        s = PATTERN_4.matcher(s).replaceAll("<br>");
        s = Strings.replace(s, "\\[([^\\]\\|]*?)\\|([^\\]]*?)\\]", Pattern.DOTALL, "<a action=\"bypass -h $1\">$2</a>");
        s = PATTERN_5.matcher(s).replaceAll("\" msg=\"");

        return s;
    }

    public static String compress(final Language lang, final HtmlCompressor compressor, final String content) {
        return compressor.compress(content);
    }
}
