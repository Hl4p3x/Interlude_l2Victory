package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.dyedata.DyeData;

import java.util.List;

/**
 * @author : Camelion
 * @date : 27.08.12 1:36
 */
public class DyeDataHolder extends AbstractHolder {
    private static final DyeDataHolder ourInstance = new DyeDataHolder();
    @Element(start = "dye_begin", end = "dye_end")
    private List<DyeData> dyes;

    private DyeDataHolder() {
    }

    public static DyeDataHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return dyes.size();
    }

    public List<DyeData> getDyes() {
        return dyes;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }
}