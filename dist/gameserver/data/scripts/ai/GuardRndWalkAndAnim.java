package ai;

import ru.j2dev.gameserver.ai.Guard;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class GuardRndWalkAndAnim extends Guard {
    public GuardRndWalkAndAnim(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        return super.thinkActive() || randomAnimation() || randomWalk();
    }
}
