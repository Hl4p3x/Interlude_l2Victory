package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.OptionDataTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OptionDataHolder extends AbstractHolder {

    private final Map<Integer, OptionDataTemplate> _templates = new HashMap<>();

    public static OptionDataHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addTemplate(final OptionDataTemplate template) {
        _templates.put(template.getId(), template);
    }

    public OptionDataTemplate getTemplate(final int id) {
        return _templates.get(id);
    }

    public int getVariationStatBySkillIdAndLvl(final int skillId, final int skillLvl) {
        final List<Integer> variationSkillList = new ArrayList<>();
        for(OptionDataTemplate template : _templates.values()) {
            if(!template.getSkills().isEmpty()) {
                for(Skill skill : template.getSkills()) {
                    if(skill.getId() == skillId && skill.getLevel() == skillLvl) {
                        variationSkillList.add(template.getId());
                    }
                }
            }
        }
        return Rnd.get(variationSkillList);
    }

    @Override
    public int size() {
        return _templates.size();
    }

    @Override
    public void clear() {
        _templates.clear();
    }

    private static class LazyHolder {
        private static final OptionDataHolder INSTANCE = new OptionDataHolder();
    }
}
