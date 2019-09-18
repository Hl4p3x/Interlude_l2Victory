package ru.j2dev.commons.file;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by JunkyFunky
 * on 06.10.2016.
 * group j2dev
 */
public class SQLFilter implements FilenameFilter {
    public SQLFilter() {
    }

    @Override
    public boolean accept(final File dir, final String name) {
        return name.endsWith(".sql");
    }
}
