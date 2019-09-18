package ru.j2dev.commons.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class MemoryClassLoader extends ClassLoader {
    private final Map<String, MemoryByteCode> classes = new HashMap<>();
    private final Map<String, MemoryByteCode> loaded = new HashMap<>();

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        MemoryByteCode mbc = classes.get(name);
        if (mbc == null) {
            mbc = classes.get(name);
            if (mbc == null) {
                return super.findClass(name);
            }
        }
        return defineClass(name, mbc.getBytes(), 0, mbc.getBytes().length);
    }

    public void addClass(final MemoryByteCode mbc) {
        final String mbcName = mbc.getName();
        classes.put(mbcName, mbc);
        loaded.put(mbcName, mbc);
    }

    public MemoryByteCode getClass(final String name) {
        return classes.get(name);
    }

    public Stream<String> getLoadedClasses() {
        return loaded.keySet().parallelStream();
    }

    public void clear() {
        loaded.clear();
    }
}
