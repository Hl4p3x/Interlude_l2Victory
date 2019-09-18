package ru.j2dev.gameserver.phantoms.ai;

import ru.j2dev.gameserver.phantoms.model.Phantom;

/**
 * Created by JunkyFunky
 * on 20.05.2018 20:11
 * group j2dev
 */
public class PhantomPvpAi extends AbstractPhantomAi {
    public PhantomPvpAi(Phantom actor) {
        super(actor);
    }

    @Override
    public void runImpl() {

    }

    @Override
    public PhantomAiType getType() {
        return PhantomAiType.PVP;
    }
}
