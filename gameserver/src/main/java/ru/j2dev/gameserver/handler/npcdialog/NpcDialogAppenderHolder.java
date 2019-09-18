package ru.j2dev.gameserver.handler.npcdialog;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import ru.j2dev.commons.data.xml.AbstractHolder;

import java.util.List;

/**
 * @author VISTALL
 * @date 15:32 13.08.11
 */
public class NpcDialogAppenderHolder extends AbstractHolder {
    private static final NpcDialogAppenderHolder INSTANCE = new NpcDialogAppenderHolder();

    private final ListMultimap<Integer, INpcDialogAppender> appenders = ArrayListMultimap.create();

    public static NpcDialogAppenderHolder getInstance() {
        return INSTANCE;
    }

    public void register(INpcDialogAppender ap) {
        ap.getNpcIds().forEach(npcId -> appenders.put(npcId, ap));
    }

    public List<INpcDialogAppender> getAppenders(int val) {
        return appenders.get(val);
    }

    @Override
    public int size() {
        return appenders.size();
    }

    @Override
    public void clear() {
        appenders.clear();
    }
}
