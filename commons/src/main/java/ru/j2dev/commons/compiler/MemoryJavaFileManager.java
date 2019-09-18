package ru.j2dev.commons.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import java.net.URI;

public class MemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
    private final MemoryClassLoader cl;

    public MemoryJavaFileManager(final StandardJavaFileManager sjfm, final MemoryClassLoader xcl) {
        super(sjfm);
        cl = xcl;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(final Location location, final String className, final Kind kind,
                                               final FileObject sibling) {
        final MemoryByteCode mbc = new MemoryByteCode(className.replace('/', '.').replace('\\', '.'),
                URI.create("file:///" + className.replace('.', '/').replace('\\', '/') + kind.extension));
        cl.addClass(mbc);

        return mbc;
    }

    @Override
    public ClassLoader getClassLoader(final Location location) {
        return cl;
    }
}
