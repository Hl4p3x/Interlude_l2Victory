package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.setting.common.ClassID;
import ru.j2dev.dataparser.holder.setting.common.PlayerRace;
import ru.j2dev.dataparser.holder.setting.model.NewPlayerBaseStat;
import ru.j2dev.dataparser.holder.transform.TransformData;

import java.util.*;

/**
 * @author : Mangol
 */
public class TransformHolder extends AbstractHolder {
    @Element(start = "transform_begin", end = "transform_end")
    private static List<TransformData> transformdata;
    private static final TransformHolder ourInstance = new TransformHolder();
    private static final Map<Integer, NewPlayerBaseStat> baseParameter = new HashMap<>();

    public static TransformHolder getInstance() {
        return ourInstance;
    }

    public List<TransformData> getTransformData() {
        return transformdata;
    }

    public TransformData getTransformationData(final int transformId) {
        final Optional<TransformData> transformationOptional = transformdata.stream().filter(transform -> transform.id == transformId).findFirst();
        return transformationOptional.orElse(null);
    }

    public NewPlayerBaseStat getTransformBaseParameter(final int transformId) {
        return baseParameter.get(transformId);
    }

    @Override
    public void afterParsing() {
        super.afterParsing();

        for (final TransformData transform : transformdata) {
            if (transform.male_begin.get(0).combat_begin != null) {
                final List<Integer> values = new ArrayList<>();
                values.add(0, (int) transform.male_begin.get(0).combat_begin.get(0).basic_stat[1]); // INT
                values.add(1, (int) transform.male_begin.get(0).combat_begin.get(0).basic_stat[0]); // STR
                values.add(2, (int) transform.male_begin.get(0).combat_begin.get(0).basic_stat[2]); // CON
                values.add(3, (int) transform.male_begin.get(0).combat_begin.get(0).basic_stat[5]); // MEN
                values.add(4, (int) transform.male_begin.get(0).combat_begin.get(0).basic_stat[3]); // DEX
                values.add(5, (int) transform.male_begin.get(0).combat_begin.get(0).basic_stat[4]); // WIT

                final NewPlayerBaseStat newTransStat = new NewPlayerBaseStat(PlayerRace.human, ClassID.fighter, ArrayUtils.toArray(values), ArrayUtils.toArray(values), ArrayUtils.toArray(values));
                baseParameter.put(transform.id, newTransStat);
            }
        }
    }

    @Override
    public int size() {
        return transformdata.size();
    }

    @Override
    public void clear() {
        transformdata.clear();
    }
}