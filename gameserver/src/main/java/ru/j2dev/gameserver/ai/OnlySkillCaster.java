package ru.j2dev.gameserver.ai;

import ru.j2dev.gameserver.model.instances.NpcInstance;

/**
 * Solution
 * 24.08.2018
 * 23:49
 */
    public class OnlySkillCaster extends DefaultAI {
    public OnlySkillCaster(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        return super.thinkActive();
    }

    @Override
    protected boolean createNewTask() {
        return defaultMagicTask();
    }

    @Override
    public int getRateDAM() {
        return 100;
    }

}
