package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.ShortCut;

public class ShortCutRegister extends ShortCutPacket {
    private final ShortcutInfo _shortcutInfo;

    public ShortCutRegister(final Player player, final ShortCut sc) {
        _shortcutInfo = ShortCutPacket.convert(player, sc);
    }

    @Override
    protected final void writeImpl() {
        writeC(0x44);
        _shortcutInfo.write(this);
    }
}
