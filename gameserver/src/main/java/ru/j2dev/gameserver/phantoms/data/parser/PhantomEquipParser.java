package ru.j2dev.gameserver.phantoms.data.parser;

import org.jdom2.Element;
import ru.j2dev.gameserver.phantoms.data.holder.PhantomArmorHolder;
import ru.j2dev.gameserver.phantoms.data.holder.PhantomEquipHolder;
import ru.j2dev.gameserver.phantoms.template.PhantomArmorTemplate;
import ru.j2dev.gameserver.phantoms.template.PhantomEquipTemplate;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.templates.item.ItemGrade;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhantomEquipParser extends AbstractFileParser<PhantomEquipHolder> {
    private static final PhantomEquipParser instance = new PhantomEquipParser();

    private PhantomEquipParser() {
        super(PhantomEquipHolder.getInstance());
    }

    public static PhantomEquipParser getInstance() {
        return PhantomEquipParser.instance;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/phantoms/equipment/class_equip.xml");
    }

    @Override
    protected void readData(final PhantomEquipHolder holder, final Element rootElement) {
        rootElement.getChildren().stream().filter(equipElement -> "class".equals(equipElement.getName())).forEach(equipElement -> {
            final PhantomEquipTemplate template = new PhantomEquipTemplate();
            template.setClassId(Integer.parseInt(equipElement.getAttributeValue("id")));
            for (final Element classElement : equipElement.getChildren()) {
                if ("items".equals(classElement.getName())) {
                    final ItemGrade itemGrade = ItemGrade.valueOf(classElement.getAttributeValue("grade"));
                    final List<Integer> weaponIds = new ArrayList<>();
                    final List<Integer> shieldIds = new ArrayList<>();
                    final List<PhantomArmorTemplate> armorTemplates = new ArrayList<>();
                    for (final Element itemsElement : classElement.getChildren()) {
                        switch (itemsElement.getName()) {
                            case "armor":
                                armorTemplates.add(PhantomArmorHolder.getInstance().getSet(Integer.parseInt(itemsElement.getAttributeValue("set_id"))));
                                break;
                            case "shield":
                                shieldIds.add(Integer.parseInt(itemsElement.getAttributeValue("item_id")));
                                break;
                            default:
                                if (!"weapon".equals(itemsElement.getName())) {
                                    return;
                                }
                                weaponIds.add(Integer.parseInt(itemsElement.getAttributeValue("item_id")));
                                break;
                        }
                    }
                    template.addWeaponList(itemGrade, weaponIds);
                    template.addShieldList(itemGrade, shieldIds);
                    template.addArmorList(itemGrade, armorTemplates);
                    holder.addClassEquip(template.getClassId(), template);
                }
            }
        });
    }
}
