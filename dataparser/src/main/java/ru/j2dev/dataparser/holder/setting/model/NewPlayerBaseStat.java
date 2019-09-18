package ru.j2dev.dataparser.holder.setting.model;

import ru.j2dev.dataparser.holder.setting.common.ClassID;
import ru.j2dev.dataparser.holder.setting.common.PlayerRace;

/**
 * @author : Camelion
 * @date : 23.08.12 11:30
 * <p/>
 * Базовый конструктор статов для создания новых игроков.
 * <p/>
 * Используется при создании новых персонажей, для пакета
 * NewCharacterSuccess
 */
public final class NewPlayerBaseStat {
    private final PlayerRace race;
    private final ClassID classId;
    // INT
    private final int maxINT;
    private final int baseINT;
    private final int minINT;
    // STR
    private final int maxSTR;
    private final int baseSTR;
    private final int minSTR;
    // CON
    private final int maxCON;
    private final int baseCON;
    private final int minCON;
    // MEN
    private final int maxMEN;
    private final int baseMEN;
    private final int minMEN;
    // DEX
    private final int maxDEX;
    private final int baseDEX;
    private final int minDEX;
    // WIT
    private final int maxWIT;
    private final int baseWIT;
    private final int minWIT;

    public NewPlayerBaseStat(PlayerRace race, ClassID classId, int[] maxStat, int[] baseStat, int[] minStat) {
        this.race = race;
        this.classId = classId;
        // INT
        maxINT = maxStat[0];
        baseINT = baseStat[0];
        minINT = minStat[0];
        // STR
        maxSTR = maxStat[1];
        baseSTR = baseStat[1];
        minSTR = minStat[1];
        // CON
        maxCON = maxStat[2];
        baseCON = baseStat[2];
        minCON = minStat[2];
        // MEN
        maxMEN = maxStat[3];
        baseMEN = baseStat[3];
        minMEN = minStat[3];
        // DEX
        maxDEX = maxStat[4];
        baseDEX = baseStat[4];
        minDEX = minStat[4];
        // WIT
        maxWIT = maxStat[5];
        baseWIT = baseStat[5];
        minWIT = minStat[5];
    }

    public int getRaceId() {
        return race.getId();
    }

    public int getClassId() {
        return classId.getClassId();
    }

    public int getMaxINT() {
        return maxINT;
    }

    public int getBaseINT() {
        return baseINT;
    }

    public int getMinINT() {
        return minINT;
    }

    public int getMaxSTR() {
        return maxSTR;
    }

    public int getBaseSTR() {
        return baseSTR;
    }

    public int getMinSTR() {
        return minSTR;
    }

    public int getMaxCON() {
        return maxCON;
    }

    public int getBaseCON() {
        return baseCON;
    }

    public int getMinCON() {
        return minCON;
    }

    public int getMaxMEN() {
        return maxMEN;
    }

    public int getBaseMEN() {
        return baseMEN;
    }

    public int getMinMEN() {
        return minMEN;
    }

    public int getMaxDEX() {
        return maxDEX;
    }

    public int getBaseDEX() {
        return baseDEX;
    }

    public int getMinDEX() {
        return minDEX;
    }

    public int getMaxWIT() {
        return maxWIT;
    }

    public int getBaseWIT() {
        return baseWIT;
    }

    public int getMinWIT() {
        return minWIT;
    }
}