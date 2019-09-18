package ru.j2dev.gameserver.model.instances;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.base.BaseStats;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.PetInventory;
import ru.j2dev.gameserver.model.items.attachment.FlagItemAttachment;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.InventoryUpdate;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.tables.PetDataTable;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Future;

public class PetInstance extends Summon {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetInstance.class);
    private static final int DELUXE_FOOD_FOR_STRIDER = 5169;
    protected final PetInventory _inventory;
    private final int _controlItemObjId;
    protected PetData _data;
    private int _curFed;
    private Future<?> _feedTask;
    private int _level;
    private boolean _respawned;
    private int lostExp;

    public PetInstance(final int objectId, final NpcTemplate template, final Player owner, final ItemInstance control) {
        this(objectId, template, owner, control, 0, 0L);
    }

    public PetInstance(final int objectId, final NpcTemplate template, final Player owner, final ItemInstance control, final int currentLevel, final long exp) {
        super(objectId, template, owner);
        _controlItemObjId = control.getObjectId();
        _exp = exp;
        _level = control.getEnchantLevel();
        if (_level <= 0) {
            if (template.npcId == 12564) {
                _level = owner.getLevel();
            } else {
                _level = template.level;
            }
            _exp = getExpForThisLevel();
        }
        final int minLevel = PetDataTable.getMinLevel(template.npcId);
        if (_level < minLevel) {
            _level = minLevel;
        }
        if (_exp < getExpForThisLevel()) {
            _exp = getExpForThisLevel();
        }
        while (_exp >= getExpForNextLevel() && _level < Experience.getMaxLevel()) {
            ++_level;
        }
        while (_exp < getExpForThisLevel() && _level > minLevel) {
            --_level;
        }
        _data = PetDataTable.getInstance().getInfo(template.npcId, _level);
        _inventory = new PetInventory(this);
    }

    public static PetInstance restore(final ItemInstance control, final NpcTemplate template, final Player owner) {
        PetInstance pet;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT objId, name, level, curHp, curMp, exp, sp, fed FROM pets WHERE item_obj_id=?");
            statement.setInt(1, control.getObjectId());
            rset = statement.executeQuery();
            if (!rset.next()) {
                if (PetDataTable.isBabyPet(template.getNpcId()) || PetDataTable.isImprovedBabyPet(template.getNpcId())) {
                    pet = new PetBabyInstance(IdFactory.getInstance().getNextId(), template, owner, control);
                } else {
                    pet = new PetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
                }
                return pet;
            }
            if (PetDataTable.isBabyPet(template.getNpcId()) || PetDataTable.isImprovedBabyPet(template.getNpcId())) {
                pet = new PetBabyInstance(rset.getInt("objId"), template, owner, control, rset.getInt("level"), rset.getLong("exp"));
            } else {
                pet = new PetInstance(rset.getInt("objId"), template, owner, control, rset.getInt("level"), rset.getLong("exp"));
            }
            pet.setRespawned(true);
            final String name = rset.getString("name");
            pet.setName((name == null || name.isEmpty()) ? template.name : name);
            pet.setCurrentHpMp(rset.getDouble("curHp"), rset.getInt("curMp"), true);
            pet.setCurrentCp(pet.getMaxCp());
            pet.setSp(rset.getInt("sp"));
            pet.setCurrentFed(rset.getInt("fed"));
        } catch (Exception e) {
            LOGGER.error("Could not restore Pet data from item: " + control + "!", e);
            return null;
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return pet;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        startFeed(false);
    }

    @Override
    protected void onDespawn() {
        super.onSpawn();
        stopFeed();
    }

    public boolean tryFeedItem(final ItemInstance item) {
        if (item == null) {
            return false;
        }
        final boolean deluxFood = PetDataTable.isStrider(getNpcId()) && item.getItemId() == DELUXE_FOOD_FOR_STRIDER;
        if (getFoodId() != item.getItemId() && !deluxFood) {
            return false;
        }
        final int newFed = Math.min(getMaxFed(), getCurrentFed() + Math.max(getMaxFed() * getAddFed() * (deluxFood ? 2 : 1) / 100, 1));
        if (getCurrentFed() != newFed && getInventory().destroyItem(item, 1L)) {
            getPlayer().sendPacket(new SystemMessage(1527).addItemName(item.getItemId()));
            setCurrentFed(newFed);
            sendStatusUpdate();
        }
        return true;
    }

    public boolean tryFeed() {
        ItemInstance food = getInventory().getItemByItemId(getFoodId());
        if (food == null && PetDataTable.isStrider(getNpcId())) {
            food = getInventory().getItemByItemId(DELUXE_FOOD_FOR_STRIDER);
        }
        return tryFeedItem(food);
    }

    @Override
    public void addExpAndSp(final long addToExp, final long addToSp) {
        final Player owner = getPlayer();
        _exp += addToExp;
        _sp += (int) addToSp;
        if (_exp > getMaxExp()) {
            _exp = getMaxExp();
        }
        if (addToExp > 0L || addToSp > 0L) {
            owner.sendPacket(new SystemMessage(1014).addNumber(addToExp));
        }
        final int old_level = _level;
        while (_exp >= getExpForNextLevel() && _level < Experience.getMaxLevel()) {
            ++_level;
        }
        while (_exp < getExpForThisLevel() && _level > getMinLevel()) {
            --_level;
        }
        if (old_level < _level) {
            owner.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2PetInstance.PetLevelUp", owner).addNumber(_level));
            broadcastPacket(new SocialAction(getObjectId(), 15));
            setCurrentHpMp(getMaxHp(), getMaxMp());
        }
        if (old_level != _level) {
            updateControlItem();
            updateData();
        }
        if (addToExp > 0L || addToSp > 0L) {
            sendStatusUpdate();
        }
    }

    @Override
    public boolean consumeItem(final int itemConsumeId, final long itemCount) {
        return getPlayer().getInventory().destroyItemByItemId(itemConsumeId, itemCount);
    }

    private void deathPenalty() {
        if (isInZoneBattle()) {
            return;
        }
        final int lvl = getLevel();
        final double percentLost = -0.07 * lvl + 6.5;
        lostExp = (int) Math.round((getExpForNextLevel() - getExpForThisLevel()) * percentLost / 100.0);
        addExpAndSp(-lostExp, 0L);
    }

    private void destroyControlItem() {
        final Player owner = getPlayer();
        if (getControlItemObjId() == 0) {
            return;
        }
        if (!owner.getInventory().destroyItemByObjectId(getControlItemObjId(), 1L)) {
            return;
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
            statement.setInt(1, getControlItemObjId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("could not delete pet:" + e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    @Override
    protected void onDeath(final Creature killer) {
        super.onDeath(killer);
        final Player owner = getPlayer();
        owner.sendPacket(Msg.THE_PET_HAS_BEEN_KILLED_IF_YOU_DO_NOT_RESURRECT_IT_WITHIN_24_HOURS_THE_PETS_BODY_WILL_DISAPPEAR_ALONG_WITH_ALL_THE_PETS_ITEMS);
        startDecay(86400000L);
        stopFeed();
        deathPenalty();
    }

    @Override
    public void doPickupItem(final GameObject object) {
        final Player owner = getPlayer();
        stopMove();
        if (!object.isItem()) {
            return;
        }
        final ItemInstance item = (ItemInstance) object;
        if (item.isCursed()) {
            owner.sendPacket(new SystemMessage(56).addItemName(item.getItemId()));
            return;
        }
        synchronized (item) {
            if (!item.isVisible()) {
                return;
            }
            if (item.isHerb()) {
                final Skill[] skills = item.getTemplate().getAttachedSkills();
                if (skills.length > 0) {
                    for (final Skill skill : skills) {
                        altUseSkill(skill, this);
                    }
                }
                item.deleteMe();
                return;
            }
            if (!getInventory().validateWeight(item)) {
                sendPacket(Msg.EXCEEDED_PET_INVENTORYS_WEIGHT_LIMIT);
                return;
            }
            if (!getInventory().validateCapacity(item)) {
                sendPacket(Msg.DUE_TO_THE_VOLUME_LIMIT_OF_THE_PETS_INVENTORY_NO_MORE_ITEMS_CAN_BE_PLACED_THERE);
                return;
            }
            if (!item.getTemplate().getHandler().pickupItem(this, item)) {
                return;
            }
            final FlagItemAttachment attachment = (item.getAttachment() instanceof FlagItemAttachment) ? ((FlagItemAttachment) item.getAttachment()) : null;
            if (attachment != null) {
                return;
            }
            item.pickupMe();
        }
        if (owner.getParty() == null || owner.getParty().getLootDistribution() == 0) {
            Log.LogItem(owner, ItemLog.PetPickup, item);
            getInventory().addItem(item);
            sendChanges();
            broadcastPickUpMsg(item);
            item.pickupMe();
        } else {
            owner.getParty().distributeItem(owner, item, null);
        }
        broadcastPickUpMsg(item);
    }

    public void doRevive(final double percent) {
        restoreExp(percent);
        doRevive();
    }

    @Override
    public void doRevive() {
        stopDecay();
        super.doRevive();
        startFeed(false);
        setRunning();
    }

    @Override
    public int getAccuracy() {
        return (int) calcStat(Stats.ACCURACY_COMBAT, _data.getAccuracy(), null, null);
    }

    @Override
    public ItemInstance getActiveWeaponInstance() {
        return null;
    }

    @Override
    public WeaponTemplate getActiveWeaponItem() {
        return null;
    }

    public ItemInstance getControlItem() {
        final Player owner = getPlayer();
        if (owner == null) {
            return null;
        }
        final int item_obj_id = getControlItemObjId();
        if (item_obj_id == 0) {
            return null;
        }
        return owner.getInventory().getItemByObjectId(item_obj_id);
    }

    @Override
    public int getControlItemObjId() {
        return _controlItemObjId;
    }

    @Override
    public int getCriticalHit(final Creature target, final Skill skill) {
        return (int) calcStat(Stats.CRITICAL_BASE, _data.getCritical(), target, skill);
    }

    @Override
    public int getCurrentFed() {
        return _curFed;
    }

    public void setCurrentFed(final int num) {
        _curFed = Math.min(getMaxFed(), Math.max(0, num));
    }

    @Override
    public int getEvasionRate(final Creature target) {
        return (int) calcStat(Stats.EVASION_RATE, _data.getEvasion(), target, null);
    }

    @Override
    public long getExpForNextLevel() {
        return PetDataTable.getInstance().getInfo(getNpcId(), _level + 1).getExp();
    }

    @Override
    public long getExpForThisLevel() {
        return PetDataTable.getInstance().getInfo(getNpcId(), _level).getExp();
    }

    public int getFoodId() {
        return _data.getFoodId();
    }

    public int getAddFed() {
        return _data.getAddFed();
    }

    @Override
    public PetInventory getInventory() {
        return _inventory;
    }

    @Override
    public long getWearedMask() {
        return _inventory.getWearedMask();
    }

    @Override
    public final int getLevel() {
        return _level;
    }

    public void setLevel(final int level) {
        _level = level;
    }

    @Override
    public double getLevelMod() {
        return (89.0 + getLevel()) / 100.0;
    }

    public int getMinLevel() {
        return _data.getMinLevel();
    }

    public long getMaxExp() {
        return PetDataTable.getInstance().getInfo(getNpcId(), Experience.getMaxLevel() + 1).getExp();
    }

    @Override
    public int getMaxFed() {
        return _data.getFeedMax();
    }

    @Override
    public int getMaxLoad() {
        return (int) calcStat(Stats.MAX_LOAD, _data.getMaxLoad(), null, null);
    }

    @Override
    public int getInventoryLimit() {
        return Config.ALT_PET_INVENTORY_LIMIT;
    }

    @Override
    public int getMaxHp() {
        return (int) calcStat(Stats.MAX_HP, _data.getHP(), null, null);
    }

    @Override
    public int getMaxMp() {
        return (int) calcStat(Stats.MAX_MP, _data.getMP(), null, null);
    }

    @Override
    public int getPAtk(final Creature target) {
        final double mod = BaseStats.STR.calcBonus(this) * getLevelMod();
        return (int) calcStat(Stats.POWER_ATTACK, _data.getPAtk() / mod, target, null);
    }

    @Override
    public int getPDef(final Creature target) {
        final double mod = getLevelMod();
        return (int) calcStat(Stats.POWER_DEFENCE, _data.getPDef() / mod, target, null);
    }

    @Override
    public int getMAtk(final Creature target, final Skill skill) {
        final double ib = BaseStats.INT.calcBonus(this);
        final double lvlb = getLevelMod();
        final double mod = lvlb * lvlb * ib * ib;
        return (int) calcStat(Stats.MAGIC_ATTACK, _data.getMAtk() / mod, target, skill);
    }

    @Override
    public int getMDef(final Creature target, final Skill skill) {
        final double mod = BaseStats.MEN.calcBonus(this) * getLevelMod();
        return (int) calcStat(Stats.MAGIC_DEFENCE, _data.getMDef() / mod, target, skill);
    }

    @Override
    public int getPAtkSpd() {
        return (int) calcStat(Stats.POWER_ATTACK_SPEED, calcStat(Stats.ATK_BASE, _data.getAtkSpeed(), null, null), null, null);
    }

    @Override
    public int getMAtkSpd() {
        return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, _data.getCastSpeed(), null, null);
    }

    @Override
    public int getRunSpeed() {
        return getSpeed(_data.getSpeed());
    }

    @Override
    public int getSoulshotConsumeCount() {
        return PetDataTable.getSoulshots(getNpcId());
    }

    @Override
    public int getSpiritshotConsumeCount() {
        return PetDataTable.getSpiritshots(getNpcId());
    }

    @Override
    public ItemInstance getSecondaryWeaponInstance() {
        return null;
    }

    @Override
    public WeaponTemplate getSecondaryWeaponItem() {
        return null;
    }

    public int getSkillLevel(final int skillId) {
        if (_skills == null || _skills.get(skillId) == null) {
            return -1;
        }
        final int lvl = getLevel();
        return (lvl > 70) ? (7 + (lvl - 70) / 5) : (lvl / 10);
    }

    @Override
    public int getSummonType() {
        return 2;
    }

    @Override
    public NpcTemplate getTemplate() {
        return (NpcTemplate) _template;
    }

    @Override
    public boolean isMountable() {
        return _data.isMountable();
    }

    public boolean isRespawned() {
        return _respawned;
    }

    public void setRespawned(final boolean respawned) {
        _respawned = respawned;
    }

    public void restoreExp(final double percent) {
        if (lostExp != 0) {
            addExpAndSp((long) (lostExp * percent / 100.0), 0L);
            lostExp = 0;
        }
    }

    @Override
    public void setSp(final int sp) {
        _sp = sp;
    }

    public void startFeed(final boolean battleFeed) {
        final boolean first = _feedTask == null;
        stopFeed();
        if (!isDead()) {
            final int feedTime = Math.max(first ? 15000 : 1000, 60000 / (battleFeed ? _data.getFeedBattle() : _data.getFeedNormal()));
            _feedTask = ThreadPoolManager.getInstance().schedule(new FeedTask(), feedTime);
        }
    }

    private void stopFeed() {
        if (_feedTask != null) {
            _feedTask.cancel(false);
            _feedTask = null;
        }
    }

    public void store() {
        if (getControlItemObjId() == 0 || _exp == 0L) {
            return;
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            String req;
            if (!isRespawned()) {
                req = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,objId,item_obj_id) VALUES (?,?,?,?,?,?,?,?,?)";
            } else {
                req = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,fed=?,objId=? WHERE item_obj_id = ?";
            }
            statement = con.prepareStatement(req);
            statement.setString(1, getName().equalsIgnoreCase(getTemplate().name) ? "" : getName());
            statement.setInt(2, _level);
            statement.setDouble(3, getCurrentHp());
            statement.setDouble(4, getCurrentMp());
            statement.setLong(5, _exp);
            statement.setLong(6, _sp);
            statement.setInt(7, _curFed);
            statement.setInt(8, getObjectId());
            statement.setInt(9, _controlItemObjId);
            statement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Could not store pet data!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        _respawned = true;
    }

    @Override
    protected void onDecay() {
        getInventory().store();
        destroyControlItem();
        super.onDecay();
    }

    @Override
    public void unSummon() {
        stopFeed();
        getInventory().store();
        store();
        super.unSummon();
    }

    public void updateControlItem() {
        final ItemInstance controlItem = getControlItem();
        if (controlItem == null) {
            return;
        }
        controlItem.setEnchantLevel(_level);
        controlItem.setDamaged(isDefaultName() ? 0 : 1);
        final Player owner = getPlayer();
        owner.sendPacket(new InventoryUpdate().addModifiedItem(controlItem));
    }

    private void updateData() {
        _data = PetDataTable.getInstance().getInfo(getTemplate().npcId, _level);
    }

    @Override
    public double getExpPenalty() {
        return PetDataTable.getExpPenalty(getTemplate().npcId);
    }

    @Override
    public void displayGiveDamageMessage(final Creature target, final int damage, final boolean crit, final boolean miss, final boolean shld, final boolean magic) {
        final Player owner = getPlayer();
        if (crit) {
            owner.sendPacket(SystemMsg.SUMMONED_MONSTERS_CRITICAL_HIT);
        }
        if (miss) {
            owner.sendPacket(new SystemMessage(43));
        } else {
            owner.sendPacket(new SystemMessage(1015).addNumber(damage));
        }
    }

    @Override
    public void displayReceiveDamageMessage(final Creature attacker, final int damage) {
        final Player owner = getPlayer();
        if (!isDead()) {
            final SystemMessage sm = new SystemMessage(1016);
            if (attacker.isNpc()) {
                sm.addNpcName(((NpcInstance) attacker).getTemplate().npcId);
            } else {
                sm.addString(attacker.getName());
            }
            sm.addNumber((long) damage);
            owner.sendPacket(sm);
        }
    }

    @Override
    public int getFormId() {
        switch (getNpcId()) {
            case 16025:
            case 16037:
            case 16041:
            case 16042: {
                if (getLevel() >= 70) {
                    return 3;
                }
                if (getLevel() >= 65) {
                    return 2;
                }
                if (getLevel() >= 60) {
                    return 1;
                }
                break;
            }
        }
        return 0;
    }

    @Override
    public boolean isPet() {
        return true;
    }

    public boolean isDefaultName() {
        return StringUtils.isEmpty(_name) || getName().equalsIgnoreCase(getTemplate().name);
    }

    @Override
    public int getEffectIdentifier() {
        return 0;
    }

    class FeedTask extends RunnableImpl {
        @Override
        public void runImpl() {
            final Player owner = getPlayer();
            while (getCurrentFed() <= 0.55 * getMaxFed() && tryFeed()) {
            }
            if (getCurrentFed() <= 0.1 * getMaxFed()) {
                owner.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2PetInstance.UnSummonHungryPet", owner));
                unSummon();
                return;
            }
            setCurrentFed(getCurrentFed() - 5);
            sendStatusUpdate();
            startFeed(isInCombat());
        }
    }
}
