package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.ElementArray;
import ru.j2dev.dataparser.holder.castledata.*;

import java.util.List;

/**
 * @author : Camelion
 * @date : 25.08.12 22:53
 */
public class CastleDataHolder extends AbstractHolder {
    private static final CastleDataHolder ourInstance = new CastleDataHolder();
    @ElementArray(start = "castle_siege_music_begin", end = "castle_siege_music_end")
    private SiegeMusic[] castleSiegeMusic;
    @ElementArray(start = "fortress_siege_music_begin", end = "fortress_siege_music_end")
    private SiegeMusic[] fortressSiegeMusic;
    @Element(start = "castle_begin", end = "castle_end")
    private List<Castle> castles;
    @Element(start = "agit_begin", end = "agit_end")
    private List<Agit> agits;
    @Element(start = "dominion_begin", end = "dominion_end")
    private List<Dominion> dominions;
    @Element(start = "fortress_begin", end = "fortress_end")
    private List<Fortress> fortresses;
    @Element(start = "castle_common_begin", end = "castle_common_end")
    private CastleCommon castle_common;

    private CastleDataHolder() {
    }

    public static CastleDataHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return castleSiegeMusic.length + fortressSiegeMusic.length + castles.size() + agits.size() + dominions.size() + fortresses.size();
    }

    public SiegeMusic[] getCastleSiegeMusic() {
        return castleSiegeMusic;
    }

    public SiegeMusic[] getFortressSiegeMusic() {
        return fortressSiegeMusic;
    }

    public List<Castle> getCastles() {
        return castles;
    }

    public List<Agit> getAgits() {
        return agits;
    }

    public List<Dominion> getDominions() {
        return dominions;
    }

    public List<Fortress> getFortresses() {
        return fortresses;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }
}