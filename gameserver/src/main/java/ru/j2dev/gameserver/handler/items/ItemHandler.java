package ru.j2dev.gameserver.handler.items;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

import java.util.Arrays;

public class ItemHandler extends AbstractHolder {
    private static final ItemHandler _instance = new ItemHandler();

    public static ItemHandler getInstance() {
        return _instance;
    }

    public void registerItemHandler(final IItemHandler handler) {
        final int[] ids = handler.getItemIds();
        Arrays.stream(ids).forEach(itemId -> {
            final ItemTemplate template = ItemTemplateHolder.getInstance().getTemplate(itemId);
            if (template == null) {
                warn("Item not found: " + itemId + " handler: " + handler.getClass().getSimpleName());
            } else if (template.getHandler() != IItemHandler.NULL) {
                warn("Duplicate handler for item: " + itemId + "(" + template.getHandler().getClass().getSimpleName() + "," + handler.getClass().getSimpleName() + ")");
            } else {
                template.setHandler(handler);
            }
        });
    }

    public void unregisterItemHandler(final IItemHandler handler) {
        final int[] ids = handler.getItemIds();
        Arrays.stream(ids).forEach(itemId -> {
            final ItemTemplate template = ItemTemplateHolder.getInstance().getTemplate(itemId);
            if (template == null) {
                warn("Item not found: " + itemId + " handler: " + handler.getClass().getSimpleName());
            } else if (template.getHandler() != handler) {
                warn("Attempt to unregister item handler");
            } else {
                template.setHandler(handler);
            }
        });
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {
    }
}
