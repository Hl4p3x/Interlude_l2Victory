package ru.j2dev.commons.data.xml;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import ru.j2dev.commons.data.xml.helpers.FileComparator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;

/**
 * @author VISTALL
 * @author Java-man
 */
public abstract class AbstractDirParser<H extends AbstractHolder> extends AbstractParser<H> {
    protected AbstractDirParser(final H holder) {
        super(holder);
    }

    public abstract File getXMLDir();

    public abstract boolean isIgnored(File f);

    @Override
    protected final void parse() {
        final File dir = getXMLDir();

        if (!dir.exists()) {
            warn("Dir " + dir.getAbsolutePath() + " not exists");
            return;
        }

        final Collection<File> files = FileUtils.listFiles(dir, FileFilterUtils.suffixFileFilter(".xml"), FileFilterUtils.directoryFileFilter());
        // сортируем [index].*.xml файлы
        final ImmutableList<File> sortedFiles = FileComparator.instance().immutableSortedCopy(files);

        for (final File file : sortedFiles) {
            if (!file.isHidden() && !isIgnored(file)) {
                try (InputStream inputStream = new FileInputStream(file); InputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
                    parseDocument(bufferedInputStream, file.getName());
                } catch (Exception e) {
                    error("Can't parse file: " + file.getName(), e);
                }
            }
        }
    }
}
