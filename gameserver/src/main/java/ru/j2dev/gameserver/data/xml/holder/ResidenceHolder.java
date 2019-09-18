package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.residence.Residence;

import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public final class ResidenceHolder extends AbstractHolder {

    private final Map<Integer, Residence> _residences = new TreeMap();
    private final Map<Class, List<Residence>> _fastResidencesByType = new HashMap<>(4);

    public static ResidenceHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addResidence(final Residence r) {
        _residences.put(r.getId(), r);
    }

    public <R extends Residence> R getResidence(final int id) {
        return (R) _residences.get(id);
    }

    public <R extends Residence> R getResidence(final Class<R> type, final int id) {
        final Residence r = getResidence(id);
        if (r == null || r.getClass() != type) {
            return null;
        }
        return (R) r;
    }

    public <R extends Residence> List<R> getResidenceList(final Class<R> t) {
        return (List<R>) _fastResidencesByType.get(t);
    }

    public Collection<Residence> getResidences() {
        return _residences.values();
    }

    public <R extends Residence> R getResidenceByObject(final Class<? extends Residence> type, final GameObject object) {
        return (R) getResidenceByCoord(type, object.getX(), object.getY(), object.getZ(), object.getReflection());
    }

    public <R extends Residence> R getResidenceByCoord(final Class<R> type, final int x, final int y, final int z, final Reflection ref) {
        final Collection<Residence> residences = (Collection<Residence>) ((type == null) ? getResidences() : getResidenceList(type));
        for (final Residence residence : residences) {
            if (residence.checkIfInZone(x, y, z, ref)) {
                return (R) residence;
            }
        }
        return null;
    }

    public <R extends Residence> R findNearestResidence(final Class<R> clazz, final int x, final int y, final int z, final Reflection ref, final int offset) {
        Residence residence = getResidenceByCoord(clazz, x, y, z, ref);
        if (residence == null) {
            double closestDistance = offset;
            for (final Residence r : getResidenceList(clazz)) {
                final double distance = r.getZone().findDistanceToZone(x, y, z, false);
                if (closestDistance > distance) {
                    closestDistance = distance;
                    residence = r;
                }
            }
        }
        return (R) residence;
    }

    public void callInit() {
        for (final Residence r : getResidences()) {
            r.init();
        }
    }

    private void buildFastLook() {
        for (final Residence residence : _residences.values()) {
            List<Residence> list = _fastResidencesByType.computeIfAbsent(residence.getClass(), k -> new ArrayList<>());
            list.add(residence);
        }
    }

    @Override
    public void log() {
        buildFastLook();
        info("total size: " + _residences.size());
        for (final Entry<Class, List<Residence>> entry : _fastResidencesByType.entrySet()) {
            info(" - load " + entry.getValue().size() + " " + entry.getKey().getSimpleName().toLowerCase() + "(s).");
        }
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {
        _residences.clear();
        _fastResidencesByType.clear();
    }

    private static class LazyHolder {
        private static final ResidenceHolder INSTANCE = new ResidenceHolder();
    }
}
