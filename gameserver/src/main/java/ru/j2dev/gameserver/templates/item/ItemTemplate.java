package ru.j2dev.gameserver.templates.item;

import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.handler.items.IItemHandler;
import ru.j2dev.gameserver.manager.CursedWeaponsManager;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.StatTemplate;
import ru.j2dev.gameserver.stats.conditions.Condition;
import ru.j2dev.gameserver.stats.funcs.Func;
import ru.j2dev.gameserver.stats.funcs.FuncTemplate;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ItemTemplate extends StatTemplate {
    public static final int ITEM_ID_PC_BANG_POINTS = -100;
    public static final int ITEM_ID_CLAN_REPUTATION_SCORE = -200;
    public static final int ITEM_ID_FAME = -300;
    public static final int ITEM_ID_ADENA = 57;
    public static final int[] ITEM_ID_CASTLE_CIRCLET = {0, 6838, 6835, 6839, 6837, 6840, 6834, 6836, 8182, 8183};
    public static final int ITEM_ID_FORMAL_WEAR = 6408;
    public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
    public static final int TYPE1_SHIELD_ARMOR = 1;
    public static final int TYPE1_OTHER = 2;
    public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
    public static final int TYPE2_WEAPON = 0;
    public static final int TYPE2_SHIELD_ARMOR = 1;
    public static final int TYPE2_ACCESSORY = 2;
    public static final int TYPE2_QUEST = 3;
    public static final int TYPE2_MONEY = 4;
    public static final int TYPE2_OTHER = 5;
    public static final int TYPE2_PET_WOLF = 6;
    public static final int TYPE2_PET_HATCHLING = 7;
    public static final int TYPE2_PET_STRIDER = 8;
    public static final int TYPE2_NODROP = 9;
    public static final int TYPE2_PET_GWOLF = 10;
    public static final int TYPE2_PENDANT = 11;
    public static final int TYPE2_PET_BABY = 12;
    public static final int SLOT_NONE = 0;
    public static final int SLOT_UNDERWEAR = 0x1;
    public static final int SLOT_R_EAR = 0x2;
    public static final int SLOT_L_EAR = 0x4;
    public static final int SLOT_NECK = 0x8;
    public static final int SLOT_R_FINGER = 0x10;
    public static final int SLOT_L_FINGER = 0x20;
    public static final int SLOT_HEAD = 0x40;
    public static final int SLOT_R_HAND = 0x80;
    public static final int SLOT_L_HAND = 0x100;
    public static final int SLOT_GLOVES = 0x200;
    public static final int SLOT_CHEST = 0x400;
    public static final int SLOT_LEGS = 0x800;
    public static final int SLOT_FEET = 0x1000;
    public static final int SLOT_BACK = 0x2000;
    public static final int SLOT_LR_HAND = 0x4000;
    public static final int SLOT_FULL_ARMOR = 0x8000;
    public static final int SLOT_HAIR = 0x10000;
    public static final int SLOT_FORMAL_WEAR = 0x20000;
    public static final int SLOT_DHAIR = 0x40000;
    public static final int SLOT_HAIRALL = 0x80000;

    protected final int _itemId;
    protected final String _name;
    protected final String _addname;
    protected final String _icon;
    protected final String _icon32;
    protected final ItemGrade _crystalType;
    private final ItemClass _class;
    private final int _weight;
    private final int _durability;
    private final int _referencePrice;
    private final int _crystalCount;
    private final boolean _temporal;
    private final boolean _stackable;
    private final boolean _crystallizable;
    private final ReuseType _reuseType;
    private final int _reuseDelay;
    private final int _reuseGroup;
    private final int _agathionEnergy;
    public ItemType type;
    protected int _type1;
    protected int _type2;
    protected int _bodyPart;
    protected Skill[] _skills;
    private int _flags;
    private Skill _enchant4Skill;
    private int[] _baseAttributes;
    private Map<Integer, int[]> _enchantOptions;
    private Condition[] _conditions;
    private IItemHandler _handler;
    private boolean _isShotItem;
    private boolean _isStatDisabled;

    protected ItemTemplate(final StatsSet set) {
        _enchant4Skill = null;
        _baseAttributes = new int[6];
        _enchantOptions = Collections.emptyMap();
        _conditions = Condition.EMPTY_ARRAY;
        _handler = IItemHandler.NULL;
        _isStatDisabled = false;
        _itemId = set.getInteger("item_id");
        _class = set.getEnum("class", ItemClass.class, ItemClass.OTHER);
        _name = set.getString("name");
        _addname = set.getString("add_name", "");
        _icon = set.getString("icon", "");
        _icon32 = "<img src=" + _icon + " width=32 height=32>";
        _weight = set.getInteger("weight", 0);
        _crystallizable = set.getBool("crystallizable", false);
        _stackable = set.getBool("stackable", false);
        _crystalType = set.getEnum("crystal_type", ItemGrade.class, ItemGrade.NONE);
        _durability = set.getInteger("durability", -1);
        _temporal = set.getBool("temporal", false);
        _bodyPart = set.getInteger("bodypart", 0);
        _referencePrice = set.getInteger("price", 0);
        _crystalCount = set.getInteger("crystal_count", 0);
        _reuseType = set.getEnum("reuse_type", ReuseType.class, ReuseType.NORMAL);
        _reuseDelay = set.getInteger("reuse_delay", 0);
        _reuseGroup = set.getInteger("delay_share_group", -_itemId);
        _agathionEnergy = set.getInteger("agathion_energy", 0);
        for (final ItemFlags f : ItemFlags.VALUES) {
            boolean flag = set.getBool(f.name().toLowerCase(), f.getDefaultValue());
            if (_name.contains("{PvP}")) {
                if (f == ItemFlags.TRADEABLE && Config.ALT_PVP_ITEMS_TREDABLE) {
                    flag = true;
                }
                if (f == ItemFlags.ATTRIBUTABLE && Config.ALT_PVP_ITEMS_ATTRIBUTABLE) {
                    flag = true;
                }
                if (f == ItemFlags.AUGMENTABLE && Config.ALT_PVP_ITEMS_AUGMENTABLE) {
                    flag = true;
                }
            }
            if (flag) {
                activeFlag(f);
            }
        }
        _funcTemplates = FuncTemplate.EMPTY_ARRAY;
        _skills = Skill.EMPTY_ARRAY;
    }

    public ItemType getItemType() {
        return type;
    }

    public String getIcon() {
        return _icon;
    }

    public String getIcon32() {
        return _icon32;
    }

    public final int getDurability() {
        return _durability;
    }

    public final boolean isTemporal() {
        return _temporal;
    }

    public final int getItemId() {
        return _itemId;
    }

    public abstract long getItemMask();

    public final int getType2() {
        return _type2;
    }

    public final int getBaseAttributeValue(final Element element) {
        if (element == Element.NONE) {
            return 0;
        }
        return _baseAttributes[element.getId()];
    }

    public void setBaseAtributeElements(final int[] val) {
        _baseAttributes = val;
    }

    public final int getType2ForPackets() {
        int type2 = _type2;
        switch (_type2) {
            case 6:
            case 7:
            case 8:
            case 10:
            case 12: {
                if (_bodyPart == 1024) {
                    type2 = 1;
                    break;
                }
                type2 = 0;
                break;
            }
            case 11: {
                type2 = 2;
                break;
            }
        }
        return type2;
    }

    public final int getWeight() {
        return _weight;
    }

    public final boolean isCrystallizable() {
        return _crystallizable && !isStackable() && getCrystalType() != ItemGrade.NONE && getCrystalCount() > 0;
    }

    public final ItemGrade getCrystalType() {
        return _crystalType;
    }

    public final ItemGrade getItemGrade() {
        return getCrystalType();
    }

    public final int getCrystalCount() {
        return _crystalCount;
    }

    public final String getName() {
        return _name;
    }

    public final String getAdditionalName() {
        return _addname;
    }

    public final int getBodyPart() {
        return _bodyPart;
    }

    public final int getType1() {
        return _type1;
    }

    public final boolean isStackable() {
        return _stackable;
    }

    public final int getReferencePrice() {
        return _referencePrice;
    }

    public boolean isForHatchling() {
        return _type2 == 7;
    }

    public boolean isForStrider() {
        return _type2 == 8;
    }

    public boolean isForWolf() {
        return _type2 == 6;
    }

    public boolean isForPetBaby() {
        return _type2 == 12;
    }

    public boolean isForGWolf() {
        return _type2 == 10;
    }

    public boolean isPendant() {
        return _type2 == 11;
    }

    public boolean isForPet() {
        return _type2 == 11 || _type2 == 7 || _type2 == 6 || _type2 == 8 || _type2 == 10 || _type2 == 12;
    }

    public void attachSkill(final Skill skill) {
        _skills = ArrayUtils.add(_skills, skill);
    }

    public Skill[] getAttachedSkills() {
        return _skills;
    }

    public Skill getFirstSkill() {
        if (_skills.length > 0) {
            return _skills[0];
        }
        return null;
    }

    public Skill getEnchant4Skill() {
        return _enchant4Skill;
    }

    public void setEnchant4Skill(final Skill enchant4Skill) {
        _enchant4Skill = enchant4Skill;
    }

    @Override
    public String toString() {
        return _itemId + " " + _name;
    }

    public boolean isShadowItem() {
        return _durability > 0 && !isTemporal();
    }

    public boolean isSealedItem() {
        return _name.startsWith("Sealed");
    }

    public boolean isAltSeed() {
        return _name.contains("Alternative");
    }

    public ItemClass getItemClass() {
        return _class;
    }

    public boolean isSealStone() {
        switch (_itemId) {
            case 6360:
            case 6361:
            case 6362: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public boolean isAdena() {
        return _itemId == 57;
    }

    public boolean isEquipment() {
        return _type1 != 4;
    }

    public boolean isKeyMatherial() {
        return _class == ItemClass.PIECES;
    }

    public boolean isMaterial() {
        return _class == ItemClass.MATHERIALS;
    }

    public boolean isRecipe() {
        return _class == ItemClass.RECIPIES;
    }

    public boolean isArrow() {
        return type == EtcItemType.ARROW;
    }

    public boolean isBelt() {
        return _bodyPart == 0x10000000;
    }

    public boolean isBracelet() {
        return _bodyPart == 0x100000 || _bodyPart == 0x200000;
    }

    public boolean isUnderwear() {
        return _bodyPart == 0x1;
    }

    public boolean isCloak() {
        return _bodyPart == 0x2000;
    }

    public boolean isTalisman() {
        return _bodyPart == 4194304;
    }

    public boolean isHerb() {
        return type == EtcItemType.HERB;
    }

    public boolean isAttributeCrystal() {
        return _itemId == 9552 || _itemId == 9553 || _itemId == 9554 || _itemId == 9555 || _itemId == 9556 || _itemId == 9557;
    }

    public boolean isAttributeEnergy() {
        return _itemId >= 9564 && _itemId <= 9569;
    }

    public boolean isHeroWeapon() {
        return (_itemId >= 6611 && _itemId <= 6621) || (_itemId >= 9388 && _itemId <= 9390);
    }

    public boolean isCursed() {
        return CursedWeaponsManager.getInstance().isCursed(_itemId);
    }

    public boolean isMercenaryTicket() {
        return type == EtcItemType.MERCENARY_TICKET;
    }

    public boolean isRod() {
        return getItemType() == WeaponType.ROD;
    }

    public boolean isWeapon() {
        return getType2() == 0;
    }

    public boolean isArmor() {
        return getType2() == 1;
    }

    public boolean isAccessory() {
        return getType2() == 2;
    }

    public boolean isQuest() {
        return getType2() == 3;
    }

    public boolean isMageItem() {
        return false;
    }

    public boolean canBeEnchanted(@Deprecated final boolean gradeCheck) {
        return (!gradeCheck || getCrystalType() != ItemGrade.NONE) && !isCursed() && !isQuest() && isEnchantable();
    }

    public boolean isEquipable() {
        return getItemType() == EtcItemType.BAIT || getItemType() == EtcItemType.ARROW || (getBodyPart() != 0 && !(this instanceof EtcItemTemplate));
    }

    public boolean testCondition(final Playable player, final ItemInstance instance) {
        return testCondition(player, instance, true);
    }

    public boolean testCondition(final Playable player, final ItemInstance instance, final boolean showMessage) {
        if (getConditions().length == 0) {
            return true;
        }
        final Env env = new Env();
        env.character = player;
        env.item = instance;
        for (final Condition con : getConditions()) {
            if (!con.test(env)) {
                if (showMessage && con.getSystemMsg() != null) {
                    if (con.getSystemMsg().size() > 0) {
                        player.sendPacket(new SystemMessage2(con.getSystemMsg()).addItemName(getItemId()));
                    } else {
                        player.sendPacket(con.getSystemMsg());
                    }
                }
                return false;
            }
        }
        return true;
    }

    public void addCondition(final Condition condition) {
        _conditions = ArrayUtils.add(_conditions, condition);
    }

    public Condition[] getConditions() {
        return _conditions;
    }

    public boolean isEnchantable() {
        return hasFlag(ItemFlags.ENCHANTABLE);
    }

    public boolean isTradeable() {
        return hasFlag(ItemFlags.TRADEABLE);
    }

    public boolean isDestroyable() {
        return hasFlag(ItemFlags.DESTROYABLE);
    }

    public boolean isDropable() {
        return hasFlag(ItemFlags.DROPABLE);
    }

    public final boolean isSellable() {
        return hasFlag(ItemFlags.SELLABLE);
    }

    public final boolean isAugmentable() {
        return hasFlag(ItemFlags.AUGMENTABLE);
    }

    public final boolean isAttributable() {
        return hasFlag(ItemFlags.ATTRIBUTABLE);
    }

    public final boolean isStoreable() {
        return hasFlag(ItemFlags.STOREABLE);
    }

    public final boolean isFreightable() {
        return hasFlag(ItemFlags.FREIGHTABLE);
    }

    public boolean isCtFFlag() {
        return _itemId == 13560 || _itemId == 13561 || _itemId == 13562 || _itemId == 13563 || _itemId == 13564 || _itemId == 13565 || _itemId == 13566 || _itemId == 13567 || _itemId == 13568;
    }

    public boolean hasFlag(final ItemFlags f) {
        return (_flags & f.mask()) == f.mask();
    }

    private void activeFlag(final ItemFlags f) {
        _flags |= f.mask();
    }

    public IItemHandler getHandler() {
        return _handler;
    }

    public void setHandler(final IItemHandler handler) {
        _handler = handler;
    }

    public boolean isShotItem() {
        return _isShotItem;
    }

    public void setIsShotItem(final boolean isShotItem) {
        _isShotItem = isShotItem;
    }

    public int getReuseDelay() {
        return _reuseDelay;
    }

    public int getReuseGroup() {
        return _reuseGroup;
    }

    public int getDisplayReuseGroup() {
        return (_reuseGroup < 0) ? -1 : _reuseGroup;
    }

    public int getAgathionEnergy() {
        return _agathionEnergy;
    }

    public void addEnchantOptions(final int level, final int[] options) {
        if (_enchantOptions.isEmpty()) {
            _enchantOptions = new HashMap<>();
        }
        _enchantOptions.put(level, options);
    }

    public Map<Integer, int[]> getEnchantOptions() {
        return _enchantOptions;
    }

    public ReuseType getReuseType() {
        return _reuseType;
    }

    public void setStatDisabled(final boolean val) {
        _isStatDisabled = val;
    }

    @Override
    public FuncTemplate[] getAttachedFuncs() {
        if (_isStatDisabled) {
            return FuncTemplate.EMPTY_ARRAY;
        }
        return super.getAttachedFuncs();
    }

    @Override
    public Func[] getStatFuncs(final Object owner) {
        if (_isStatDisabled) {
            return Func.EMPTY_FUNC_ARRAY;
        }
        return super.getStatFuncs(owner);
    }

    public enum ReuseType {
        NORMAL(new SystemMsg[]{SystemMsg.THERE_ARE_S2_SECONDS_REMAINING_IN_S1S_REUSE_TIME, SystemMsg.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_IN_S1S_REUSE_TIME, SystemMsg.THERE_ARE_S2_HOURS_S3_MINUTES_AND_S4_SECONDS_REMAINING_IN_S1S_REUSE_TIME}) {
            @Override
            public long next(final ItemInstance item) {
                return System.currentTimeMillis() + item.getTemplate().getReuseDelay();
            }
        },
        EVERY_DAY_AT_6_30(new SystemMsg[]{SystemMsg.THERE_ARE_S2_SECONDS_REMAINING_FOR_S1S_REUSE_TIME, SystemMsg.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_FOR_S1S_REUSE_TIME, SystemMsg.THERE_ARE_S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_S1S_REUSE_TIME}) {
            @Override
            public long next(final ItemInstance item) {
                final Calendar nextTime = Calendar.getInstance();
                if (nextTime.get(Calendar.HOUR_OF_DAY) > 6 || (nextTime.get(Calendar.HOUR_OF_DAY) == Calendar.FRIDAY && nextTime.get(Calendar.MINUTE) >= 30 && nextTime.get(Calendar.SECOND) >= 0)) {
                    nextTime.add(Calendar.DATE, 1);
                }
                nextTime.set(Calendar.HOUR_OF_DAY, 6);
                nextTime.set(Calendar.MINUTE, 30);
                nextTime.set(Calendar.SECOND, 0);
                return nextTime.getTimeInMillis();
            }
        };

        private final SystemMsg[] _messages;

        ReuseType(final SystemMsg[] msg) {
            _messages = msg;
        }

        public abstract long next(final ItemInstance p0);

        public SystemMsg[] getMessages() {
            return _messages;
        }
    }

    public enum ItemClass {
        ALL,
        WEAPON,
        ARMOR,
        JEWELRY,
        ACCESSORY,
        CONSUMABLE,
        MATHERIALS,
        PIECES,
        RECIPIES,
        SPELLBOOKS,
        MISC,
        OTHER
    }

}
