package ru.j2dev.gameserver.model.entity.events.objects;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.utils.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DuelSnapshotObject implements Serializable {
    private final TeamType _team;
    private final HardReference<Player> _playerRef;
    private final int _activeClass;
    private final List<Effect> _effects;
    private final Location _returnLoc;
    private final double _currentHp;
    private final double _currentMp;
    private final double _currentCp;
    private boolean _isDead;

    public DuelSnapshotObject(final Player player, final TeamType team) {
        _playerRef = player.getRef();
        _team = team;
        _returnLoc = ((player.getReflection().getReturnLoc() == null) ? player.getLoc() : player.getReflection().getReturnLoc());
        _currentCp = player.getCurrentCp();
        _currentHp = player.getCurrentHp();
        _currentMp = player.getCurrentMp();
        _activeClass = player.getActiveClassId();
        final List<Effect> effectList = player.getEffectList().getAllEffects();
        _effects = new ArrayList<>(effectList.size());
        for (final Effect $effect : effectList) {
            final Effect effect = $effect.getTemplate().getEffect(new Env($effect.getEffector(), $effect.getEffected(), $effect.getSkill()));
            if (!effect.isSaveable()) {
                continue;
            }
            effect.setCount($effect.getCount());
            effect.setPeriod(($effect.getCount() == 1) ? ($effect.getPeriod() - $effect.getTime()) : $effect.getPeriod());
            _effects.add(effect);
        }
    }

    public void restore(final boolean abnormal) {
        final Player player = getPlayer();
        if (player == null) {
            return;
        }
        if (!abnormal) {
            player.getEffectList().stopAllEffects();
            if (_activeClass == player.getActiveClassId()) {
                for (final Effect e : _effects) {
                    if (player.getEffectList().getEffectsBySkill(e.getSkill()) == null) {
                        player.getEffectList().addEffect(e);
                    }
                }
            }
            player.setCurrentCp(_currentCp);
            player.setCurrentHpMp(_currentHp, _currentMp);
        }
    }

    public void teleport() {
        final Player player = getPlayer();
        player._stablePoint = null;
        if (player.isFrozen()) {
            player.stopFrozen();
        }
        ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                final Player player = getPlayer();
                if (player == null) {
                    return;
                }
                player.teleToLocation(_returnLoc, ReflectionManager.DEFAULT);
            }
        }, 5000L);
    }

    public Player getPlayer() {
        return _playerRef.get();
    }

    public boolean isDead() {
        return _isDead;
    }

    public void setDead() {
        _isDead = true;
    }

    public Location getLoc() {
        return _returnLoc;
    }

    public TeamType getTeam() {
        return _team;
    }
}
