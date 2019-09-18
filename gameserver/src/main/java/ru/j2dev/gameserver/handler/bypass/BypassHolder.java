package ru.j2dev.gameserver.handler.bypass;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.commons.data.xml.AbstractHolder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @date 15:51/12.07.2011
 */
public class BypassHolder extends AbstractHolder {
    private static final BypassHolder _instance = new BypassHolder();
    private final Map<String, Pair<Object, Method>> _bypasses = new HashMap<>();

    public static BypassHolder getInstance() {
        return _instance;
    }

    public void registerBypass(String bypass, Object o, Method method) {
        Pair<Object, Method> old = _bypasses.put(bypass, new ImmutablePair<>(o, method));
        if (old != null) {
            warn("Duplicate bypass: " + bypass + " old: (" + old.getKey().getClass().getName() + ':' + old.getRight().getName() + "), new: (" + o.getClass().getName() + ':' + method.getName() + ')');
        }
    }

    public Pair<Object, Method> getBypass(String name) {
        return _bypasses.get(name);
    }

    @Override
    public int size() {
        return _bypasses.size();
    }

    @Override
    public void clear() {
        _bypasses.clear();
    }
}
