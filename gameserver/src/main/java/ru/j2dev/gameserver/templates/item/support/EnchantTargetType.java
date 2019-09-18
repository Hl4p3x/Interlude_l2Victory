package ru.j2dev.gameserver.templates.item.support;

import ru.j2dev.gameserver.model.items.ItemInstance;

public enum EnchantTargetType {
    ALL(true, true, true),
    WEAPON(true, false, false),
    ARMOR(false, true, true);

    private final boolean _useOnWeapon;
    private final boolean _useOnArmor;
    private final boolean _useOnAccessory;

    EnchantTargetType(final boolean weapon, final boolean armor, final boolean accesory) {
        _useOnWeapon = weapon;
        _useOnArmor = armor;
        _useOnAccessory = accesory;
    }

    public boolean isUsableOn(final ItemInstance item) {
        return (_useOnWeapon && item.isWeapon()) || (_useOnArmor && item.isArmor()) || (_useOnAccessory && item.isAccessory());
    }
}
