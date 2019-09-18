package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;

public class BlacksmithMammon extends MammonMerchant {
    private static final long chatDelay = 1800000L;
    private static final Location[] _teleports = {new Location(-19360, 13278, -4901, 0), new Location(-53190, -250493, -7908, 0), new Location(46303, 170091, -4981, 0), new Location(-20543, -251010, -8164, 0), new Location(12620, -248690, -9580, 0), new Location(140519, 79464, -5429, 0)};
    private static final String[] mamonText = {"Rulers of the seal! I bring you wondrous gifts!", "Rulers of the seal! I have some excellent weapons to show you!", "I've been so busy lately, in addition to planning my trip!"};
    private long _chatVar;

    public BlacksmithMammon(final NpcInstance actor) {
        super(actor);
        _chatVar = 0L;
    }

    @Override
    protected Location getRndTeleportLoc() {
        return _teleports[Rnd.get(_teleports.length)];
    }

    @Override
    protected boolean thinkActive() {
        if (super.thinkActive()) {
            return true;
        }
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return true;
        }
        if (_chatVar + chatDelay < System.currentTimeMillis()) {
            _chatVar = System.currentTimeMillis();
            Functions.npcShout(actor, mamonText[Rnd.get(mamonText.length)]);
        }
        return false;
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

}