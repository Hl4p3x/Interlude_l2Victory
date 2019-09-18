package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.AnnouncementsHolder;
import ru.j2dev.gameserver.model.announcement.Announcement;

import java.io.File;

/**
 * @author Java-man
 */
public class AnnouncementsParser extends AbstractFileParser<AnnouncementsHolder> {
    private static final AnnouncementsParser INSTANCE = new AnnouncementsParser();

    protected AnnouncementsParser() {
        super(AnnouncementsHolder.getInstance());
    }

    public static AnnouncementsParser getInstance() {
        return INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/announcements.xml");
    }

    @Override
    protected void readData(final AnnouncementsHolder holder, final Element rootElement) {
        int initialDelay, delay, limit;
        String message;
        boolean critical, auto;

        for (final Element announcementElement : rootElement.getChildren("announcement")) {
            message = announcementElement.getAttributeValue("message");
            critical = Boolean.parseBoolean(announcementElement.getAttributeValue("critical"));
            auto = Boolean.parseBoolean(announcementElement.getAttributeValue("auto"));
            initialDelay = Integer.parseInt(announcementElement.getAttributeValue("initial_delay"));
            delay = Integer.parseInt(announcementElement.getAttributeValue("delay"));
            limit = Integer.parseInt(announcementElement.getAttributeValue("limit"));
            if (auto && limit < -1) {
                limit = -1;
            }
            if (auto && delay < 30) {
                warn("Announcement " + message + " is using unrealistic delay " + delay + ". Ignoring it!");
                continue;
            }
            if (message == null) {
                warn("Announcement is empty. Ignoring it!");
                continue;
            }

            if (auto) {
                holder.addAutoAnnouncement(new Announcement(message, critical, true, initialDelay, delay, limit));
            } else {
                holder.addShownOnLoginAnnouncement(new Announcement(message, critical, false, initialDelay, delay, limit));
            }
            holder.addFullAnnouncement(new Announcement(message, critical, auto, initialDelay, delay, limit));
        }
    }
}