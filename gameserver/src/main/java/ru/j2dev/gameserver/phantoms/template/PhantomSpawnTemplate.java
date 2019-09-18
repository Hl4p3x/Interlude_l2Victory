package ru.j2dev.gameserver.phantoms.template;

import ru.j2dev.gameserver.phantoms.ai.PhantomAiType;
import ru.j2dev.gameserver.model.Territory;
import ru.j2dev.gameserver.templates.item.ItemGrade;

public class PhantomSpawnTemplate {
    private PhantomAiType type;
    private int count;
    private ItemGrade itemGradeMin;
    private ItemGrade itemGradeMax;
    private Territory territory;

    public PhantomSpawnTemplate() {
    }

    public PhantomSpawnTemplate(final PhantomAiType type, final int count, final ItemGrade itemGradeMin, final ItemGrade itemGradeMax, final Territory territory) {
        this.type = type;
        this.count = count;
        this.itemGradeMin = itemGradeMin;
        this.itemGradeMax = itemGradeMax;
        this.territory = territory;
    }

    public PhantomAiType getType() {
        return type;
    }

    public void setType(final PhantomAiType type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public ItemGrade getItemGradeMin() {
        return itemGradeMin;
    }

    public void setItemGradeMin(final ItemGrade itemGradeMin) {
        this.itemGradeMin = itemGradeMin;
    }

    public ItemGrade getItemGradeMax() {
        return itemGradeMax;
    }

    public void setItemGradeMax(final ItemGrade itemGradeMax) {
        this.itemGradeMax = itemGradeMax;
    }

    public Territory getTerritory() {
        return territory;
    }

    public void setTerritory(final Territory territory) {
        this.territory = territory;
    }
}
