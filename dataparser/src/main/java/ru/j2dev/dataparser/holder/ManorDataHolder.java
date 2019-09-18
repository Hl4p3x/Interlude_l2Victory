package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.manordata.ManorData;

import java.util.List;

/**
 * @author : Camelion
 * @date : 30.08.12 13:13
 */
public class ManorDataHolder extends AbstractHolder {
    private static final ManorDataHolder ourInstance = new ManorDataHolder();
    @Element(start = "manor_begin", end = "manor_end")
    private List<ManorData> manorDataList;

    private ManorDataHolder() {
    }

    public static ManorDataHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return manorDataList.size();
    }

    public List<ManorData> getManorDatas() {
        return manorDataList;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }
}