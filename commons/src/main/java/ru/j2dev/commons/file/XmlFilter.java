package ru.j2dev.commons.file;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by JunkyFunky
 * on 07.10.2016.
 * group j2dev
 */
public class XmlFilter implements FilenameFilter {
    public XmlFilter() {
    }

    @Override
    public boolean accept(final File dir, final String name) {
        return name.endsWith(".xml");
    }
}