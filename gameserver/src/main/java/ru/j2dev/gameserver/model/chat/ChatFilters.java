package ru.j2dev.gameserver.model.chat;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatFilter;

public class ChatFilters extends AbstractHolder {
    private static final ChatFilters _instance = new ChatFilters();

    private ChatFilter[] filters;

    private ChatFilters() {
        filters = new ChatFilter[0];
    }

    public static ChatFilters getInstance() {
        return _instance;
    }

    public ChatFilter[] getFilters() {
        return filters;
    }

    public void add(final ChatFilter f) {
        filters = ArrayUtils.add(filters, f);
    }

    @Override
    public void log() {
        info(String.format("loaded %d filter(s).", size()));
    }

    @Override
    public int size() {
        return filters.length;
    }

    @Override
    public void clear() {
        filters = new ChatFilter[0];
    }
}
