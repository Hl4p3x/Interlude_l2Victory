package ru.j2dev.gameserver.network.lineage2.components;

import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

public class CustomMessage {
    private String _text;
    private int mark;

    public CustomMessage(final String address, final Player player, final Object... args) {
        mark = 0;
        _text = StringHolder.getInstance().getNotNull(player, address);
        add(args);
    }

    public CustomMessage addNumber(final long number) {
        _text = _text.replace("{" + mark + "}", String.valueOf(number));
        ++mark;
        return this;
    }

    public CustomMessage add(final Object... args) {
        for (final Object arg : args) {
            if (arg instanceof String) {
                addString((String) arg);
            } else if (arg instanceof Integer) {
                addNumber((int) arg);
            } else if (arg instanceof Long) {
                addNumber((long) arg);
            } else if (arg instanceof ItemTemplate) {
                addItemName((ItemTemplate) arg);
            } else if (arg instanceof ItemInstance) {
                addItemName((ItemInstance) arg);
            } else if (arg instanceof Creature) {
                addCharName((Creature) arg);
            } else if (arg instanceof Skill) {
                addSkillName((Skill) arg);
            } else {
                System.out.println("unknown CustomMessage arg type: " + arg);
                Thread.dumpStack();
            }
        }
        return this;
    }

    public CustomMessage addString(final String str) {
        _text = _text.replace("{" + mark + "}", str);
        ++mark;
        return this;
    }

    public CustomMessage addSkillName(final Skill skill) {
        _text = _text.replace("{" + mark + "}", skill.getName());
        ++mark;
        return this;
    }

    public CustomMessage addSkillName(final int skillId, final int skillLevel) {
        return addSkillName(SkillTable.getInstance().getInfo(skillId, skillLevel));
    }

    public CustomMessage addItemName(final ItemTemplate item) {
        _text = _text.replace("{" + mark + "}", item.getName());
        ++mark;
        return this;
    }

    public CustomMessage addItemName(final int itemId) {
        return addItemName(ItemTemplateHolder.getInstance().getTemplate(itemId));
    }

    public CustomMessage addItemName(final ItemInstance item) {
        return addItemName(item.getTemplate());
    }

    public CustomMessage addCharName(final Creature cha) {
        _text = _text.replace("{" + mark + "}", cha.getName());
        ++mark;
        return this;
    }

    @Override
    public String toString() {
        return _text;
    }
}
