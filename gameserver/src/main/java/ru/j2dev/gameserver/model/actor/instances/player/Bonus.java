package ru.j2dev.gameserver.model.actor.instances.player;

public class Bonus {
    public static final float DEFAULT_RATE_XP = 1.0f;
    public static final float DEFAULT_RATE_SP = 1.0f;
    public static final float DEFAULT_QUEST_REWARD_RATE = 1.0f;
    public static final float DEFAULT_QUEST_DROP_RATE = 1.0f;
    public static final float DEFAULT_DROP_ADENA = 1.0f;
    public static final float DEFAULT_DROP_ITEMS = 1.0f;
    public static final float DEFAULT_DROP_RAID_ITEMS = 1.0f;
    public static final float DEFAULT_DROP_SPOIL = 1.0f;
    public static final float DEFAULT_ENCHANT_ITEM = 1.0f;
    private float rateXp;
    private float rateSp;
    private float questRewardRate;
    private float questDropRate;
    private float dropAdena;
    private float dropItems;
    private float dropRaidItems;
    private float dropSpoil;
    private float enchantItem;
    private long bonusExpire;

    public Bonus() {
        rateXp = 1.0f;
        rateSp = 1.0f;
        questRewardRate = 1.0f;
        questDropRate = 1.0f;
        dropAdena = 1.0f;
        dropItems = 1.0f;
        dropRaidItems = 1.0f;
        dropSpoil = 1.0f;
        enchantItem = 1.0f;
        bonusExpire = 0L;
    }

    public void reset() {
        setRateXp(1.0f);
        setRateSp(1.0f);
        setQuestRewardRate(1.0f);
        setQuestDropRate(1.0f);
        setDropAdena(1.0f);
        setDropItems(1.0f);
        setDropRaidItems(1.0f);
        setDropSpoil(1.0f);
        bonusExpire = 0L;
    }

    public float getRateXp() {
        return rateXp;
    }

    public void setRateXp(final float rateXp) {
        this.rateXp = rateXp;
    }

    public float getRateSp() {
        return rateSp;
    }

    public void setRateSp(final float rateSp) {
        this.rateSp = rateSp;
    }

    public float getQuestRewardRate() {
        return questRewardRate;
    }

    public void setQuestRewardRate(final float questRewardRate) {
        this.questRewardRate = questRewardRate;
    }

    public float getQuestDropRate() {
        return questDropRate;
    }

    public void setQuestDropRate(final float questDropRate) {
        this.questDropRate = questDropRate;
    }

    public float getDropAdena() {
        return dropAdena;
    }

    public void setDropAdena(final float dropAdena) {
        this.dropAdena = dropAdena;
    }

    public float getDropItems() {
        return dropItems;
    }

    public void setDropItems(final float dropItems) {
        this.dropItems = dropItems;
    }

    public float getDropRaidItems() {
        return dropRaidItems;
    }

    public void setDropRaidItems(final float dropRaidItems) {
        this.dropRaidItems = dropRaidItems;
    }

    public float getDropSpoil() {
        return dropSpoil;
    }

    public void setDropSpoil(final float dropSpoil) {
        this.dropSpoil = dropSpoil;
    }

    public float getEnchantItemMul() {
        return enchantItem;
    }

    public void setEnchantItem(final float enchantItem) {
        this.enchantItem = enchantItem;
    }

    public long getBonusExpire() {
        return bonusExpire;
    }

    public void setBonusExpire(final long bonusExpire) {
        this.bonusExpire = bonusExpire;
    }
}
