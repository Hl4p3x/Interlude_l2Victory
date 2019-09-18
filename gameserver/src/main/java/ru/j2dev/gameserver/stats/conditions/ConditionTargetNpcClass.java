package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Scripts;
import ru.j2dev.gameserver.stats.Env;

public class ConditionTargetNpcClass extends Condition {
    private final Class<NpcInstance> _npcClass;

    @SuppressWarnings("unchecked")
    public ConditionTargetNpcClass(final String name) {
        Class<NpcInstance> classType;
        try {
            classType = (Class<NpcInstance>) Class.forName("ru.j2dev.gameserver.model.instances." + name + "Instance");
        } catch (ClassNotFoundException e) {
            classType = (Class<NpcInstance>) Scripts.getInstance().getClasses().get("npc.model." + name + "Instance");
        }
        if (classType == null) {
            throw new IllegalArgumentException("Not found type class for type: " + name + ".");
        }
        _npcClass = classType;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.target != null && env.target.getClass() == _npcClass;
    }
}
