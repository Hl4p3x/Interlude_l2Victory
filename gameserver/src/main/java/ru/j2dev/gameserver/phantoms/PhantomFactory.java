package ru.j2dev.gameserver.phantoms;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.SubClass;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.phantoms.ai.*;
import ru.j2dev.gameserver.phantoms.data.holder.PhantomEquipHolder;
import ru.j2dev.gameserver.phantoms.model.Phantom;
import ru.j2dev.gameserver.phantoms.model.PhantomInventory;
import ru.j2dev.gameserver.phantoms.template.PhantomEquipTemplate;
import ru.j2dev.gameserver.phantoms.template.PhantomTemplate;
import ru.j2dev.gameserver.tables.CharTemplateTable;
import ru.j2dev.gameserver.templates.PlayerTemplate;
import ru.j2dev.gameserver.templates.item.ItemGrade;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PhantomFactory {
    private static final int defaultNameColor = 0xffffff;
    private static final int defaultTitleColor = 0xffff77;
    private static final PhantomFactory instance = new PhantomFactory();

    public static PhantomFactory getInstance() {
        return PhantomFactory.instance;
    }

    @SuppressWarnings("unchecked")
    private static void applyMagic(final Phantom phantom) {
        final Class<? extends Phantom> phantomClass = phantom.getClass();
        try {
            final Class<Player> playerClass = (Class<Player>) phantomClass.getSuperclass();
            final Field inventoryField = playerClass.getDeclaredField("_inventory");
            final boolean accessible = inventoryField.isAccessible();
            try {
                inventoryField.setAccessible(true);
                inventoryField.set(phantom, new PhantomInventory(phantom));
            } finally {
                inventoryField.setAccessible(accessible);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Phantom create(final PhantomTemplate template) {
        final int objId = IdFactory.getInstance().getNextId();
        final int nameColor = (template.getNameColor() == 0) ? defaultNameColor : template.getNameColor();
        final int titleColor = (template.getTitleColor() == 0) ? defaultTitleColor : template.getTitleColor();
        final String title = (template.getTitle() == null) ? "" : template.getTitle();
        final PlayerTemplate playerTemplate = CharTemplateTable.getInstance().getTemplate(template.getClassId(), template.getSex() != 0);
        final Phantom phantom = new Phantom(objId, playerTemplate, "phantom_bot_" + objId);
        applyMagic(phantom);
        phantom.setRace(0, template.getRace());
        final SubClass sub = new SubClass();
        sub.setActive(true);
        sub.setBase(true);
        sub.setClassId(template.getClassId());
        phantom.setActiveClass(sub);
        phantom.setClassId(template.getClassId(), true, false);
        phantom.setBaseClass(template.getClassId());
        phantom.setFace(template.getFace());
        phantom.setHairStyle(template.getHair());
        phantom.setHairColor(0);
        phantom.setNameColor(nameColor);
        phantom.setTitleColor(titleColor);
        phantom.setName(template.getName());
        phantom.setDisconnectedTitleColor(phantom.getTitleColor());
        phantom.setTitle(title);
        phantom.setDisconnectedTitle(phantom.getTitle());
        phantom.addExpAndSp(Experience.getExpForLevel(getLevel(template)), 0L);
        phantom.setOnlineStatus(true);
        phantom.entering = false;
        phantom.setAI(getAi(phantom, template.getType()));
        phantom.setRunning();
        phantom.stopAutoSaveTask();
        setEquip(phantom, template.getItemGrade());
        return phantom;
    }

    private void setEquip(final Phantom phantom, final ItemGrade itemGrade) {
        final PhantomEquipTemplate template = PhantomEquipHolder.getInstance().getClassEquip(phantom.getActiveClassId());
        if (template == null) {
            return;
        }
        final List<Integer> items = new ArrayList<>();
        items.add(template.getRandomWeaponId(itemGrade));
        items.add(template.getRandomShieldId(itemGrade));
        items.addAll(template.getRandomArmor(itemGrade).getIds());
        items.stream().mapToInt(id -> id).filter(id -> id != 0).mapToObj(ItemFunctions::createItem).forEach(item -> {
            if (itemGrade != ItemGrade.NONE) {
                item.setEnchantLevel(getEnchant());
            }
            phantom.getInventory().addItem(item);
            phantom.getInventory().equipItem(item);
        });
    }

    private int getLevel(final PhantomTemplate template) {
        if (PhantomConfig.everybodyMaxLevel) {
            return Experience.getMaxLevel();
        }
        switch (template.getItemGrade()) {
            case NONE: {
                return Rnd.get(1, 19);
            }
            case D: {
                return Rnd.get(20, 39);
            }
            case C: {
                return Rnd.get(40, 51);
            }
            case B: {
                return Rnd.get(52, 60);
            }
            case A: {
                return Rnd.get(61, 75);
            }
            case S: {
                return Rnd.get(76, 79);
            }
            default: {
                return 1;
            }
        }
    }

    private int getEnchant() {
        return Rnd.get(PhantomConfig.minEnchant, PhantomConfig.maxEnchant);
    }

    private AbstractPhantomAi getAi(final Phantom phantom, final PhantomAiType type) {
        switch (type) {
            case TOWN: {
                return new PhantomTownAi(phantom);
            }
            case FARM: {
                return new PhantomFarmAi(phantom);
            }
            case PVP: {
                return new PhantomPvpAi(phantom);
            }
            case OLYMPIAD: {
                return new PhantomOlympiadAi(phantom);
            }
            case EVENTS: {
                return new PhantomEventsAi(phantom);
            }
            default: {
                return new PhantomTownAi(phantom);
            }
        }
    }
}
