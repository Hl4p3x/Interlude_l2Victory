package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.minigame.BlockupsetSettings;

/**
 * @author : Camelion
 * @date : 30.08.12 13:33
 */
public class MinigameHolder extends AbstractHolder {
    private static final MinigameHolder ourInstance = new MinigameHolder();
    @Element(start = "blockupset_setting_begin", end = "blockupset_setting_end")
    private BlockupsetSettings blockupsetSettings;

    private MinigameHolder() {
    }

    public static MinigameHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return 1;
    }

    public BlockupsetSettings getBlockupsetSettings() {
        return blockupsetSettings;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }
}