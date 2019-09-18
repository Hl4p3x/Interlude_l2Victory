package ru.j2dev.gameserver.phantoms.template;

import ru.j2dev.gameserver.phantoms.ai.PhantomAiType;
import ru.j2dev.gameserver.templates.item.ItemGrade;

public class PhantomTemplate {
    private int classId;
    private int race;
    private int sex;
    private int face;
    private int hair;
    private int nameColor;
    private int titleColor;
    private String name;
    private String title;
    private PhantomAiType type;
    private ItemGrade itemGrade;

    public PhantomTemplate() {
    }

    public PhantomTemplate(final int classId, final int race, final int sex, final int face, final int hair, final int nameColor, final int titleColor, final String name, final String title, final PhantomAiType type, final ItemGrade itemGrade) {
        this.classId = classId;
        this.race = race;
        this.sex = sex;
        this.face = face;
        this.hair = hair;
        this.nameColor = nameColor;
        this.titleColor = titleColor;
        this.name = name;
        this.title = title;
        this.type = type;
        this.itemGrade = itemGrade;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(final int classId) {
        this.classId = classId;
    }

    public int getRace() {
        return race;
    }

    public void setRace(final int race) {
        this.race = race;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(final int sex) {
        this.sex = sex;
    }

    public int getFace() {
        return face;
    }

    public void setFace(final int face) {
        this.face = face;
    }

    public int getHair() {
        return hair;
    }

    public void setHair(final int hair) {
        this.hair = hair;
    }

    public int getNameColor() {
        return nameColor;
    }

    public void setNameColor(final int nameColor) {
        this.nameColor = nameColor;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(final int titleColor) {
        this.titleColor = titleColor;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public PhantomAiType getType() {
        return type;
    }

    public void setType(final PhantomAiType type) {
        this.type = type;
    }

    public ItemGrade getItemGrade() {
        return itemGrade;
    }

    public void setItemGrade(final ItemGrade itemGrade) {
        this.itemGrade = itemGrade;
    }
}
