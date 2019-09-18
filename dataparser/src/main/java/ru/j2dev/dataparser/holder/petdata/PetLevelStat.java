package ru.j2dev.dataparser.holder.petdata;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.value.DoubleValue;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.LongValue;

/**
 * @author KilRoy
 * Все значения указаны "на уровень".
 */
public class PetLevelStat {
    @IntValue
    private int level; // Текущий уровень
    @IntValue
    private int max_meal; // Максимальное значение еды
    @LongValue
    private long exp; // Показатель экспы
    @IntValue
    private int get_exp_type; // Количество получаемого опыта в процентах(сколько получает пет)
    @IntValue
    private int consume_meal_in_battle; // Потребление еды в тик, в режиме боя
    @IntValue
    private int consume_meal_in_normal; // Потребление еды в тик, в нормальном(стэндби) режиме
    @DoubleValue
    private double org_pattack; // Базовое значение pAtk
    @DoubleValue
    private double org_pdefend; // Базовое значение pDef
    @DoubleValue
    private double org_mattack; // Базовое значение mAtk
    @DoubleValue
    private double org_mdefend; // Базовое значение mDef
    @DoubleValue
    private double org_hp; // Базовое значение hp
    @DoubleValue
    private double org_mp; // Базовое значение mp
    @DoubleValue
    private double org_hp_regen; // Базовый реген hp
    @DoubleValue
    private double org_mp_regen; // Базовый генер mp
    @IntValue
    private int consume_meal_in_battle_on_ride; // Базовый параметр потребления еды при моунте в режиме боя
    @IntValue
    private int consume_meal_in_normal_on_ride; // Базовый параметр потребления еды при моунте
    @IntArray
    private int[] speed_on_ride; // Базовый параметр скорости при моунте
    @IntValue
    private int attack_speed_on_ride; // Базовый параметр скорости атаки при моунте
    @DoubleValue
    private double pattack_on_ride; // Базовый параметр пАтаки при моунте
    @DoubleValue
    private double mattack_on_ride; // Базовый параметр мАтаки при моунте
    @IntArray
    private int[] food; // Потребляемый список еды(вид ID)
    @IntValue
    private int hungry_limit; // Лимит в процентах, после которых пет будет "голодать" и включать все режимы автопотреблений и т.д
    @IntValue
    private int soulshot_count; // Количество используемых соулШотов
    @IntValue
    private int spiritshot_count; // Количество используемых спиритШотов

    public int getLevel() {
        return level;
    }

    public int getMaxMeal() {
        return max_meal;
    }

    public long getExp() {
        return exp;
    }

    public int getGetExpType() {
        return get_exp_type;
    }

    public int getConsumeMealInBattle() {
        return consume_meal_in_battle;
    }

    public int getConsumeMealInNormal() {
        return consume_meal_in_normal;
    }

    public double getPAtk() {
        return org_pattack;
    }

    public double getPDef() {
        return org_pdefend;
    }

    public double getMAtk() {
        return org_mattack;
    }

    public double getMDef() {
        return org_mdefend;
    }

    public double getMaxHp() {
        return org_hp;
    }

    public double getMaxMp() {
        return org_mp;
    }

    public double getHpRegen() {
        return org_hp_regen;
    }

    public double getMpRegen() {
        return org_mp_regen;
    }

    public int getConsumeMealInBattleOnRide() {
        return consume_meal_in_battle_on_ride;
    }

    public int getConsumeMealInNormalOnRide() {
        return consume_meal_in_normal_on_ride;
    }

    public int[] getSpeedOnRide() {
        return speed_on_ride;
    }

    public int getAttackSpeedOnRide() {
        return attack_speed_on_ride;
    }

    public double getPAttackOnRide() {
        return pattack_on_ride;
    }

    public double getMAttackOnRide() {
        return mattack_on_ride;
    }

    public int[] getFood() {
        return food;
    }

    public int getHungryLimit() {
        return hungry_limit;
    }

    public int getSoulShotCount() {
        return soulshot_count;
    }

    public int getSpiritShotCount() {
        return spiritshot_count;
    }
}