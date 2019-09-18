package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.factory.IObjectFactory;
import ru.j2dev.dataparser.holder.fishingdata.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : Camelion
 * @date : 27.08.12 2:50
 */
public class FishingDataHolder extends AbstractHolder {
    private static final Pattern fishingMonsterPattern = Pattern.compile("fishingmonsters=\\{([\\S;]+)}");
    private static final FishingDataHolder ourInstance = new FishingDataHolder();
    @Element(start = "distribution_begin", end = "distribution_end")
    public List<FishingDistribution> distributions;
    @Element(start = "fishing_place_begin", end = "fishing_place_end")
    public List<FishingPlace> fishingPlaces;
    @Element(start = "lure_begin", end = "lure_end")
    public List<Lure> fishingLures;
    @Element(start = "fish_begin", end = "fish_end")
    public List<Fish> fishes;
    @Element(start = "fishingrod_begin", end = "fishingrod_end")
    public List<FishingRod> fishingRods;
    @Element(start = "fishingmonster_begin", end = "fishingmonster_end", objectFactory = FishingMonsterObjectFactory.class)
    public List<FishingMonster> fishingMonsters;

    private FishingDataHolder() {
    }

    public static FishingDataHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return distributions.size() + fishingPlaces.size() + fishingLures.size() + fishes.size() + fishingRods.size() + fishingMonsters.size();
    }

    public List<FishingDistribution> getDistributions() {
        return distributions;
    }

    public List<FishingPlace> getFishingPlaces() {
        return fishingPlaces;
    }

    public List<Lure> getFishingLures() {
        return fishingLures;
    }

    public List<Fish> getFishes() {
        return fishes;
    }

    public List<FishingRod> getFishingRods() {
        return fishingRods;
    }

    public List<FishingMonster> getFishingMonsters() {
        return fishingMonsters;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }

    public static class FishingMonsterObjectFactory implements IObjectFactory<FishingMonster> {
        private Class<?> clazz;

        @Override
        public FishingMonster createObjectFor(StringBuilder data) throws IllegalAccessException, InstantiationException {
            FishingMonster monster = (FishingMonster) clazz.newInstance();
            Matcher matcher = fishingMonsterPattern.matcher(data);
            if (matcher.find()) {
                monster.fishingmonsters = matcher.group(1).split(";");
            }
            return monster;
        }

        @Override
        public void setFieldClass(Class<?> clazz) {
            this.clazz = clazz;
        }
    }
}