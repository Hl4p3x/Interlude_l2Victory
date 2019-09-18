package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.factory.IObjectFactory;
import ru.j2dev.dataparser.holder.areadata.AreaType;
import ru.j2dev.dataparser.holder.areadata.area.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : Camelion
 * @date : 24.08.12 23:23
 */
public class AreaDataHolder extends AbstractHolder {
    private static final Pattern zoneTypePattern = Pattern.compile("type\\s*?=\\s*?(\\S+)");
    private static final AreaDataHolder ourInstance = new AreaDataHolder();
    @Element(start = "default_setting_begin", end = "default_setting_end", objectFactory = DefaultSettingFactory.class)
    private List<DefaultArea> defaultSettings;
    @Element(start = "area_begin", end = "area_end", objectFactory = AreaFactory.class)
    private List<DefaultArea> areas;
    private final Map<AreaType, DefaultArea> defaultSettingsMap = new HashMap<>();

    private AreaDataHolder() {
    }

    public static AreaDataHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return areas.size();
    }

    public List<DefaultArea> getAreas() {
        return areas;
    }

    public DefaultArea getDefaultSettingFor(AreaType type) {
        if (defaultSettings.size() != defaultSettingsMap.size()) { // Init map
            for (DefaultArea area : defaultSettings)
                if (!defaultSettingsMap.containsKey(area.type))
                    defaultSettingsMap.put(area.type, area);
        }
        return defaultSettingsMap.get(type);
    }

    @Override
    public void clear() {
        areas.clear();
    }

    public static class DefaultSettingFactory implements IObjectFactory<DefaultArea> {
        @Override
        public DefaultArea createObjectFor(StringBuilder data) {
            Matcher matcher = zoneTypePattern.matcher(data);
            if (matcher.find()) {
                AreaType type = AreaType.valueOf(matcher.group(1));
                switch (type) {
                    case water:
                        return new WaterArea();
                    case mother_tree:
                        return new MotherTreeZone();
                    case peace_zone:
                        return new PeaceZone();
                    case battle_zone:
                        return new BattleZone();
                    case no_restart:
                        return new NoRestartZone();
                    case damage:
                        return new DamageZone();
                    case swamp:
                        return new SwampZone();
                    case poison:
                        return new PoisonZone();
                    case instant_skill:
                        return new InstantSkillZone();
                    case teleport:
                        return new TeleportZone();
                    case ssq_zone:
                        return new SSQZone();
                }
            }
            return new DefaultArea();
        }

        @Override
        public void setFieldClass(Class<?> clazz) {
        }
    }

    public static class AreaFactory implements IObjectFactory<DefaultArea> {
        @Override
        public DefaultArea createObjectFor(StringBuilder data) {
            Matcher matcher = zoneTypePattern.matcher(data);
            if (matcher.find()) {
                AreaType type = AreaType.valueOf(matcher.group(1));
                DefaultArea defaultSetting = getInstance().getDefaultSettingFor(type);
                switch (type) {
                    case water:
                        return defaultSetting == null ? new WaterArea() : new WaterArea(defaultSetting);
                    case mother_tree:
                        return defaultSetting == null ? new MotherTreeZone() : new MotherTreeZone(defaultSetting);
                    case peace_zone:
                        return defaultSetting == null ? new PeaceZone() : new PeaceZone(defaultSetting);
                    case battle_zone:
                        return defaultSetting == null ? new BattleZone() : new BattleZone(defaultSetting);
                    case no_restart:
                        return defaultSetting == null ? new NoRestartZone() : new NoRestartZone(defaultSetting);
                    case damage:
                        return defaultSetting == null ? new DamageZone() : new DamageZone(defaultSetting);
                    case swamp:
                        return defaultSetting == null ? new SwampZone() : new SwampZone(defaultSetting);
                    case poison:
                        return defaultSetting == null ? new PoisonZone() : new PoisonZone(defaultSetting);
                    case instant_skill:
                        return defaultSetting == null ? new InstantSkillZone() : new InstantSkillZone(defaultSetting);
                    case teleport:
                        return defaultSetting == null ? new TeleportZone() : new TeleportZone(defaultSetting);
                    case ssq_zone:
                        return defaultSetting == null ? new SSQZone() : new SSQZone(defaultSetting);
                }
                throw new ExceptionInInitializerError("Class for type " + type + " not found");
            }
            return null;
        }

        @Override
        public void setFieldClass(Class<?> clazz) {
            // Ignored
        }
    }
}