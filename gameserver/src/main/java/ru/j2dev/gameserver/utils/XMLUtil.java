package ru.j2dev.gameserver.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class XMLUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLUtil.class);

    public static String getAttributeValue(final Node n, final String item) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return "";
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return "";
        }
        return val;
    }

    public static boolean getAttributeBooleanValue(final Node n, final String item, final boolean dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }
        return Boolean.parseBoolean(val);
    }

    public static int getAttributeIntValue(final Node n, final String item, final int dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }
        return Integer.parseInt(val);
    }

    public static long getAttributeLongValue(final Node n, final String item, final long dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }
        return Long.parseLong(val);
    }
}
