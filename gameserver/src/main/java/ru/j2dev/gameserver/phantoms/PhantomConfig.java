package ru.j2dev.gameserver.phantoms;

import ru.j2dev.commons.configuration.PropertiesParser;
import ru.j2dev.gameserver.Config;

public class PhantomConfig {
    public static boolean allowPhantoms;
    public static String CONFIG_FILE = "config/fake.ini";
    public static int firstWaveDelay;
    public static int waveRespawn;
    public static int[] phantomSpawnDelayMinMax;
    public static int[] phantomDespawnDelayMinMax;
    public static boolean everybodyMaxLevel;
    public static int minEnchant;
    public static int maxEnchant;
    public static double enchantChance;
    public static long townAiTick;
    public static long townAiInit;
    public static long chatAnswerDelay;
    public static double chatAnswerChance;
    public static double chatspeakChance;
    public static int chanceSpeakAll;
    public static int chanceSpeakShout;
    public static int chanceSpeakTrade;
    public static long chatSpeakDelay;
    public static int randomMoveDistance;
    public static double randomMoveChance;
    public static int[] userActions;
    public static double userActionChance;
    public static int moveToNpcRange;
    public static double moveToNpcChance;
    public static int moveToFarmMonsterRange;

    public static void load() {
        final PropertiesParser properties = Config.load(CONFIG_FILE);
        allowPhantoms = properties.getProperty("allowPhantoms", false);
        firstWaveDelay = properties.getProperty("firstWaveDelay", 1);
        waveRespawn = properties.getProperty("waveRespawn", 5);
        phantomSpawnDelayMinMax = properties.getProperty("phantomSpawnDelayMinMax", new int[]{0, 2});
        phantomDespawnDelayMinMax = properties.getProperty("phantomDespawnDelayMinMax", new int[]{2, 3});
        everybodyMaxLevel = properties.getProperty("everybodyMaxLevel", false);
        minEnchant = properties.getProperty("minEnchant", 3);
        maxEnchant = properties.getProperty("maxEnchant", 16);
        enchantChance = properties.getProperty("enchantChance", 70);
        townAiTick = properties.getProperty("townAiTick", 5000);
        townAiInit = properties.getProperty("townAiInit", 10000);
        chatAnswerDelay = properties.getProperty("chatAnswerDelay", 3000);
        chatAnswerChance = properties.getProperty("chatAnswerChance", 33);
        chatspeakChance = properties.getProperty("chatSpeakChance", 33);
        chanceSpeakAll = properties.getProperty("chanceSpeakTypeAll", 5);
        chanceSpeakShout = properties.getProperty("chanceSpeakTypeShout", 5);
        chanceSpeakTrade = properties.getProperty("chanceSpeakTypeTrade", 1);
        chatSpeakDelay = properties.getProperty("chatSpeakDelay", 3000);
        randomMoveDistance = properties.getProperty("randomMoveDistance", 1500);
        randomMoveChance = properties.getProperty("randomMoveChance", 33);
        userActions = properties.getProperty("userActions", new int[]{0});
        userActionChance = properties.getProperty("userActionChance", 33);
        moveToFarmMonsterRange = properties.getProperty("moveToFarmMonsterRange", 1500);
        moveToNpcRange = properties.getProperty("moveToNpcRange", 1500);
        moveToNpcChance = properties.getProperty("moveToNpcChance", 50);
    }
}
