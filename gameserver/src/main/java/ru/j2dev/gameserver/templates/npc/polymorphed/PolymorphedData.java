package ru.j2dev.gameserver.templates.npc.polymorphed;


import ru.j2dev.commons.util.Rnd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by JunkyFunky
 * on 02.01.2018 19:33
 * group j2dev
 */
public class PolymorphedData {

    private int npcId;

    private int classId;
    private int sex;
    private int hairStyle;
    private int hairColor;
    private int face;
    private List<String> title_colors = Collections.emptyList();
    private List<String> name_colors = Collections.emptyList();
    private int hero;
    private int recomend;
    private int magicRate = 5;
    private int weaponEnchant;
    private int race;
    private int collision_radius;
    private int collision_height;
    private List<Integer> _items = Collections.emptyList();
    private PolymorphedInventory inventory;

    public PolymorphedData(int npcId) {
        this.npcId = npcId;
    }

    public int getMagicRate() {
        return magicRate;
    }

    public void setMagicRate(int magicRate) {
        this.magicRate = magicRate;
    }

    public int getWeaponEnchant() {
        return weaponEnchant;
    }

    public void setWeaponEnchant(int weaponEnchant) {
        this.weaponEnchant = weaponEnchant;
    }

    public void addTitleColors(String color) {
        if (title_colors.isEmpty()) {
            title_colors = new ArrayList<>();
        }
        title_colors.add(color.trim());
    }

    public void addNameColors(String color) {
        if (name_colors.isEmpty()) {
            name_colors = new ArrayList<>();
        }
        name_colors.add(color.trim());
    }

    public int getHero() {
        return hero;
    }

    public void setHero(int hero) {
        this.hero = hero;
    }

    public int getRace() {
        return race;
    }

    public void setRace(int race) {
        this.race = race;
    }

    public int getCollisionRadius() {
        return collision_radius;
    }

    public void setCollisionRadius(int collision_radius) {
        this.collision_radius = collision_radius;
    }

    public int getCollisionHeight() {
        return collision_height;
    }

    public void setCollisionHeight(int collision_height) {
        this.collision_height = collision_height;
    }

    public List<Integer> getItems() {
        return _items;
    }

    public void addItem(int itemsId) {
        if (_items.isEmpty()) {
            _items = new ArrayList<>();
        }
        _items.add(itemsId);
    }

    public int getRndTitleColor() {
        return Integer.decode(Rnd.get(title_colors));
    }

    public int getRndNameColor() {
        return Integer.decode(Rnd.get(name_colors));
    }

    public PolymorphedInventory getInventory() {
        return inventory;
    }

    //
    public void setInventory(PolymorphedInventory inventory) {
        this.inventory = inventory;
    }

    public int getNpcId() {
        return npcId;
    }

    public void setNpcId(int npcId) {
        this.npcId = npcId;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getHairStyle() {
        return hairStyle;
    }

    public void setHairStyle(int hairStyle) {
        this.hairStyle = hairStyle;
    }

    public int getHairColor() {
        return hairColor;
    }

    public void setHairColor(int hairColor) {
        this.hairColor = hairColor;
    }

    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public int getRecomHave() {
        return recomend;
    }

    public void setRecomend(int recomend) {
        if (recomend > 255) {
            recomend = 255;
        }
        this.recomend = recomend;
    }
}
