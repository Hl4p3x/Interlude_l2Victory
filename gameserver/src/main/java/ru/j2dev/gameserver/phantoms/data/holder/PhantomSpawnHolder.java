package ru.j2dev.gameserver.phantoms.data.holder;

import ru.j2dev.gameserver.phantoms.template.PhantomSpawnTemplate;
import ru.j2dev.commons.data.xml.AbstractHolder;

import java.util.ArrayList;
import java.util.List;

public class PhantomSpawnHolder extends AbstractHolder {
    private static final PhantomSpawnHolder instance = new PhantomSpawnHolder();

    private List<PhantomSpawnTemplate> spawns;

    public PhantomSpawnHolder() {
        spawns = new ArrayList<>();
    }

    public static PhantomSpawnHolder getInstance() {
        return PhantomSpawnHolder.instance;
    }

    public void addSpawn(final PhantomSpawnTemplate template) {
        spawns.add(template);
    }

    public List<PhantomSpawnTemplate> getSpawns() {
        return spawns;
    }

    @Override
    public int size() {
        return spawns.size();
    }

    @Override
    public void clear() {
        spawns.clear();
    }
}
