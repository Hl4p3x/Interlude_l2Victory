package ru.j2dev.gameserver.model.entity.olympiad;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;

@HideAccess
@StringEncryption
public abstract class OlympiadPlayer {
    public static final int SIDE_RED = 2;
    public static final int SIDE_BLUE = 1;

    private final int _side;
    private final OlympiadGame _comp;

    protected OlympiadPlayer(final int side, final OlympiadGame comp) {
        _side = side;
        _comp = comp;
    }

    public final OlympiadGame getCompetition() {
        return _comp;
    }

    public final int getSide() {
        return _side;
    }

    public abstract void onStart();

    public abstract void onFinish();

    public abstract void onDamaged(final Player p0, final Creature p1, final double p2, final double p3);

    public abstract void onDisconnect(final Player p0);

    public abstract void sendPacket(final L2GameServerPacket p0);

    public abstract String getName();

    public abstract boolean isAlive();

    public abstract boolean isPlayerLoose(final Player p0);

    public abstract double getDamageOf(final Player p0);

    public abstract Player[] getPlayers();

    public abstract double getTotalDamage();

    public abstract boolean validateThis();
}
