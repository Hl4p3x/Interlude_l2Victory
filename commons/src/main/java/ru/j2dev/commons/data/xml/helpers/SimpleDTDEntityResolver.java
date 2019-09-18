package ru.j2dev.commons.data.xml.helpers;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.File;

/**
 * Author: VISTALL
 * Date:  20:44/30.11.2010
 */
public class SimpleDTDEntityResolver implements EntityResolver {
    private final String _fileName;

    public SimpleDTDEntityResolver(final File f) {
        _fileName = f.getAbsolutePath();
    }

    @Override
    public InputSource resolveEntity(final String publicId, final String systemId) {
        return new InputSource(_fileName);
    }
}
