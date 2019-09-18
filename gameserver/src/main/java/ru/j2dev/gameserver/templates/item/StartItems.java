package ru.j2dev.gameserver.templates.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Solution
 * 14.08.2018
 * 20:10
 */

public class StartItems {
    private int classId;
    private List<Integer> _items = Collections.emptyList();
    private int enchantLvL;

    public StartItems(int classId) {
        this.classId = classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getClassId() {
        return classId;
    }

    public List<Integer> getAmunitions() {
        return _items;
    }

    public void addItem(int itemsId) {
        if (_items.isEmpty()) {
            _items = new ArrayList<>();
        }
        _items.add(itemsId);
    }

    public void setEnchantLvL(int enchantLvL) {
        this.enchantLvL = enchantLvL;
    }

    public int getEnchantLvL() {
        return enchantLvL;
    }
}
