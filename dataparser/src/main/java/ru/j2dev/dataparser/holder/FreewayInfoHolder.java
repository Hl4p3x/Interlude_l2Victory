package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.freeway.Freeway;

import java.util.List;

/**
 * @author : Camelion
 * @date : 27.08.12 13:14
 */
public class FreewayInfoHolder extends AbstractHolder {
    private static final FreewayInfoHolder ourInstance = new FreewayInfoHolder();
    @Element(start = "freeway_begin", end = "freeway_end")
    private List<Freeway> freeways;

    private FreewayInfoHolder() {
    }

    public static FreewayInfoHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return freeways.size();
    }

    public List<Freeway> getFreeways() {
        return freeways;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }
}