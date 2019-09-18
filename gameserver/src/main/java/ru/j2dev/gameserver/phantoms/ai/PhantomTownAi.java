package ru.j2dev.gameserver.phantoms.ai;

import ru.j2dev.gameserver.phantoms.PhantomConfig;
import ru.j2dev.gameserver.phantoms.action.MoveToNpcAction;
import ru.j2dev.gameserver.phantoms.action.RandomMoveAction;
import ru.j2dev.gameserver.phantoms.action.RandomUserAction;
import ru.j2dev.gameserver.phantoms.action.SpeakAction;
import ru.j2dev.gameserver.phantoms.model.Phantom;
import ru.j2dev.commons.util.Rnd;

public class PhantomTownAi extends AbstractPhantomAi {
    public PhantomTownAi(final Phantom actor) {
        super(actor);
    }

    @Override
    public void runImpl() {
        if (Rnd.chance(PhantomConfig.randomMoveChance)) {
            actor.doAction(new RandomMoveAction());
        } else if (Rnd.chance(PhantomConfig.moveToNpcChance)) {
            actor.doAction(new MoveToNpcAction());
        } else if (Rnd.chance(PhantomConfig.userActionChance)) {
            actor.doAction(new RandomUserAction());
        } else if (Rnd.chance(PhantomConfig.chatspeakChance)) {
            actor.doAction(new SpeakAction());
        }
    }

    @Override
    public PhantomAiType getType() {
        return PhantomAiType.TOWN;
    }
}
