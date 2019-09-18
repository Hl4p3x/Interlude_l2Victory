package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.announce_sphere.AnnounceArea;

import java.util.List;

/**
 * @author : Camelion
 * @date : 24.08.12 21:27
 */
public class AnnounceSphereHolder extends AbstractHolder {
    private static final AnnounceSphereHolder ourInstance = new AnnounceSphereHolder();
    @Element(start = "announce_area_begin", end = "announce_area_end")
    private List<AnnounceArea> announceAreas;

    private AnnounceSphereHolder() {
    }

    public static AnnounceSphereHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return announceAreas.size();
    }

    public List<AnnounceArea> getAnnounceAreas() {
        return announceAreas;
    }

    @Override
    public void clear() {
        announceAreas.clear();
    }
}