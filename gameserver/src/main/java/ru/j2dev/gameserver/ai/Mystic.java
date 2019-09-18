package ru.j2dev.gameserver.ai;

import ru.j2dev.gameserver.model.instances.NpcInstance;

public class Mystic extends DefaultAI {
    public Mystic(final NpcInstance actor) {
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
        return (_damSkills.length == 0) ? 25 : 0;
    }

    @Override
    public int getRateDOT() {
        return 10;
    }

    @Override
    public int getRateDEBUFF() {
        return 5;
    }

    @Override
    public int getRateDAM() {
        if (getActor().isPolymorphedNpc()) {
            return getActor().getPolymorphedData().getMagicRate();
        }
        return 30;
    }

    @Override
    public int getRateSTUN() {
        return 3;
    }

    @Override
    public int getRateBUFF() {
        return 3;
    }

    @Override
    public int getRateHEAL() {
        return 5;
    }
}
