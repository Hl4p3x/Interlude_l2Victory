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

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

@HideAccess
@StringEncryption
public class TeamOlympiadPlayer extends OlympiadPlayer {
    private final HardReference<Player>[] _playerRefs;
    private final int[] _playerClassIds;
    private final double[] _damage;
    private final boolean[] _alive;
    private String _name;

    @SuppressWarnings("unchecked")
    public TeamOlympiadPlayer(final int side, final OlympiadGame comp, final Player[] players) {
        super(side, comp);
        _name = "";
        _damage = new double[players.length];
        _alive = new boolean[players.length];
        _playerClassIds = new int[players.length];
        _playerRefs = (HardReference<Player>[]) new HardReference[players.length];
        _name = players[0].getName();
        IntStream.range(0, players.length).forEach(i -> {
            _playerRefs[i] = players[i].getRef();
            _playerClassIds[i] = players[i].getActiveClassId();
            _damage[i] = 0.0;
            _alive[i] = true;
        });
    }

    @Override
    public void onStart() {
        Arrays.stream(getPlayers()).forEach(player -> player.setOlyParticipant(this));
    }

    @Override
    public void onFinish() {
        Arrays.stream(getPlayers()).forEach(player -> {
            if (player.isDead()) {
                player.doRevive(100.0);
            }
            player.setOlyParticipant(null);
        });
    }

    @Override
    public void onDamaged(final Player player, final Creature attacker, final double damage, final double hp) {
        if (player.isOlyCompetitionFinished()) {
            return;
        }
        IntStream.range(0, _playerRefs.length).filter(i -> _alive[i] && _playerRefs[i].get() == player).forEach(i -> {
            if (attacker.isPlayer()) {
                _damage[i] += Math.min(damage, hp);
            }
            if (damage >= hp) {
                _alive[i] = false;
                attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                attacker.abortAttack(true, true);
                if (attacker.isCastingNow()) {
                    attacker.abortCast(true, false);
                }
                attacker.sendActionFailed();
                getCompetition().ValidateWinner();
            }
        });
    }

    @Override
    public void onDisconnect(final Player player) {
        if (player.isOlyCompetitionFinished()) {
            return;
        }
        IntStream.range(0, _playerRefs.length).forEach(i -> _alive[i] = false);
        getCompetition().ValidateWinner();
    }

    @Override
    public void sendPacket(final L2GameServerPacket gsp) {
        Arrays.stream(getPlayers()).forEach(player -> player.sendPacket(gsp));
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean isAlive() {
        for (final boolean alv : _alive) {
            if (alv) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPlayerLoose(final Player player) {
        for (int i = 0; i < _playerRefs.length; ++i) {
            if (_playerRefs[i].get() == player) {
                return !_alive[i];
            }
        }
        return false;
    }

    @Override
    public double getDamageOf(final Player player) {
        for (int i = 0; i < _playerRefs.length; ++i) {
            if (_playerRefs[i].get() == player) {
                return _damage[i];
            }
        }
        return 0.0;
    }

    @Override
    public Player[] getPlayers() {
        return Arrays.stream(_playerRefs).map(HardReference::get).filter(Objects::nonNull).toArray(Player[]::new);
    }

    @Override
    public double getTotalDamage() {
        double rdmg = 0.0;
        for (final double dmg : _damage) {
            rdmg += dmg;
        }
        return rdmg;
    }

    @Override
    public boolean validateThis() {
        OlympiadPlayer oponent = null;
        for (final OlympiadPlayer p : getCompetition().getGamePlayers()) {
            if (p != this) {
                oponent = p;
            }
        }
        for (int refIdx = 0; refIdx < _playerRefs.length; ++refIdx) {
            final Player player = _playerRefs[refIdx].get();
            if (player == null || !player.isOnline() || player.isLogoutStarted()) {
                oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
                return false;
            }
            if (player.isDead()) {
                sendPacket(new SystemMessage(1858).addName(player));
                oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
                return false;
            }
            if (player.getActiveClassId() != _playerClassIds[refIdx] || !player.getActiveClass().isBase()) {
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
        }
        return true;
    }
}
