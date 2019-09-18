package ru.j2dev.commons.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

public class MemoryByteCode extends SimpleJavaFileObject {
    private final String className;
    private ByteArrayOutputStream oStream;

    public MemoryByteCode(final String className, final URI uri) {
        super(uri, Kind.CLASS);
        this.className = className;
    }

    @Override
    public OutputStream openOutputStream() {
        oStream = new ByteArrayOutputStream(3 * 1024);
        return oStream;
    }

    public byte[] getBytes() {
        return oStream.toByteArray();
    }

    @Override
    public String getName() {
        return className;
    }
}
