package ru.j2dev.gameserver.ai;

import ru.j2dev.gameserver.model.instances.NpcInstance;

public class Fighter extends DefaultAI {
    public Fighter(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        return super.thinkActive() || defaultThinkBuff(10);
    }

    @Override
    protected boolean createNewTask() {
        return defaultFightTask();
    }

    @Override
    public int getRatePHYS() {
        return 10;
    }

    @Override
    public int getRateDOT() {
        return 8;
    }

    @Override
    public int getRateDEBUFF() {
        return 5;
    }

    @Override
    public int getRateDAM() {
        return 5;
    }

    @Override
    public int getRateSTUN() {
        return 8;
    }

    @Override
    public int getRateBUFF() {
        return 5;
    }

    @Override
    public int getRateHEAL() {
        return 5;
    }
}
