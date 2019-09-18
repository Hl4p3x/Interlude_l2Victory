package ru.j2dev.gameserver.utils;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.Format.TextMode;
import org.jdom2.output.XMLOutputter;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.AnnouncementsHolder;
import ru.j2dev.gameserver.data.xml.parser.AnnouncementsParser;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.announcement.Announcement;
import ru.j2dev.gameserver.model.announcement.AutoAnnouncement;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Say2;

import java.io.*;
import java.util.Iterator;

public class AnnouncementUtils {

    public static void autoAnnouncementsLaunch() {
        AnnouncementsHolder.getInstance().getAutoAnnouncements().forEach(AutoAnnouncement::start);
    }

    public static void showAnnouncements(final Player activeChar) {
        AnnouncementsHolder.getInstance().getShownOnLoginAnnouncements().forEach(announce -> showAnnounce(announce, activeChar));
    }

    public static void announceToAll(final Announcement announcement) {
        announceToAll(announcement.getMessage(), announcement.isCritical() ? ChatType.CRITICAL_ANNOUNCE : ChatType.ANNOUNCEMENT);
    }

    public static void announceToAll(final String text) {
        announceToAll(text, ChatType.ANNOUNCEMENT);
    }

    public static void announceToPlayer(final String text, Player player) {
        announceToPlayer(player, text, ChatType.ANNOUNCEMENT);
    }

    public static void shout(final Player activeChar, final String text, final ChatType type) {
        final Say2 cs = new Say2(activeChar.getObjectId(), type, activeChar.getName(), text);
        //ChatUtils.shout(activeChar, cs);
        activeChar.sendPacket(cs);
    }

    public static void announceToAll(final String text, final ChatType type) {
        final Say2 cs = new Say2(0, type, "", text);
        GameObjectsStorage.getPlayers().forEach(player -> player.sendPacket(cs));
    }

    public static void announceToAllGM(final String text) {
        final Say2 cs = new Say2(0, ChatType.ANNOUNCEMENT, "", text);
        GameObjectsStorage.getPlayers().stream().filter(Player::isGM).forEach(player -> player.sendPacket(cs));
    }

    public static void announceToPlayer(Player player, final String text, final ChatType type) {
        if (player == null)
            return;

        final Say2 cs = new Say2(0, type, "", text);
        player.sendPacket(cs);
    }

    /*public static void announceToPlayerFromStringHolder(final String add, Player player, final Object... arg) {
        player.sendPacket(new Say2(0, ChatType.ANNOUNCEMENT, "", String.format(StringHolder.getInstance().getString(add, player), arg), null));
    }

    public static void announceToAllFromStringHolder(final String add, final Object... arg) {
        GameObjectsStorage.getPlayers().forEach(player -> player.sendPacket(new Say2(0, ChatType.ANNOUNCEMENT, "", String.format(StringHolder.getInstance().getNotNull(player, add), arg), null)));
    }*/

    public static void announceToAll(final CustomMessage cm) {
        GameObjectsStorage.getPlayers().forEach(player -> player.sendPacket(new Say2(0, ChatType.ANNOUNCEMENT, "", cm.toString())));
    }

    public static void announceToAll(final IStaticPacket sm) {
        GameObjectsStorage.getPlayers().forEach(player -> player.sendPacket(sm));
    }

    public static void showAnnounce(final Announcement announce, final Player player) {
        final Say2 cs = new Say2(0, ChatType.ANNOUNCEMENT, player.getName(), announce.getMessage());
        player.sendPacket(cs);
    }

    public static void addAnnouncement(final String message, final boolean critical, final boolean auto, final int initial_delay, final int delay, final int limit) {
        try {
            final File file_xml = new File(Config.DATAPACK_ROOT, "data/announcements.xml");
            final String fName = file_xml.getAbsolutePath();
            final SAXBuilder parser = new SAXBuilder();
            final Document doc = parser.build(file_xml);
            final Element temp = doc.getRootElement();
            final Element announcement = new Element("announcement");
            announcement.setAttribute("message", message);
            announcement.setAttribute("critical", String.valueOf(critical));
            announcement.setAttribute("auto", delay >= 30 ? String.valueOf(auto) : "false");
            announcement.setAttribute("initial_delay", auto ? (initial_delay > 0 ? String.valueOf(initial_delay) : "-1") : "-1");
            announcement.setAttribute("delay", auto ? (delay >= 30 ? String.valueOf(delay) : "-1") : "-1");
            announcement.setAttribute("limit", auto ? (limit > 0 ? String.valueOf(limit) : "-1") : "-1");
            temp.addContent(announcement);
            XMLOutputter writer;
            Format format = null;
            try {
                format = Format.getRawFormat();
                format.setIndent("\t");
                format.setTextMode(TextMode.TRIM);
                writer = new XMLOutputter();
                writer.setFormat(format);
                writer.output(doc, new OutputStreamWriter(new FileOutputStream(fName), "UTF8"));
                AnnouncementsParser.getInstance().reload();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAnnouncement(final int line) {
        try {
            final File file_xml = new File(Config.DATAPACK_ROOT, "data/announcements.xml");
            final String fName = file_xml.getAbsolutePath();
            final SAXBuilder parser = new SAXBuilder();
            final Document doc = parser.build(file_xml);
            final Iterator<Element> cities = doc.getRootElement().getChildren().iterator();
            int i = -1;
            while (cities.hasNext()) {
                i++;
                cities.next();
                if (i == line) {
                    cities.remove();
                    break;
                }
            }
            XMLOutputter writer;
            Format format;
            try {
                format = Format.getRawFormat();
                format.setIndent("\t");
                format.setTextMode(TextMode.TRIM);
                writer = new XMLOutputter();
                writer.setFormat(format);
                writer.output(doc, new OutputStreamWriter(new FileOutputStream(fName), "UTF8"));
                AnnouncementsParser.getInstance().reload();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }
}
