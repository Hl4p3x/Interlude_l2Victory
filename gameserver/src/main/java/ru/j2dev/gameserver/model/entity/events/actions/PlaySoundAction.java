package ru.j2dev.gameserver.model.entity.events.actions;

import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.EventAction;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound.Type;

import java.util.List;
import java.util.Objects;

public class PlaySoundAction implements EventAction {
    private final int _range;
    private final String _sound;
    private final Type _type;

    public PlaySoundAction(final int range, final String s, final Type type) {
        _range = range;
        _sound = s;
        _type = type;
    }

    @Override
    public void call(final GlobalEvent event) {
        final GameObject object = event.getCenterObject();
        PlaySound packet;
        if (object != null) {
            packet = new PlaySound(_type, _sound, 1, object.getObjectId(), object.getLoc());
        } else {
            packet = new PlaySound(_type, _sound, 0, 0, 0, 0, 0);
        }
        final List<Player> players = event.broadcastPlayers(_range);
        players.stream().filter(Objects::nonNull).forEach(player -> player.sendPacket(packet));
    }
}
