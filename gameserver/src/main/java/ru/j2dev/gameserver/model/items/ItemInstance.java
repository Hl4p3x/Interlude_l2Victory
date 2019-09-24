package ru.j2dev.gameserver.model.items;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ru.j2dev.commons.util.TroveUtils;
import ru.j2dev.commons.util.concurrent.atomic.AtomicEnumBitFlag;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.dao.ItemsDAO;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.manager.CursedWeaponsManager;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.attachment.ItemAttachment;
import ru.j2dev.gameserver.model.items.listeners.ItemEnchantOptionsListener;
import ru.j2dev.gameserver.network.lineage2.serverpackets.DropItem;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SpawnItem;
import ru.j2dev.gameserver.scripts.Events;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.funcs.Func;
import ru.j2dev.gameserver.stats.funcs.FuncTemplate;
import ru.j2dev.gameserver.tables.PetDataTable;
import ru.j2dev.gameserver.taskmanager.ItemsAutoDestroy;
import ru.j2dev.gameserver.taskmanager.LazyPrecisionTaskManager;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.item.ItemGrade;
import ru.j2dev.gameserver.templates.item.ItemTemplate.ItemClass;
import ru.j2dev.gameserver.templates.item.ItemType;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public final class ItemInstance extends GameObject {
    public static final int[] EMPTY_ENCHANT_OPTIONS = new int[3];
    public static final long MAX_AMMOUNT = 2147483647L;
    public static final int CHARGED_NONE = 0;
    public static final int CHARGED_SOULSHOT = 1;
    public static final int CHARGED_SPIRITSHOT = 1;
    public static final int CHARGED_BLESSED_SPIRITSHOT = 2;
    public static final int FLAG_NO_DROP = 1;
    public static final int FLAG_NO_TRADE = 2;
    public static final int FLAG_NO_TRANSFER = 4;
    public static final int FLAG_NO_CRYSTALLIZE = 8;
    public static final int FLAG_NO_ENCHANT = 16;
    public static final int FLAG_NO_DESTROY = 32;
    private static final long serialVersionUID = 3162753878915133228L;
    private static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();
    private final AtomicEnumBitFlag<ItemStateFlags> _stateFlags;
    private ItemAttributes attrs;
    private int[] _enchantOptions;
    private int _owner_id;
    private int _item_id;
    private long _ammount;
    private ItemLocation _location;
    private int _slot;
    private int _enchant;
    private int _duaration;
    private int _period;
    private int _variation_stat1;
    private int _variation_stat2;
    private int _blessed;
    private int _damaged;
    private int _energy;
    private int _cflags;
    private int _visItemId;
    private ItemTemplate template;
    private boolean isEquipped;
    private long _dropTime;
    private TIntSet _dropPlayers;
    private long _dropTimeOwner;
    private int _chargedSoulshot;
    private int _chargedSpiritshot;
    private boolean _chargedFishtshot;
    private ItemAttachment _attachment;
    private ScheduledFuture<?> _timerTask;

    public ItemInstance(final int objectId) {
        super(objectId);
        attrs = new ItemAttributes();
        _enchantOptions = EMPTY_ENCHANT_OPTIONS;
        _duaration = -1;
        _period = -9999;
        _stateFlags = new AtomicEnumBitFlag<>();
        _dropPlayers = TroveUtils.EMPTY_INT_SET;
        _chargedSoulshot = 0;
        _chargedSpiritshot = 0;
        _chargedFishtshot = false;
    }

    public ItemInstance(final int objectId, final int itemId) {
        super(objectId);
        attrs = new ItemAttributes();
        _enchantOptions = EMPTY_ENCHANT_OPTIONS;
        _duaration = -1;
        _period = -9999;
        _stateFlags = new AtomicEnumBitFlag<>();
        _dropPlayers = TroveUtils.EMPTY_INT_SET;
        _chargedSoulshot = 0;
        _chargedSpiritshot = 0;
        _chargedFishtshot = false;
        setItemId(itemId);
        setDuration(getTemplate().getDurability());
        setPeriodBegin(getTemplate().isTemporal() ? ((int) (System.currentTimeMillis() / 1000L) + getTemplate().getDurability() * 60) : -9999);
        setAgathionEnergy(getTemplate().getAgathionEnergy());
        setLocData(-1);
        setEnchantLevel(0);
    }

    public int getOwnerId() {
        return _owner_id;
    }

    public void setOwnerId(final int ownerId) {
        if (_owner_id == ownerId) {
            return;
        }
        _owner_id = ownerId;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public int getItemId() {
        return _item_id;
    }

    public void setItemId(final int id) {
        _item_id = id;
        template = ItemTemplateHolder.getInstance().getTemplate(id);
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public int getVisibleItemId() {
        if (_visItemId > 0) {
            return _visItemId;
        }
        return getItemId();
    }

    public void setVisibleItemId(final int visItemId) {
        _visItemId = visItemId;
    }

    public long getCount() {
        return _ammount;
    }

    public void setCount(long count) {
        if (count < 0L) {
            count = 0L;
        }
        if (isStackable() && count > 2147483647L) {
            _ammount = 2147483647L;
            getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
            return;
        }
        if (!isStackable() && count > 1L) {
            _ammount = 1L;
            getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
            return;
        }
        if (_ammount == count) {
            return;
        }
        _ammount = count;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public AtomicEnumBitFlag<ItemStateFlags> getItemStateFlag() {
        return _stateFlags;
    }

    public int getEnchantLevel() {
        return _enchant;
    }

    public void setEnchantLevel(final int enchantLevel) {
        final int old = _enchant;
        _enchant = enchantLevel;
        if (old != _enchant && getTemplate().getEnchantOptions().size() > 0) {
            final Player player = GameObjectsStorage.getPlayer(getOwnerId());
            if (isEquipped() && player != null) {
                ItemEnchantOptionsListener.getInstance().onUnequip(getEquipSlot(), this, player);
            }
            final int[] enchantOptions = getTemplate().getEnchantOptions().get(_enchant);
            _enchantOptions = ((enchantOptions == null) ? EMPTY_ENCHANT_OPTIONS : enchantOptions);
            if (isEquipped() && player != null) {
                ItemEnchantOptionsListener.getInstance().onEquip(getEquipSlot(), this, player);
            }
        }
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public String getLocName() {
        return _location.name();
    }

    public void setLocName(final String loc) {
        setLocation(ItemLocation.valueOf(loc));
    }

    public ItemLocation getLocation() {
        return _location;
    }

    public void setLocation(final ItemLocation loc) {
        if (_location == loc) {
            return;
        }
        _location = loc;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public int getLocData() {
        return _slot;
    }

    public void setLocData(final int slot) {
        if (_slot == slot) {
            return;
        }
        _slot = slot;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public int getBlessed() {
        return _blessed;
    }

    public void setBlessed(final int val) {
        _blessed = val;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public int getDamaged() {
        return _damaged;
    }

    public void setDamaged(final int val) {
        _damaged = val;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public int getCustomFlags() {
        return _cflags;
    }

    public void setCustomFlags(final int flags) {
        if (_cflags == flags) {
            return;
        }
        _cflags = flags;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public ItemAttributes getAttributes() {
        return attrs;
    }

    public void setAttributes(final ItemAttributes attrs) {
        this.attrs = attrs;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public int getDuration() {
        if (!isShadowItem()) {
            return -1;
        }
        return _duaration;
    }

    public void setDuration(final int duration) {
        _duaration = duration;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public int getPeriod() {
        if (!isTemporalItem()) {
            return -9999;
        }
        return _period - (int) (System.currentTimeMillis() / 1000L);
    }

    public int getPeriodBegin() {
        if (!isTemporalItem()) {
            return -9999;
        }
        return _period;
    }

    public void setPeriodBegin(final int period) {
        _period = period;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public void startTimer(final Runnable r) {
        _timerTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(r, 0L, 60000L);
    }

    public void stopTimer() {
        if (_timerTask != null) {
            _timerTask.cancel(false);
            _timerTask = null;
        }
    }

    public boolean isEquipable() {
        return template.isEquipable();
    }

    public boolean isEquipped() {
        return isEquipped;
    }

    public void setEquipped(final boolean isEquipped) {
        this.isEquipped = isEquipped;
    }

    public int getBodyPart() {
        return template.getBodyPart();
    }

    public int getEquipSlot() {
        return getLocData();
    }

    public ItemTemplate getTemplate() {
        return template;
    }

    public void setDropTime(final long time) {
        _dropTime = time;
    }

    public long getLastDropTime() {
        return _dropTime;
    }

    public long getDropTimeOwner() {
        return _dropTimeOwner;
    }

    public ItemType getItemType() {
        return template.getItemType();
    }

    public boolean isArmor() {
        return template.isArmor();
    }

    public boolean isAccessory() {
        return template.isAccessory();
    }

    public boolean isWeapon() {
        return template.isWeapon();
    }

    public int getReferencePrice() {
        return template.getReferencePrice();
    }

    public boolean isStackable() {
        return template.isStackable();
    }

    @Override
    public void onAction(final Player player, final boolean shift) {
        if (Events.onAction(player, this, shift)) {
            return;
        }
        if (player.isCursedWeaponEquipped() && CursedWeaponsManager.getInstance().isCursed(getItemId())) {
            return;
        }
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this, null);
    }

    @Override
    public int getActingRange() {
        return 16;
    }

    public boolean isAugmented() {
        return getVariationStat1() != 0 || getVariationStat2() != 0;
    }

    public int getVariationStat1() {
        return _variation_stat1;
    }

    public void setVariationStat1(final int stat) {
        _variation_stat1 = stat;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public int getVariationStat2() {
        return _variation_stat2;
    }

    public void setVariationStat2(final int stat) {
        _variation_stat2 = stat;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public int getChargedSoulshot() {
        return _chargedSoulshot;
    }

    public void setChargedSoulshot(final int type) {
        _chargedSoulshot = type;
    }

    public int getChargedSpiritshot() {
        return _chargedSpiritshot;
    }

    public void setChargedSpiritshot(final int type) {
        _chargedSpiritshot = type;
    }

    public boolean getChargedFishshot() {
        return _chargedFishtshot;
    }

    public void setChargedFishshot(final boolean type) {
        _chargedFishtshot = type;
    }

    public Func[] getStatFuncs() {
        Func[] result = Func.EMPTY_FUNC_ARRAY;
        final List<Func> funcs = new ArrayList<>();
        if (template.getAttachedFuncs().length > 0) {
            for (final FuncTemplate t : template.getAttachedFuncs()) {
                final Func f = t.getFunc(this);
                if (f != null) {
                    funcs.add(f);
                }
            }
        }
        for (final Element e : Element.VALUES) {
            if (isWeapon()) {
                funcs.add(new FuncAttack(e, 0x40, this));
            }
            if (isArmor()) {
                funcs.add(new FuncDefence(e, 0x40, this));
            }
        }
        if (!funcs.isEmpty()) {
            result = funcs.toArray(Func.EMPTY_FUNC_ARRAY);
        }
        funcs.clear();
        return result;
    }

    public boolean isHeroWeapon() {
        return template.isHeroWeapon();
    }

    public boolean canBeDestroyed(final Player player) {
        return (getCustomFlags() & 0x20) != 0x20 && !isHeroWeapon() && (!PetDataTable.isPetControlItem(this) || !player.isMounted()) && player.getPetControlItem() != this && player.getEnchantScroll() != this && !isCursed() && template.isDestroyable() && !player.getInventory().itemIsLocked(this);
    }

    public boolean canBeDropped(final Player player, final boolean pk) {
        return player.isGM() || ((getCustomFlags() & 0x1) != 0x1 && !isShadowItem() && !isTemporalItem() && (!isAugmented() || (pk && Config.DROP_ITEMS_AUGMENTED) || Config.ALT_ALLOW_DROP_AUGMENTED) && ItemFunctions.checkIfCanDiscard(player, this) && template.isDropable() && !player.getInventory().itemIsLocked(this));
    }

    public boolean canBeTraded(final Player player) {
        return !isEquipped() && (player.isGM() || ((getCustomFlags() & 0x2) != 0x2 && !isShadowItem() && !isTemporalItem() && (!isAugmented() || Config.ALT_ALLOW_DROP_AUGMENTED) && ItemFunctions.checkIfCanDiscard(player, this) && template.isTradeable() && !player.getInventory().itemIsLocked(this)));
    }

    public boolean canBeSold(final Player player) {
        return (getCustomFlags() & 0x20) != 0x20 && getItemId() != 57 && template.getReferencePrice() != 0 && !isShadowItem() && !isTemporalItem() && (!isAugmented() || Config.ALT_ALLOW_DROP_AUGMENTED) && !isEquipped() && ItemFunctions.checkIfCanDiscard(player, this) && template.isSellable() && !player.getInventory().itemIsLocked(this);
    }

    public boolean canBeStored(final Player player, final boolean privatewh) {
        return (getCustomFlags() & 0x4) != 0x4 && getTemplate().isStoreable() && (privatewh || (!isShadowItem() && !isTemporalItem())) && (privatewh || !isAugmented() || Config.ALT_ALLOW_DROP_AUGMENTED) && !isEquipped() && ItemFunctions.checkIfCanDiscard(player, this) && (privatewh || template.isTradeable() && !player.getInventory().itemIsLocked(this));
    }

    public boolean canBeCrystallized(final Player player) {
        return (getCustomFlags() & 0x8) != 0x8 && !isShadowItem() && !isTemporalItem() && ItemFunctions.checkIfCanDiscard(player, this) && template.isCrystallizable();
    }

    public boolean canBeEnchanted(final boolean gradeCheck) {
        return (getCustomFlags() & 0x10) != 0x10 && template.canBeEnchanted(gradeCheck);
    }

    public boolean canBeExchanged(final Player player) {
        return (getCustomFlags() & 0x20) != 0x20 && !isShadowItem() && !isTemporalItem() && ItemFunctions.checkIfCanDiscard(player, this) && template.isDestroyable() && !player.getInventory().itemIsLocked(this);
    }

    public boolean isShadowItem() {
        return template.isShadowItem();
    }

    public boolean isTemporalItem() {
        return template.isTemporal();
    }
    public boolean isAgathionItem() {
        return template.isAgathion();
    }

    public boolean isAltSeed() {
        return template.isAltSeed();
    }

    public boolean isCursed() {
        return template.isCursed();
    }

    public void dropToTheGround(final Player lastAttacker, final NpcInstance fromNpc) {
        Creature dropper = fromNpc;
        if (dropper == null) {
            dropper = lastAttacker;
        }
        final Location pos = Location.findAroundPosition(dropper, 128);
        if (lastAttacker != null) {
            _dropPlayers = new TIntHashSet(1, 2.0f);
            for (final Player $member : lastAttacker.getPlayerGroup()) {
                _dropPlayers.add($member.getObjectId());
            }
            _dropTimeOwner = System.currentTimeMillis();
            if (fromNpc != null && fromNpc.isRaid()) {
                _dropTimeOwner += Config.NONOWNER_ITEM_PICKUP_DELAY_RAID;
            } else {
                _dropTimeOwner += Config.NONOWNER_ITEM_PICKUP_DELAY;
            }
        }
        dropMe(dropper, pos);
        if (isHerb()) {
            ItemsAutoDestroy.getInstance().addHerb(this);
        } else if (Config.AUTODESTROY_ITEM_AFTER > 0 && !isCursed()) {
            ItemsAutoDestroy.getInstance().addItem(this);
        }
    }

    public void dropToTheGround(final Creature dropper, final Location dropPos) {
        if (GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex())) {
            dropMe(dropper, dropPos);
        } else {
            dropMe(dropper, dropper.getLoc());
        }
        if (isHerb()) {
            ItemsAutoDestroy.getInstance().addHerb(this);
        } else if (Config.AUTODESTROY_ITEM_AFTER > 0 && !isCursed()) {
            ItemsAutoDestroy.getInstance().addItem(this);
        }
    }

    public void dropToTheGround(final Playable dropper, final Location dropPos) {
        setLocation(ItemLocation.VOID);
        save();
        if (GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex())) {
            dropMe(dropper, dropPos);
        } else {
            dropMe(dropper, dropper.getLoc());
        }
        if (isHerb()) {
            ItemsAutoDestroy.getInstance().addHerb(this);
        } else if (Config.AUTODESTROY_ITEM_AFTER > 0 && !isCursed()) {
            ItemsAutoDestroy.getInstance().addItem(this);
        }
    }

    public void dropMe(final Creature dropper, final Location loc) {
        if (dropper != null) {
            setReflection(dropper.getReflection());
        }
        spawnMe0(loc, dropper);
    }

    public final void pickupMe() {
        decayMe();
        setReflection(ReflectionManager.DEFAULT);
    }

    public ItemClass getItemClass() {
        return template.getItemClass();
    }

    private int getDefence(final Element element) {
        return isArmor() ? getAttributeElementValue(element, true) : 0;
    }

    public int getDefenceFire() {
        return getDefence(Element.FIRE);
    }

    public int getDefenceWater() {
        return getDefence(Element.WATER);
    }

    public int getDefenceWind() {
        return getDefence(Element.WIND);
    }

    public int getDefenceEarth() {
        return getDefence(Element.EARTH);
    }

    public int getDefenceHoly() {
        return getDefence(Element.HOLY);
    }

    public int getDefenceUnholy() {
        return getDefence(Element.UNHOLY);
    }

    public int getAttributeElementValue(final Element element, final boolean withBase) {
        return attrs.getValue(element) + (withBase ? template.getBaseAttributeValue(element) : 0);
    }

    public Element getAttributeElement() {
        return attrs.getElement();
    }

    public int getAttributeElementValue() {
        return attrs.getValue();
    }

    public Element getAttackElement() {
        final Element element = isWeapon() ? getAttributeElement() : Element.NONE;
        if (element == Element.NONE) {
            for (final Element e : Element.VALUES) {
                if (template.getBaseAttributeValue(e) > 0) {
                    return e;
                }
            }
        }
        return element;
    }

    public int getAttackElementValue() {
        return isWeapon() ? getAttributeElementValue(getAttackElement(), true) : 0;
    }

    public void setAttributeElement(final Element element, final int value) {
        attrs.setValue(element, value);
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public boolean isHerb() {
        return getTemplate().isHerb();
    }

    public ItemGrade getCrystalType() {
        return template.getCrystalType();
    }

    @Override
    public String getName() {
        return getTemplate().getName();
    }

    public void save() {
        if (getPlayer() != null && getPlayer().isPhantom()) {
            return;
        }
        _itemsDAO.store(this);
    }

    public void delete() {
        _itemsDAO.delete(this);
    }

    @Override
    public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper) {
        L2GameServerPacket packet;
        if (dropper != null) {
            packet = new DropItem(this, dropper.getObjectId());
        } else {
            packet = new SpawnItem(this);
        }
        return Collections.singletonList(packet);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getTemplate().getItemId());
        sb.append(" ");
        if (getEnchantLevel() > 0) {
            sb.append("+");
            sb.append(getEnchantLevel());
            sb.append(" ");
        }
        sb.append(getTemplate().getName());
        if (!getTemplate().getAdditionalName().isEmpty()) {
            sb.append(" ");
            sb.append("\\").append(getTemplate().getAdditionalName()).append("\\");
        }
        sb.append(" ");
        sb.append("(");
        sb.append(getCount());
        sb.append(")");
        sb.append("[");
        sb.append(getObjectId());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean isItem() {
        return true;
    }

    public ItemAttachment getAttachment() {
        return _attachment;
    }

    public void setAttachment(final ItemAttachment attachment) {
        final ItemAttachment old = _attachment;
        _attachment = attachment;
        if (_attachment != null) {
            _attachment.setItem(this);
        }
        if (old != null) {
            old.setItem(null);
        }
    }

    public int getAgathionEnergy() {
        return _energy;
    }

    public void setAgathionEnergy(final int energy) {
        if (_energy == energy) {
            return;
        }
        _energy = energy;
        getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
    }

    public int[] getEnchantOptions() {
        return _enchantOptions;
    }

    public TIntSet getDropPlayers() {
        return _dropPlayers;
    }

    public enum ItemLocation {
        VOID,
        INVENTORY,
        PAPERDOLL,
        PET_INVENTORY,
        PET_PAPERDOLL,
        WAREHOUSE,
        CLANWH,
        FREIGHT,
        LEASE,
        MAIL
    }

    public class FuncAttack extends Func {
        private final Element element;

        public FuncAttack(final Element element, final int order, final Object owner) {
            super(element.getAttack(), order, owner);
            this.element = element;
        }

        @Override
        public void calc(final Env env) {
            env.value += getAttributeElementValue(element, true);
        }
    }

    public class FuncDefence extends Func {
        private final Element element;

        public FuncDefence(final Element element, final int order, final Object owner) {
            super(element.getDefence(), order, owner);
            this.element = element;
        }

        @Override
        public void calc(final Env env) {
            env.value += getAttributeElementValue(element, true);
        }
    }
}
