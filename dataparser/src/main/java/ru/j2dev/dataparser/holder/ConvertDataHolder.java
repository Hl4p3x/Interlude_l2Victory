package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.convertdata.ConvertData;

import java.util.List;

/**
 * @author : Camelion
 * @date : 25.08.12 22:47
 */
public class ConvertDataHolder extends AbstractHolder {
    private static final ConvertDataHolder ourInstance = new ConvertDataHolder();
    @Element(start = "convert_begin", end = "convert_end")
    public List<ConvertData> convertDatas;

    private ConvertDataHolder() {
    }

    public static ConvertDataHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return convertDatas.size();
    }

    public List<ConvertData> getConvertDatas() {
        return convertDatas;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }
}