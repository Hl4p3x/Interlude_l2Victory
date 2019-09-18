package ru.j2dev.gameserver.ai;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.entity.boat.Boat;

public class BoatAI extends CharacterAI {
    public BoatAI(final Creature actor) {
        super(actor);
    }

    @Override
    protected void onEvtArrived() {
        final Boat actor = (Boat) getActor();
        if (actor == null) {
            return;
        }
        actor.onEvtArrived();
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }
}
