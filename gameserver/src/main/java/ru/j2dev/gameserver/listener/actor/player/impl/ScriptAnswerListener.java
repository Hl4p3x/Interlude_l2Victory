package ru.j2dev.gameserver.listener.actor.player.impl;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.gameserver.listener.actor.player.OnAnswerListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.scripts.Scripts;

public class ScriptAnswerListener implements OnAnswerListener {
    private final HardReference<Player> _playerRef;
    private final String _scriptName;
    private final Object[] _arg;
    private final long _endTime;

    public ScriptAnswerListener(final Player player, final String scriptName, final Object[] arg, final long time) {
        _scriptName = scriptName;
        _arg = arg;
        _playerRef = player.getRef();
        _endTime = System.currentTimeMillis() + time;
    }

    @Override
    public void sayYes() {
        final Player player = _playerRef.get();
        if (player == null || System.currentTimeMillis() > _endTime) {
            return;
        }
        Scripts.getInstance().callScripts(player, _scriptName.split(":")[0], _scriptName.split(":")[1], _arg);
    }

    @Override
    public void sayNo() {
    }
}
