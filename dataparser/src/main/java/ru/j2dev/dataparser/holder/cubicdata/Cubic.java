package ru.j2dev.dataparser.holder.cubicdata;

import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;

/**
 * @author : Camelion
 * @date : 26.08.12 13:12
 */
@ParseSuper
public class Cubic extends DefaultCubicData {
    @Override
    public boolean isCubic() {
        return true;
    }
}
