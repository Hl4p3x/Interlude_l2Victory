package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.ShortCut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ShortCutInit extends ShortCutPacket {
    private List<ShortcutInfo> _shortCuts;

    public ShortCutInit(final Player pl) {
        _shortCuts = Collections.emptyList();
        final Collection<ShortCut> shortCuts = pl.getAllShortCuts();
        _shortCuts = new ArrayList<>(shortCuts.size());
        shortCuts.forEach(shortCut -> _shortCuts.add(ShortCutPacket.convert(pl, shortCut)));
    }

    @Override
    protected final void writeImpl() {
        writeC(0x45);
        writeD(_shortCuts.size());
        _shortCuts.forEach(sc -> sc.write(this));
    }
}
