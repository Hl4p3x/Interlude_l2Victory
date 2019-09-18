package ru.j2dev.gameserver.model.entity.olympiad.participants;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadGame;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadPlayer;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

@HideAccess
@StringEncryption
public class SinglePlayerOlympiadPlayer extends OlympiadPlayer {
    private final HardReference<Player> _playerRef;
    private final int _playerClassId;
    private final String _name;
    private double _damage;
    private boolean _alive;

    public SinglePlayerOlympiadPlayer(final int side, final OlympiadGame comp, final Player player) {
        super(side, comp);
        _playerRef = player.getRef();
        _playerClassId = player.getActiveClassId();
        _name = player.getName();
        _alive = true;
    }

    private Player getPlayer() {
        return _playerRef.get();
    }

    @Override
    public void onStart() {
        final Player player = getPlayer();
        if (player != null) {
            player.setOlyParticipant(this);
        }
    }

    @Override
    public void onFinish() {
        final Player player = getPlayer();
        if (player != null) {
            player.setOlyParticipant(null);
        }
    }

    @Override
    public void onDamaged(final Player player, final Creature attacker, final double damage, final double hp) {
        if (!player.isOlyCompetitionStarted()) {
            return;
        }
        if (attacker.isPlayer()) {
            _damage += Math.min(damage, hp);
        }
        if (damage >= hp) {
            _alive = false;
            attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            attacker.abortAttack(true, true);
            if (attacker.isCastingNow()) {
                attacker.abortCast(true, false);
            }
            attacker.sendActionFailed();
            getCompetition().ValidateWinner();
            player.setCurrentHp(1.0, false);
        }
    }

    @Override
    public void onDisconnect(final Player player) {
        if (player.isOlyCompetitionFinished()) {
            return;
        }
        _alive = false;
        getCompetition().ValidateWinner();
    }

    @Override
    public void sendPacket(final L2GameServerPacket gsp) {
        final Player player = getPlayer();
        if (player != null) {
            player.sendPacket(gsp);
        }
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean isAlive() {
        return _alive;
    }

    @Override
    public boolean isPlayerLoose(final Player player) {
        return player != null && player == _playerRef.get() && !_alive;
    }

    @Override
    public double getDamageOf(final Player player) {
        if (player != null && player == _playerRef.get()) {
            return _damage;
        }
        return 0.0;
    }

    @Override
    public Player[] getPlayers() {
        if (getPlayer() != null) {
            return new Player[]{getPlayer()};
        }
        return new Player[0];
    }

    @Override
    public double getTotalDamage() {
        return _damage;
    }

    @Override
    public boolean validateThis() {
        OlympiadPlayer oponent = null;
        for (final OlympiadPlayer p : getCompetition().getGamePlayers()) {
            if (p != this) {
                oponent = p;
            }
        }
        final Player player = _playerRef.get();
        if (player == null || !player.isOnline() || player.isLogoutStarted()) {
            oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
            return false;
        }
        if (player.isDead()) {
            sendPacket(new SystemMessage(1858).addName(player));
            oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
            return false;
        }
        if (player.getActiveClassId() != _playerClassId || !player.getActiveClass().isBase()) {
            player.sendPacket(new SystemMessage(1692).addName(player));
            oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
            return false;
        }
        if (player.isCursedWeaponEquipped()) {
            sendPacket(new SystemMessage(1857).addName(player).addItemName(player.getCursedWeaponEquippedId()));
            oponent.sendPacket(new SystemMessage(1856).addItemName(player.getCursedWeaponEquippedId()));
            return false;
        }
        if (!player.isInPeaceZone()) {
            oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
            return false;
        }
        final SystemMessage msg = OlympiadGame.checkPlayer(player);
        if (msg != null) {
            sendPacket(msg);
            oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
            return false;
        }
        return true;
    }
}
