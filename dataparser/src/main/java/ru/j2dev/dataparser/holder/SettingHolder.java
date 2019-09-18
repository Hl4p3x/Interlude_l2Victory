package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.setting.*;
import ru.j2dev.dataparser.holder.setting.common.ClassID;
import ru.j2dev.dataparser.holder.setting.common.PlayerRace;
import ru.j2dev.dataparser.holder.setting.model.NewPlayerBaseStat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : Camelion
 * @date : 22.08.12 1:32
 */
public class SettingHolder extends AbstractHolder {
    private static final SettingHolder ourInstance = new SettingHolder();
    @Element(start = "initial_equipment_begin", end = "initial_equipment_end")
    private InitialEquipment initialEquipment;
    @Element(start = "initial_custom_equipment_begin", end = "initial_custom_equipment_end")
    private InitialCustomEquipment initialCustomEquipment;
    @Element(start = "initial_start_point_begin", end = "initial_start_point_end")
    private InitialStartPoint initialStartPoint;
    @Element(start = "restart_point_begin", end = "restart_point_end")
    private RestartPoint restartPoint;
    @Element(start = "minimum_stat_begin", end = "minimum_stat_end")
    private MinimumStat minimumStat;
    @Element(start = "maximum_stat_begin", end = "maximum_stat_end")
    private MaximumStat maximumStat;
    @Element(start = "recommended_stat_begin", end = "recommended_stat_end")
    private RecommendedStat recommendedStat;
    @Element(start = "olympiad_arena_begin", end = "olympiad_arena_end")
    private OlympiadArena olympiadArena;
    @Element(start = "olympiad_general_setting_begin", end = "olympiad_general_setting_end")
    private OlympiadGeneralSetting olympiadGeneralSetting;
    @Element(start = "hero_general_setting_start", end = "hero_general_setting_end")
    private HeroGeneralSetting heroGeneralSetting;
    @Element(start = "pvpmatch_setting_start", end = "pvpmatch_setting_end")
    private PVPMatchSetting pvpMatchSetting;
    @Element(start = "cleft_setting_begin", end = "cleft_setting_end")
    private CleftSetting cleftSetting;
    // Базовые параметры для новых игроков
    private final List<NewPlayerBaseStat> newPlayerBaseStats;

    private SettingHolder() {
        newPlayerBaseStats = new ArrayList<>();
    }

    public static SettingHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return newPlayerBaseStats.size();
    }

    @Override
    public void afterParsing() {
        super.afterParsing();
        // Создание базовых конструкторов
        // Человек-воин
        newPlayerBaseStats.add(new NewPlayerBaseStat(PlayerRace.human, ClassID.fighter, maximumStat.getFor(PlayerRace.human, false), recommendedStat.getFor(PlayerRace.human, ClassID.fighter), minimumStat.getFor(PlayerRace.human, false)));
        // Человек-маг
        newPlayerBaseStats.add(new NewPlayerBaseStat(PlayerRace.human, ClassID.mage, maximumStat.getFor(PlayerRace.human, true), recommendedStat.getFor(PlayerRace.human, ClassID.mage), minimumStat.getFor(PlayerRace.human, true)));
        // Светлый эльф-воин
        newPlayerBaseStats.add(new NewPlayerBaseStat(PlayerRace.elf, ClassID.elven_fighter, maximumStat.getFor(PlayerRace.elf, false), recommendedStat.getFor(PlayerRace.elf, ClassID.elven_fighter), minimumStat.getFor(PlayerRace.elf, false)));
        // Светлый эльф-маг
        newPlayerBaseStats.add(new NewPlayerBaseStat(PlayerRace.elf, ClassID.elven_mage, maximumStat.getFor(PlayerRace.elf, true), recommendedStat.getFor(PlayerRace.elf, ClassID.elven_mage), minimumStat.getFor(PlayerRace.elf, true)));
        // Темный эльф-воин
        newPlayerBaseStats.add(new NewPlayerBaseStat(PlayerRace.darkelf, ClassID.dark_fighter, maximumStat.getFor(PlayerRace.darkelf, false), recommendedStat.getFor(PlayerRace.darkelf, ClassID.dark_fighter), minimumStat.getFor(PlayerRace.darkelf, false)));
        // Темный эльф-маг
        newPlayerBaseStats.add(new NewPlayerBaseStat(PlayerRace.darkelf, ClassID.dark_mage, maximumStat.getFor(PlayerRace.darkelf, true), recommendedStat.getFor(PlayerRace.darkelf, ClassID.dark_mage), minimumStat.getFor(PlayerRace.darkelf, true)));
        // Орк-воин
        newPlayerBaseStats.add(new NewPlayerBaseStat(PlayerRace.orc, ClassID.orc_fighter, maximumStat.getFor(PlayerRace.orc, false), recommendedStat.getFor(PlayerRace.orc, ClassID.orc_fighter), minimumStat.getFor(PlayerRace.orc, false)));
        // Орк-маг
        newPlayerBaseStats.add(new NewPlayerBaseStat(PlayerRace.orc, ClassID.orc_mage, maximumStat.getFor(PlayerRace.orc, true), recommendedStat.getFor(PlayerRace.orc, ClassID.orc_mage), minimumStat.getFor(PlayerRace.orc, true)));
        // Гном
        newPlayerBaseStats.add(new NewPlayerBaseStat(PlayerRace.dwarf, ClassID.dwarven_fighter, maximumStat.getFor(PlayerRace.dwarf, false), recommendedStat.getFor(PlayerRace.dwarf, ClassID.dwarven_fighter), minimumStat.getFor(PlayerRace.dwarf, false)));
        // Кмаэль - мужчина
        newPlayerBaseStats.add(new NewPlayerBaseStat(PlayerRace.kamael, ClassID.kamael_m_soldier, maximumStat.getFor(PlayerRace.kamael, false), recommendedStat.getFor(PlayerRace.kamael, ClassID.kamael_m_soldier), minimumStat.getFor(PlayerRace.kamael, false)));
        // Кмаэль - женщина
        newPlayerBaseStats.add(new NewPlayerBaseStat(PlayerRace.kamael, ClassID.kamael_f_soldier, maximumStat.getFor(PlayerRace.kamael, true), recommendedStat.getFor(PlayerRace.kamael, ClassID.kamael_f_soldier), minimumStat.getFor(PlayerRace.kamael, true)));
    }

    public InitialEquipment getInitialEquipment() {
        return initialEquipment;
    }

    public InitialCustomEquipment getInitialCustomEquipment() {
        return initialCustomEquipment;
    }

    public InitialStartPoint getInitialStartPoint() {
        return initialStartPoint;
    }

    public RestartPoint getRestartPoint() {
        return restartPoint;
    }

    public RecommendedStat getRecommendedStat() {
        return recommendedStat;
    }

    public OlympiadArena getOlympiadArena() {
        return olympiadArena;
    }

    public OlympiadGeneralSetting getOlympiadGeneralSetting() {
        return olympiadGeneralSetting;
    }

    public HeroGeneralSetting getHeroGeneralSetting() {
        return heroGeneralSetting;
    }

    public CleftSetting getCleftSetting() {
        return cleftSetting;
    }

    public List<NewPlayerBaseStat> getNewPlayerBaseStats() {
        return newPlayerBaseStats;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }
}