package ru.j2dev.gameserver.templates;

import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.stats.StatTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OptionDataTemplate extends StatTemplate {
    private List<Skill> _skills = Collections.emptyList();
    private final int _id;

    public OptionDataTemplate(final int id) {
        _id = id;
    }

    public void addSkill(final Skill skill) {
        if(_skills.isEmpty()) {
            _skills = new ArrayList<>();
        }
        _skills.add(skill);
    }

    public List<Skill> getSkills() {
        return _skills;
    }

    public int getId() {
        return _id;
    }
}
