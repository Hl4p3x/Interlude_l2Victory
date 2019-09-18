package ru.j2dev.gameserver.ai;

import ru.j2dev.gameserver.model.instances.NpcInstance;

public class Priest extends DefaultAI {
    public Priest(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        return super.thinkActive() || defaultThinkBuff(10, 5);
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
        return 15;
    }

    @Override
    public int getRateDEBUFF() {
        return 15;
    }

    @Override
    public int getRateDAM() {
        return 30;
    }

    @Override
    public int getRateSTUN() {
        return 3;
    }

    @Override
    public int getRateBUFF() {
        return 10;
    }

    @Override
    public int getRateHEAL() {
        return 40;
    }
}
