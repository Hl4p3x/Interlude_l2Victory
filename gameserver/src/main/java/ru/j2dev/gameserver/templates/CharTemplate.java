package ru.j2dev.gameserver.templates;

import ru.j2dev.gameserver.templates.item.WeaponTemplate;

public class CharTemplate {
    private static final int[] EMPTY_ATTRIBUTES = new int[6];
    private static final int[] DEFAULT_DAMAGE_RANGE = {0, 0, 26, 120};

    private int baseSTR;
    private int baseCON;
    private int baseDEX;
    private int baseINT;
    private int baseWIT;
    private int baseMEN;
    private double baseHpMax;
    private double baseCpMax;
    private double baseMpMax;
    private double baseHpReg;
    private double baseMpReg;
    private double baseCpReg;
    private int basePAtk;
    private int baseMAtk;
    private int basePDef;
    private int baseMDef;
    private int basePAtkSpd;
    private int baseMAtkSpd;
    private int baseShldDef;
    private int baseAtkRange;
    private int baseShldRate;
    private int baseCritRate;
    private int baseRunSpd;
    private int baseWalkSpd;
    private int[] baseAttributeAttack;
    private int[] baseAttributeDefence;
    private int[] baseDamageRange;
    private double collisionRadius;
    private double collisionHeight;
    private WeaponTemplate.WeaponType baseAttackType;
    private double physical_hit_modify;
    private double physical_avoid_modify;
    private double hit_time_factor;
    private int safe_height;
    private int baseRndDam;

    public CharTemplate(final StatsSet set) {
        baseSTR = set.getInteger("baseSTR");
        baseCON = set.getInteger("baseCON");
        baseDEX = set.getInteger("baseDEX");
        baseINT = set.getInteger("baseINT");
        baseWIT = set.getInteger("baseWIT");
        baseMEN = set.getInteger("baseMEN");
        setBaseHpMax(set.getDouble("baseHpMax"));
        baseCpMax = set.getDouble("baseCpMax");
        setBaseMpMax(set.getDouble("baseMpMax"));
        baseHpReg = set.getDouble("baseHpReg");
        baseCpReg = set.getDouble("baseCpReg");
        baseMpReg = set.getDouble("baseMpReg");
        setBasePAtk(set.getInteger("basePAtk"));
        setBaseMAtk(set.getInteger("baseMAtk"));
        setBasePDef(set.getInteger("basePDef"));
        setBaseMDef(set.getInteger("baseMDef"));
        basePAtkSpd = set.getInteger("basePAtkSpd");
        baseMAtkSpd = set.getInteger("baseMAtkSpd");
        baseShldDef = set.getInteger("baseShldDef");
        baseAtkRange = set.getInteger("baseAtkRange");
        baseShldRate = set.getInteger("baseShldRate");
        baseCritRate = set.getInteger("baseCritRate");
        baseRunSpd = set.getInteger("baseRunSpd");
        baseWalkSpd = set.getInteger("baseWalkSpd");
        baseAttributeAttack = set.getIntegerArray("baseAttributeAttack", getEmptyAttributes());
        baseAttributeDefence = set.getIntegerArray("baseAttributeDefence", getEmptyAttributes());
        baseDamageRange = set.getIntegerArray("baseDamageRange", getDefaultDamageRange());
        collisionRadius = set.getDouble("collision_radius", 5.0);
        collisionHeight = set.getDouble("collision_height", 5.0);
        hit_time_factor = set.getDouble("hit_time_factor", 1.1);
        safe_height = set.getInteger("safe_height", 300);
        baseRndDam = set.getInteger("baseRndDam", 10);
        physical_hit_modify = set.getDouble("physical_hit_modify", 6);
        physical_avoid_modify = set.getDouble("physical_avoid_modify", 6);
        baseAttackType = WeaponTemplate.WeaponType.valueOf(set.getString("baseAttackType", "FIST").toUpperCase());
    }

    public static int[] getEmptyAttributes() {
        return EMPTY_ATTRIBUTES;
    }

    public static int[] getDefaultDamageRange() {
        return DEFAULT_DAMAGE_RANGE;
    }

    public static StatsSet getEmptyStatsSet() {
        final StatsSet npcDat = new StatsSet();
        npcDat.set("baseSTR", 0);
        npcDat.set("baseCON", 0);
        npcDat.set("baseDEX", 0);
        npcDat.set("baseINT", 0);
        npcDat.set("baseWIT", 0);
        npcDat.set("baseMEN", 0);
        npcDat.set("baseHpMax", 0);
        npcDat.set("baseCpMax", 0);
        npcDat.set("baseMpMax", 0);
        npcDat.set("baseHpReg", 3.e-3f);
        npcDat.set("baseCpReg", 0);
        npcDat.set("baseMpReg", 3.e-3f);
        npcDat.set("basePAtk", 0);
        npcDat.set("baseMAtk", 0);
        npcDat.set("basePDef", 100);
        npcDat.set("baseMDef", 100);
        npcDat.set("basePAtkSpd", 0);
        npcDat.set("baseMAtkSpd", 0);
        npcDat.set("baseShldDef", 0);
        npcDat.set("baseAtkRange", 0);
        npcDat.set("baseShldRate", 0);
        npcDat.set("baseCritRate", 0);
        npcDat.set("baseRunSpd", 0);
        npcDat.set("baseWalkSpd", 0);
        npcDat.set("physical_hit_modify", 6);
        npcDat.set("physical_avoid_modify", 6);
        return npcDat;
    }

    public int[] getBaseDamageRange() {
        return baseDamageRange;
    }

    public int getBaseRndDam() {
        return baseRndDam;
    }

    public double getHitTimeFactor() {
        return hit_time_factor;
    }

    public int getSafeHeight() {
        return safe_height;
    }

    public double getPhysicalHitModify() {
        return physical_hit_modify;
    }

    public double getPhysicalAvoidModify() {
        return physical_avoid_modify;
    }

    public int getNpcId() {
        return 0;
    }

    public int getBaseSTR() {
        return baseSTR;
    }

    public int getBaseCON() {
        return baseCON;
    }

    public int getBaseDEX() {
        return baseDEX;
    }

    public int getBaseINT() {
        return baseINT;
    }

    public int getBaseWIT() {
        return baseWIT;
    }

    public int getBaseMEN() {
        return baseMEN;
    }

    public double getBaseHpMax() {
        return baseHpMax;
    }

    public double getBaseCpMax() {
        return baseCpMax;
    }

    public double getBaseMpMax() {
        return baseMpMax;
    }

    public double getBaseHpReg() {
        return baseHpReg;
    }

    public double getBaseMpReg() {
        return baseMpReg;
    }

    public double getBaseCpReg() {
        return baseCpReg;
    }

    public int getBasePAtk() {
        return basePAtk;
    }

    public int getBaseMAtk() {
        return baseMAtk;
    }

    public int getBasePDef() {
        return basePDef;
    }

    public int getBaseMDef() {
        return baseMDef;
    }

    public int getBasePAtkSpd() {
        return basePAtkSpd;
    }

    public int getBaseMAtkSpd() {
        return baseMAtkSpd;
    }

    public int getBaseShldDef() {
        return baseShldDef;
    }

    public int getBaseAtkRange() {
        return baseAtkRange;
    }

    public int getBaseShldRate() {
        return baseShldRate;
    }

    public int getBaseCritRate() {
        return baseCritRate;
    }

    public int getBaseRunSpd() {
        return baseRunSpd;
    }

    public int getBaseWalkSpd() {
        return baseWalkSpd;
    }

    public int[] getBaseAttributeAttack() {
        return baseAttributeAttack;
    }

    public int[] getBaseAttributeDefence() {
        return baseAttributeDefence;
    }

    public double getCollisionRadius() {
        return collisionRadius;
    }

    public double getCollisionHeight() {
        return collisionHeight;
    }

    public WeaponTemplate.WeaponType getBaseAttackType() {
        return baseAttackType;
    }

    public void setBaseHpMax(double baseHpMax) {
        this.baseHpMax = baseHpMax;
    }

    public void setBaseMpMax(double baseMpMax) {
        this.baseMpMax = baseMpMax;
    }

    public void setBasePAtk(int basePAtk) {
        this.basePAtk = basePAtk;
    }

    public void setBaseMAtk(int baseMAtk) {
        this.baseMAtk = baseMAtk;
    }

    public void setBasePDef(int basePDef) {
        this.basePDef = basePDef;
    }

    public void setBaseMDef(int baseMDef) {
        this.baseMDef = baseMDef;
    }
}
