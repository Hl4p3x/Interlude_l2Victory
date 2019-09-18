package ru.j2dev.gameserver.model;

import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;

import java.util.Collections;
import java.util.Iterator;

public interface PlayerGroup extends Iterable<Player> {
    PlayerGroup EMPTY = new PlayerGroup() {
        @Override
        public void broadCast(final IStaticPacket... packet) {
        }


        @Override
        public Iterator<Player> iterator() {
            return Collections.emptyIterator();
        }
    };

    void broadCast(final IStaticPacket... p0);
}
