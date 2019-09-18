package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.multisell.Multisell;

import java.util.List;

/**
 * @author : Camelion
 * @date : 30.08.12 14:23
 */
public class MultisellHolder extends AbstractHolder {
    private static final MultisellHolder ourInstance = new MultisellHolder();
    @Element(start = "MultiSell_begin", end = "MultiSell_end")
    private List<Multisell> multisells;

    private MultisellHolder() {
    }

    public static MultisellHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return multisells.size();
    }

    public List<Multisell> getMultisells() {
        return multisells;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }
}