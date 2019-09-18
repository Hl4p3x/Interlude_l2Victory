package ru.j2dev.gameserver.listener.actor.player.impl;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.gameserver.listener.actor.player.OnAnswerListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.PetInstance;

public class ReviveAnswerListener implements OnAnswerListener {
    private final HardReference<Player> _playerRef;
    private final double _power;
    private final boolean _forPet;

    public ReviveAnswerListener(final Player player, final double power, final boolean forPet) {
        _playerRef = player.getRef();
        _forPet = forPet;
        _power = power;
    }

    @Override
    public void sayYes() {
        final Player player = _playerRef.get();
        if (player == null) {
            return;
        }
        if ((!player.isDead() && !_forPet) || (_forPet && player.getPet() != null && !player.getPet().isDead())) {
            return;
        }
        if (!_forPet) {
            player.doRevive(_power);
        } else if (player.getPet() != null) {
            ((PetInstance) player.getPet()).doRevive(_power);
        }
    }

    @Override
    public void sayNo() {
    }

    public double getPower() {
        return _power;
    }

    public boolean isForPet() {
        return _forPet;
    }
}
