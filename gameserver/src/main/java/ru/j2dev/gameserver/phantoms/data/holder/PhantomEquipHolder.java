package ru.j2dev.gameserver.phantoms.data.holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.phantoms.template.PhantomEquipTemplate;
import ru.j2dev.commons.data.xml.AbstractHolder;

import java.util.HashMap;
import java.util.Map;

public class PhantomEquipHolder extends AbstractHolder {
    private static final Logger log = LoggerFactory.getLogger(PhantomEquipHolder.class);
    private static final PhantomEquipHolder instance = new PhantomEquipHolder();
    private final Map<Integer, PhantomEquipTemplate> equip;

    public PhantomEquipHolder() {
        equip = new HashMap<>();
    }

    public static PhantomEquipHolder getInstance() {
        return instance;
    }

    public PhantomEquipTemplate getClassEquip(final int classId) {
        if (equip.containsKey(classId)) {
            return equip.get(classId);
        }
        log.warn("Can't find equipment for class id: " + classId + "! Please check class_equip.xml");
        return null;
    }

    public void addClassEquip(final int classId, final PhantomEquipTemplate template) {
        equip.put(classId, template);
    }

    @Override
    public int size() {
        return equip.size();
    }

    @Override
    public void clear() {
        equip.clear();
    }

}
