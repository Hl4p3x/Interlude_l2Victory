package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.pcparameter.ClassDataInfo;
import ru.j2dev.dataparser.holder.pcparameter.base_parameter.BaseParameterArray;
import ru.j2dev.dataparser.holder.pcparameter.base_parameter.BaseParameterInt;
import ru.j2dev.dataparser.holder.pcparameter.bonus.LevelBonus;
import ru.j2dev.dataparser.holder.pcparameter.bonus.ParameterBonus;
import ru.j2dev.dataparser.holder.setting.common.ClassID;

import java.util.HashMap;
import java.util.Map;

/**
 * @author KilRoy
 */
public class PCParameterHolder extends AbstractHolder {
    private static final PCParameterHolder ourInstance = new PCParameterHolder();
    private final Map<Integer, ClassDataInfo> classDataInfo = new HashMap<>();
    @Element(start = "mage_hp_table_begin", end = "mage_hp_table_end")
    public LevelBonus mageHpTable;
    @Element(start = "mage_mp_table_begin", end = "mage_mp_table_end")
    public LevelBonus mageMpTable;
    @Element(start = "mage_cp_table_begin", end = "mage_cp_table_end")
    public LevelBonus mageCpTable;
    @Element(start = "wizard_hp_table_begin", end = "wizard_hp_table_end")
    public LevelBonus wizardHpTable;
    @Element(start = "wizard_mp_table_begin", end = "wizard_mp_table_end")
    public LevelBonus wizardMpTable;
    @Element(start = "wizard_cp_table_begin", end = "wizard_cp_table_end")
    public LevelBonus wizardCpTable;
    @Element(start = "cleric_hp_table_begin", end = "cleric_hp_table_end")
    public LevelBonus clericHpTable;
    @Element(start = "cleric_mp_table_begin", end = "cleric_mp_table_end")
    public LevelBonus clericMpTable;
    @Element(start = "cleric_cp_table_begin", end = "cleric_cp_table_end")
    public LevelBonus clericCpTable;
    @Element(start = "sorcerer_hp_table_begin", end = "sorcerer_hp_table_end")
    public LevelBonus sorcererHpTable;
    @Element(start = "sorcerer_mp_table_begin", end = "sorcerer_mp_table_end")
    public LevelBonus sorcererMpTable;
    @Element(start = "sorcerer_cp_table_begin", end = "sorcerer_cp_table_end")
    public LevelBonus sorcererCpTable;
    @Element(start = "necromancer_hp_table_begin", end = "necromancer_hp_table_end")
    public LevelBonus necromancerHpTable;
    @Element(start = "necromancer_mp_table_begin", end = "necromancer_mp_table_end")
    public LevelBonus necromancerMpTable;
    @Element(start = "necromancer_cp_table_begin", end = "necromancer_cp_table_end")
    public LevelBonus necromancerCpTable;
    @Element(start = "warlock_hp_table_begin", end = "warlock_hp_table_end")
    public LevelBonus warlockHpTable;
    @Element(start = "warlock_mp_table_begin", end = "warlock_mp_table_end")
    public LevelBonus warlockMpTable;
    @Element(start = "warlock_cp_table_begin", end = "warlock_cp_table_end")
    public LevelBonus warlockCpTable;
    @Element(start = "bishop_hp_table_begin", end = "bishop_hp_table_end")
    public LevelBonus bishopHpTable;
    @Element(start = "bishop_mp_table_begin", end = "bishop_mp_table_end")
    public LevelBonus bishopMpTable;
    @Element(start = "bishop_cp_table_begin", end = "bishop_cp_table_end")
    public LevelBonus bishopCpTable;
    @Element(start = "prophet_hp_table_begin", end = "prophet_hp_table_end")
    public LevelBonus prophetHpTable;
    @Element(start = "prophet_mp_table_begin", end = "prophet_mp_table_end")
    public LevelBonus prophetMpTable;
    @Element(start = "prophet_cp_table_begin", end = "prophet_cp_table_end")
    public LevelBonus prophetCpTable;
    @Element(start = "elven_fighter_hp_table_begin", end = "elven_fighter_hp_table_end")
    public LevelBonus elvenFighterHpTable;
    @Element(start = "elven_fighter_mp_table_begin", end = "elven_fighter_mp_table_end")
    public LevelBonus elvenFighterMpTable;
    @Element(start = "elven_fighter_cp_table_begin", end = "elven_fighter_cp_table_end")
    public LevelBonus elvenFighterCpTable;
    @Element(start = "elven_knight_hp_table_begin", end = "elven_knight_hp_table_end")
    public LevelBonus elvenKnightHpTable;
    @Element(start = "elven_knight_mp_table_begin", end = "elven_knight_mp_table_end")
    public LevelBonus elvenKnightMpTable;
    @Element(start = "elven_knight_cp_table_begin", end = "elven_knight_cp_table_end")
    public LevelBonus elvenKnightCpTable;
    @Element(start = "elven_scout_hp_table_begin", end = "elven_scout_hp_table_end")
    public LevelBonus elvenScoutHpTable;
    @Element(start = "elven_scout_mp_table_begin", end = "elven_scout_mp_table_end")
    public LevelBonus elvenScoutMpTable;
    @Element(start = "elven_scout_cp_table_begin", end = "elven_scout_cp_table_end")
    public LevelBonus elvenScoutCpTable;
    @Element(start = "temple_knight_hp_table_begin", end = "temple_knight_hp_table_end")
    public LevelBonus templeKnightHpTable;
    @Element(start = "temple_knight_mp_table_begin", end = "temple_knight_mp_table_end")
    public LevelBonus templeKnightMpTable;
    @Element(start = "temple_knight_cp_table_begin", end = "temple_knight_cp_table_end")
    public LevelBonus templeKnightCpTable;
    @Element(start = "swordsinger_hp_table_begin", end = "swordsinger_hp_table_end")
    public LevelBonus swordsingerHpTable;
    @Element(start = "swordsinger_mp_table_begin", end = "swordsinger_mp_table_end")
    public LevelBonus swordsingerMpTable;
    @Element(start = "swordsinger_cp_table_begin", end = "swordsinger_cp_table_end")
    public LevelBonus swordsingerCpTable;
    @Element(start = "plain_walker_hp_table_begin", end = "plain_walker_hp_table_end")
    public LevelBonus plainWalkerHpTable;
    @Element(start = "plain_walker_mp_table_begin", end = "plain_walker_mp_table_end")
    public LevelBonus plainWalkerMpTable;
    @Element(start = "plain_walker_cp_table_begin", end = "plain_walker_cp_table_end")
    public LevelBonus plainWalkerCpTable;
    @Element(start = "silver_ranger_hp_table_begin", end = "silver_ranger_hp_table_end")
    public LevelBonus silverRangerHpTable;
    @Element(start = "silver_ranger_mp_table_begin", end = "silver_ranger_mp_table_end")
    public LevelBonus silverRangerMpTable;
    @Element(start = "silver_ranger_cp_table_begin", end = "silver_ranger_cp_table_end")
    public LevelBonus silverRangerCpTable;
    @Element(start = "elven_mage_hp_table_begin", end = "elven_mage_hp_table_end")
    public LevelBonus elvenMageHpTable;
    @Element(start = "elven_mage_mp_table_begin", end = "elven_mage_mp_table_end")
    public LevelBonus elvenMageMpTable;
    @Element(start = "elven_mage_cp_table_begin", end = "elven_mage_cp_table_end")
    public LevelBonus elvenMageCpTable;
    @Element(start = "elven_wizard_hp_table_begin", end = "elven_wizard_hp_table_end")
    public LevelBonus elvenWizardHpTable;
    @Element(start = "elven_wizard_mp_table_begin", end = "elven_wizard_mp_table_end")
    public LevelBonus elvenWizardMpTable;
    @Element(start = "elven_wizard_cp_table_begin", end = "elven_wizard_cp_table_end")
    public LevelBonus elvenWizardCpTable;
    @Element(start = "oracle_hp_table_begin", end = "oracle_hp_table_end")
    public LevelBonus oracleHpTable;
    @Element(start = "oracle_mp_table_begin", end = "oracle_mp_table_end")
    public LevelBonus oracleMpTable;
    @Element(start = "oracle_cp_table_begin", end = "oracle_cp_table_end")
    public LevelBonus oracleCpTable;
    @Element(start = "spellsinger_hp_table_begin", end = "spellsinger_hp_table_end")
    public LevelBonus spellsingerHpTable;
    @Element(start = "spellsinger_mp_table_begin", end = "spellsinger_mp_table_end")
    public LevelBonus spellsingerMpTable;
    @Element(start = "spellsinger_cp_table_begin", end = "spellsinger_cp_table_end")
    public LevelBonus spellsingerCpTable;
    @Element(start = "elemental_summoner_hp_table_begin", end = "elemental_summoner_hp_table_end")
    public LevelBonus elementalSummonerHpTable;
    @Element(start = "elemental_summoner_mp_table_begin", end = "elemental_summoner_mp_table_end")
    public LevelBonus elementalSummonerMpTable;
    @Element(start = "elemental_summoner_cp_table_begin", end = "elemental_summoner_cp_table_end")
    public LevelBonus elementalSummonerCpTable;
    @Element(start = "elder_hp_table_begin", end = "elder_hp_table_end")
    public LevelBonus elderHpTable;
    @Element(start = "elder_mp_table_begin", end = "elder_mp_table_end")
    public LevelBonus elderMpTable;
    @Element(start = "elder_cp_table_begin", end = "elder_cp_table_end")
    public LevelBonus elderCpTable;
    @Element(start = "dark_fighter_hp_table_begin", end = "dark_fighter_hp_table_end")
    public LevelBonus darkFighterHpTable;
    @Element(start = "dark_fighter_mp_table_begin", end = "dark_fighter_mp_table_end")
    public LevelBonus darkFighterMpTable;
    @Element(start = "dark_fighter_cp_table_begin", end = "dark_fighter_cp_table_end")
    public LevelBonus darkFighterCpTable;
    @Element(start = "palus_knight_hp_table_begin", end = "palus_knight_hp_table_end")
    public LevelBonus palusKnightHpTable;
    @Element(start = "palus_knight_mp_table_begin", end = "palus_knight_mp_table_end")
    public LevelBonus palusKnightMpTable;
    @Element(start = "palus_knight_cp_table_begin", end = "palus_knight_cp_table_end")
    public LevelBonus palusKnightCpTable;
    @Element(start = "assasin_hp_table_begin", end = "assasin_hp_table_end")
    public LevelBonus assasinHpTable;
    @Element(start = "assasin_mp_table_begin", end = "assasin_mp_table_end")
    public LevelBonus assasinMpTable;
    @Element(start = "assasin_cp_table_begin", end = "assasin_cp_table_end")
    public LevelBonus assasinCpTable;
    @Element(start = "shillien_knight_hp_table_begin", end = "shillien_knight_hp_table_end")
    public LevelBonus shillienKnightHpTable;
    @Element(start = "shillien_knight_mp_table_begin", end = "shillien_knight_mp_table_end")
    public LevelBonus shillienKnightMpTable;
    @Element(start = "shillien_knight_cp_table_begin", end = "shillien_knight_cp_table_end")
    public LevelBonus shillienKnightCpTable;
    @Element(start = "bladedancer_hp_table_begin", end = "bladedancer_hp_table_end")
    public LevelBonus bladedancerHpTable;
    @Element(start = "bladedancer_mp_table_begin", end = "bladedancer_mp_table_end")
    public LevelBonus bladedancerMpTable;
    @Element(start = "bladedancer_cp_table_begin", end = "bladedancer_cp_table_end")
    public LevelBonus bladedancerCpTable;
    @Element(start = "abyss_walker_hp_table_begin", end = "abyss_walker_hp_table_end")
    public LevelBonus abyssWalkerHpTable;
    @Element(start = "abyss_walker_mp_table_begin", end = "abyss_walker_mp_table_end")
    public LevelBonus abyssWalkerMpTable;
    @Element(start = "abyss_walker_cp_table_begin", end = "abyss_walker_cp_table_end")
    public LevelBonus abyssWalkerCpTable;
    @Element(start = "phantom_ranger_hp_table_begin", end = "phantom_ranger_hp_table_end")
    public LevelBonus phantomRangerHpTable;
    @Element(start = "phantom_ranger_mp_table_begin", end = "phantom_ranger_mp_table_end")
    public LevelBonus phantomRangerMpTable;
    @Element(start = "phantom_ranger_cp_table_begin", end = "phantom_ranger_cp_table_end")
    public LevelBonus phantomRangerCpTable;
    @Element(start = "dark_mage_hp_table_begin", end = "dark_mage_hp_table_end")
    public LevelBonus darkMageHpTable;
    @Element(start = "dark_mage_mp_table_begin", end = "dark_mage_mp_table_end")
    public LevelBonus darkMageMpTable;
    @Element(start = "dark_mage_cp_table_begin", end = "dark_mage_cp_table_end")
    public LevelBonus darkMageCpTable;
    @Element(start = "dark_wizard_hp_table_begin", end = "dark_wizard_hp_table_end")
    public LevelBonus darkWizardHpTable;
    @Element(start = "dark_wizard_mp_table_begin", end = "dark_wizard_mp_table_end")
    public LevelBonus darkWizardMpTable;
    @Element(start = "dark_wizard_cp_table_begin", end = "dark_wizard_cp_table_end")
    public LevelBonus darkWizardCpTable;
    @Element(start = "shillien_oracle_hp_table_begin", end = "shillien_oracle_hp_table_end")
    public LevelBonus shillienOracleHpTable;
    @Element(start = "shillien_oracle_mp_table_begin", end = "shillien_oracle_mp_table_end")
    public LevelBonus shillienOracleMpTable;
    @Element(start = "shillien_oracle_cp_table_begin", end = "shillien_oracle_cp_table_end")
    public LevelBonus shillienOracleCpTable;
    @Element(start = "spellhowler_hp_table_begin", end = "spellhowler_hp_table_end")
    public LevelBonus spellhowlerHpTable;
    @Element(start = "spellhowler_mp_table_begin", end = "spellhowler_mp_table_end")
    public LevelBonus spellhowlerMpTable;
    @Element(start = "spellhowler_cp_table_begin", end = "spellhowler_cp_table_end")
    public LevelBonus spellhowlerCpTable;
    @Element(start = "phantom_summoner_hp_table_begin", end = "phantom_summoner_hp_table_end")
    public LevelBonus phantomSummonerHpTable;
    @Element(start = "phantom_summoner_mp_table_begin", end = "phantom_summoner_mp_table_end")
    public LevelBonus phantomSummonerMpTable;
    @Element(start = "phantom_summoner_cp_table_begin", end = "phantom_summoner_cp_table_end")
    public LevelBonus phantomSummonerCpTable;
    @Element(start = "shillien_elder_hp_table_begin", end = "shillien_elder_hp_table_end")
    public LevelBonus shillienElderHpTable;
    @Element(start = "shillien_elder_mp_table_begin", end = "shillien_elder_mp_table_end")
    public LevelBonus shillienElderMpTable;
    @Element(start = "shillien_elder_cp_table_begin", end = "shillien_elder_cp_table_end")
    public LevelBonus shillienElderCpTable;
    @Element(start = "orc_fighter_hp_table_begin", end = "orc_fighter_hp_table_end")
    public LevelBonus orcFighterHpTable;
    @Element(start = "orc_fighter_mp_table_begin", end = "orc_fighter_mp_table_end")
    public LevelBonus orcFighterMpTable;
    @Element(start = "orc_fighter_cp_table_begin", end = "orc_fighter_cp_table_end")
    public LevelBonus orcFighterCpTable;
    @Element(start = "orc_raider_hp_table_begin", end = "orc_raider_hp_table_end")
    public LevelBonus orcRaiderHpTable;
    @Element(start = "orc_raider_mp_table_begin", end = "orc_raider_mp_table_end")
    public LevelBonus orcRaiderMpTable;
    @Element(start = "orc_raider_cp_table_begin", end = "orc_raider_cp_table_end")
    public LevelBonus orcRaiderCpTable;
    @Element(start = "orc_monk_hp_table_begin", end = "orc_monk_hp_table_end")
    public LevelBonus orcMonkHpTable;
    @Element(start = "orc_monk_mp_table_begin", end = "orc_monk_mp_table_end")
    public LevelBonus orcMonkMpTable;
    @Element(start = "orc_monk_cp_table_begin", end = "orc_monk_cp_table_end")
    public LevelBonus orcMonkCpTable;
    @Element(start = "destroyer_hp_table_begin", end = "destroyer_hp_table_end")
    public LevelBonus destroyerHpTable;
    @Element(start = "destroyer_mp_table_begin", end = "destroyer_mp_table_end")
    public LevelBonus destroyerMpTable;
    @Element(start = "destroyer_cp_table_begin", end = "destroyer_cp_table_end")
    public LevelBonus destroyerCpTable;
    @Element(start = "tyrant_hp_table_begin", end = "tyrant_hp_table_end")
    public LevelBonus tyrantHpTable;
    @Element(start = "tyrant_mp_table_begin", end = "tyrant_mp_table_end")
    public LevelBonus tyrantMpTable;
    @Element(start = "tyrant_cp_table_begin", end = "tyrant_cp_table_end")
    public LevelBonus tyrantCpTable;
    @Element(start = "orc_mage_hp_table_begin", end = "orc_mage_hp_table_end")
    public LevelBonus orcMageHpTable;
    @Element(start = "orc_mage_mp_table_begin", end = "orc_mage_mp_table_end")
    public LevelBonus orcMageMpTable;
    @Element(start = "orc_mage_cp_table_begin", end = "orc_mage_cp_table_end")
    public LevelBonus orcMageCpTable;
    @Element(start = "orc_shaman_hp_table_begin", end = "orc_shaman_hp_table_end")
    public LevelBonus orcShamanHpTable;
    @Element(start = "orc_shaman_mp_table_begin", end = "orc_shaman_mp_table_end")
    public LevelBonus orcShamanMpTable;
    @Element(start = "orc_shaman_cp_table_begin", end = "orc_shaman_cp_table_end")
    public LevelBonus orcShamanCpTable;
    @Element(start = "overlord_hp_table_begin", end = "overlord_hp_table_end")
    public LevelBonus overlordHpTable;
    @Element(start = "overlord_mp_table_begin", end = "overlord_mp_table_end")
    public LevelBonus overlordMpTable;
    @Element(start = "overlord_cp_table_begin", end = "overlord_cp_table_end")
    public LevelBonus overlordCpTable;
    @Element(start = "warcryer_hp_table_begin", end = "warcryer_hp_table_end")
    public LevelBonus warcryerHpTable;
    @Element(start = "warcryer_mp_table_begin", end = "warcryer_mp_table_end")
    public LevelBonus warcryerMpTable;
    @Element(start = "warcryer_cp_table_begin", end = "warcryer_cp_table_end")
    public LevelBonus warcryerCpTable;
    @Element(start = "dwarven_fighter_hp_table_begin", end = "dwarven_fighter_hp_table_end")
    public LevelBonus dwarvenFighterHpTable;
    @Element(start = "dwarven_fighter_mp_table_begin", end = "dwarven_fighter_mp_table_end")
    public LevelBonus dwarvenFighterMpTable;
    @Element(start = "dwarven_fighter_cp_table_begin", end = "dwarven_fighter_cp_table_end")
    public LevelBonus dwarvenFighterCpTable;
    @Element(start = "scavenger_hp_table_begin", end = "scavenger_hp_table_end")
    public LevelBonus scavengerHpTable;
    @Element(start = "scavenger_mp_table_begin", end = "scavenger_mp_table_end")
    public LevelBonus scavengerMpTable;
    @Element(start = "scavenger_cp_table_begin", end = "scavenger_cp_table_end")
    public LevelBonus scavengerCpTable;
    @Element(start = "artisan_hp_table_begin", end = "artisan_hp_table_end")
    public LevelBonus artisanHpTable;
    @Element(start = "artisan_mp_table_begin", end = "artisan_mp_table_end")
    public LevelBonus artisanMpTable;
    @Element(start = "artisan_cp_table_begin", end = "artisan_cp_table_end")
    public LevelBonus artisanCpTable;
    @Element(start = "bounty_hunter_hp_table_begin", end = "bounty_hunter_hp_table_end")
    public LevelBonus bountyHunterHpTable;
    @Element(start = "bounty_hunter_mp_table_begin", end = "bounty_hunter_mp_table_end")
    public LevelBonus bountyHunterMpTable;
    @Element(start = "bounty_hunter_cp_table_begin", end = "bounty_hunter_cp_table_end")
    public LevelBonus bountyHunterCpTable;
    @Element(start = "warsmith_hp_table_begin", end = "warsmith_hp_table_end")
    public LevelBonus warsmithHpTable;
    @Element(start = "warsmith_mp_table_begin", end = "warsmith_mp_table_end")
    public LevelBonus warsmithMpTable;
    @Element(start = "warsmith_cp_table_begin", end = "warsmith_cp_table_end")
    public LevelBonus warsmithCpTable;
    @Element(start = "dreadnought_hp_table_begin", end = "dreadnought_hp_table_end")
    public LevelBonus dreadnoughtHpTable;
    @Element(start = "dreadnought_mp_table_begin", end = "dreadnought_mp_table_end")
    public LevelBonus dreadnoughtMpTable;
    @Element(start = "dreadnought_cp_table_begin", end = "dreadnought_cp_table_end")
    public LevelBonus dreadnoughtCpTable;
    @Element(start = "duelist_hp_table_begin", end = "duelist_hp_table_end")
    public LevelBonus duelistHpTable;
    @Element(start = "duelist_mp_table_begin", end = "duelist_mp_table_end")
    public LevelBonus duelistMpTable;
    @Element(start = "duelist_cp_table_begin", end = "duelist_cp_table_end")
    public LevelBonus duelistCpTable;
    @Element(start = "phoenix_knight_hp_table_begin", end = "phoenix_knight_hp_table_end")
    public LevelBonus phoenixKnightHpTable;
    @Element(start = "phoenix_knight_mp_table_begin", end = "phoenix_knight_mp_table_end")
    public LevelBonus phoenixKnightMpTable;
    @Element(start = "phoenix_knight_cp_table_begin", end = "phoenix_knight_cp_table_end")
    public LevelBonus phoenixKnightCpTable;
    @Element(start = "hell_knight_hp_table_begin", end = "hell_knight_hp_table_end")
    public LevelBonus hellKnightHpTable;
    @Element(start = "hell_knight_mp_table_begin", end = "hell_knight_mp_table_end")
    public LevelBonus hellKnightMpTable;
    @Element(start = "hell_knight_cp_table_begin", end = "hell_knight_cp_table_end")
    public LevelBonus hellKnightCpTable;
    @Element(start = "adventurer_hp_table_begin", end = "adventurer_hp_table_end")
    public LevelBonus adventurerHpTable;
    @Element(start = "adventurer_mp_table_begin", end = "adventurer_mp_table_end")
    public LevelBonus adventurerMpTable;
    @Element(start = "adventurer_cp_table_begin", end = "adventurer_cp_table_end")
    public LevelBonus adventurerCpTable;
    @Element(start = "sagittarius_hp_table_begin", end = "sagittarius_hp_table_end")
    public LevelBonus sagittariusHpTable;
    @Element(start = "sagittarius_mp_table_begin", end = "sagittarius_mp_table_end")
    public LevelBonus sagittariusMpTable;
    @Element(start = "sagittarius_cp_table_begin", end = "sagittarius_cp_table_end")
    public LevelBonus sagittariusCpTable;
    @Element(start = "archmage_hp_table_begin", end = "archmage_hp_table_end")
    public LevelBonus archmageHpTable;
    @Element(start = "archmage_mp_table_begin", end = "archmage_mp_table_end")
    public LevelBonus archmageMpTable;
    @Element(start = "archmage_cp_table_begin", end = "archmage_cp_table_end")
    public LevelBonus archmageCpTable;
    @Element(start = "soultaker_hp_table_begin", end = "soultaker_hp_table_end")
    public LevelBonus soultakerHpTable;
    @Element(start = "soultaker_mp_table_begin", end = "soultaker_mp_table_end")
    public LevelBonus soultakerMpTable;
    @Element(start = "soultaker_cp_table_begin", end = "soultaker_cp_table_end")
    public LevelBonus soultakerCpTable;
    @Element(start = "arcana_lord_hp_table_begin", end = "arcana_lord_hp_table_end")
    public LevelBonus arcanaLordHpTable;
    @Element(start = "arcana_lord_mp_table_begin", end = "arcana_lord_mp_table_end")
    public LevelBonus arcanaLordMpTable;
    @Element(start = "arcana_lord_cp_table_begin", end = "arcana_lord_cp_table_end")
    public LevelBonus arcanaLordCpTable;
    @Element(start = "cardinal_hp_table_begin", end = "cardinal_hp_table_end")
    public LevelBonus cardinalHpTable;
    @Element(start = "cardinal_mp_table_begin", end = "cardinal_mp_table_end")
    public LevelBonus cardinalMpTable;
    @Element(start = "cardinal_cp_table_begin", end = "cardinal_cp_table_end")
    public LevelBonus cardinalCpTable;
    @Element(start = "hierophant_hp_table_begin", end = "hierophant_hp_table_end")
    public LevelBonus hierophantHpTable;
    @Element(start = "hierophant_mp_table_begin", end = "hierophant_mp_table_end")
    public LevelBonus hierophantMpTable;
    @Element(start = "hierophant_cp_table_begin", end = "hierophant_cp_table_end")
    public LevelBonus hierophantCpTable;
    @Element(start = "evas_templar_hp_table_begin", end = "evas_templar_hp_table_end")
    public LevelBonus evasTemplarHpTable;
    @Element(start = "evas_templar_mp_table_begin", end = "evas_templar_mp_table_end")
    public LevelBonus evasTemplarMpTable;
    @Element(start = "evas_templar_cp_table_begin", end = "evas_templar_cp_table_end")
    public LevelBonus evasTemplarCpTable;
    @Element(start = "sword_muse_hp_table_begin", end = "sword_muse_hp_table_end")
    public LevelBonus swordMuseHpTable;
    @Element(start = "sword_muse_mp_table_begin", end = "sword_muse_mp_table_end")
    public LevelBonus swordMuseMpTable;
    @Element(start = "sword_muse_cp_table_begin", end = "sword_muse_cp_table_end")
    public LevelBonus swordMuseCpTable;
    @Element(start = "wind_rider_hp_table_begin", end = "wind_rider_hp_table_end")
    public LevelBonus windRiderHpTable;
    @Element(start = "wind_rider_mp_table_begin", end = "wind_rider_mp_table_end")
    public LevelBonus windRiderMpTable;
    @Element(start = "wind_rider_cp_table_begin", end = "wind_rider_cp_table_end")
    public LevelBonus windRiderCpTable;
    @Element(start = "moonlight_sentinel_hp_table_begin", end = "moonlight_sentinel_hp_table_end")
    public LevelBonus moonlightSentinelHpTable;
    @Element(start = "moonlight_sentinel_mp_table_begin", end = "moonlight_sentinel_mp_table_end")
    public LevelBonus moonlightSentinelMpTable;
    @Element(start = "moonlight_sentinel_cp_table_begin", end = "moonlight_sentinel_cp_table_end")
    public LevelBonus moonlightSentinelCpTable;
    @Element(start = "mystic_muse_hp_table_begin", end = "mystic_muse_hp_table_end")
    public LevelBonus mysticMuseHpTable;
    @Element(start = "mystic_muse_mp_table_begin", end = "mystic_muse_mp_table_end")
    public LevelBonus mysticMuseMpTable;
    @Element(start = "mystic_muse_cp_table_begin", end = "mystic_muse_cp_table_end")
    public LevelBonus mysticMuseCpTable;
    @Element(start = "elemental_master_hp_table_begin", end = "elemental_master_hp_table_end")
    public LevelBonus elementalMasterHpTable;
    @Element(start = "elemental_master_mp_table_begin", end = "elemental_master_mp_table_end")
    public LevelBonus elementalMasterMpTable;
    @Element(start = "elemental_master_cp_table_begin", end = "elemental_master_cp_table_end")
    public LevelBonus elementalMasterCpTable;
    @Element(start = "evas_saint_hp_table_begin", end = "evas_saint_hp_table_end")
    public LevelBonus evasSaintHpTable;
    @Element(start = "evas_saint_mp_table_begin", end = "evas_saint_mp_table_end")
    public LevelBonus evasSaintMpTable;
    @Element(start = "evas_saint_cp_table_begin", end = "evas_saint_cp_table_end")
    public LevelBonus evasSaintCpTable;
    @Element(start = "shillien_templar_hp_table_begin", end = "shillien_templar_hp_table_end")
    public LevelBonus shillienTemplarHpTable;
    @Element(start = "shillien_templar_mp_table_begin", end = "shillien_templar_mp_table_end")
    public LevelBonus shillienTemplarMpTable;
    @Element(start = "shillien_templar_cp_table_begin", end = "shillien_templar_cp_table_end")
    public LevelBonus shillienTemplarCpTable;
    @Element(start = "spectral_dancer_hp_table_begin", end = "spectral_dancer_hp_table_end")
    public LevelBonus spectralDancerHpTable;
    @Element(start = "spectral_dancer_mp_table_begin", end = "spectral_dancer_mp_table_end")
    public LevelBonus spectralDancerMpTable;
    @Element(start = "spectral_dancer_cp_table_begin", end = "spectral_dancer_cp_table_end")
    public LevelBonus spectralDancerCpTable;
    @Element(start = "ghost_hunter_hp_table_begin", end = "ghost_hunter_hp_table_end")
    public LevelBonus ghostHunterHpTable;
    @Element(start = "ghost_hunter_mp_table_begin", end = "ghost_hunter_mp_table_end")
    public LevelBonus ghostHunterMpTable;
    @Element(start = "ghost_hunter_cp_table_begin", end = "ghost_hunter_cp_table_end")
    public LevelBonus ghostHunterCpTable;
    @Element(start = "ghost_sentinel_hp_table_begin", end = "ghost_sentinel_hp_table_end")
    public LevelBonus ghostSentinelHpTable;
    @Element(start = "ghost_sentinel_mp_table_begin", end = "ghost_sentinel_mp_table_end")
    public LevelBonus ghostSentinelMpTable;
    @Element(start = "ghost_sentinel_cp_table_begin", end = "ghost_sentinel_cp_table_end")
    public LevelBonus ghostSentinelCpTable;
    @Element(start = "storm_screamer_hp_table_begin", end = "storm_screamer_hp_table_end")
    public LevelBonus stormScreamerHpTable;
    @Element(start = "storm_screamer_mp_table_begin", end = "storm_screamer_mp_table_end")
    public LevelBonus stormScreamerMpTable;
    @Element(start = "storm_screamer_cp_table_begin", end = "storm_screamer_cp_table_end")
    public LevelBonus stormScreamerCpTable;
    @Element(start = "spectral_master_hp_table_begin", end = "spectral_master_hp_table_end")
    public LevelBonus spectralMasterHpTable;
    @Element(start = "spectral_master_mp_table_begin", end = "spectral_master_mp_table_end")
    public LevelBonus spectralMasterMpTable;
    @Element(start = "spectral_master_cp_table_begin", end = "spectral_master_cp_table_end")
    public LevelBonus spectralMasterCpTable;
    @Element(start = "shillien_saint_hp_table_begin", end = "shillien_saint_hp_table_end")
    public LevelBonus shillienSaintHpTable;
    @Element(start = "shillien_saint_mp_table_begin", end = "shillien_saint_mp_table_end")
    public LevelBonus shillienSaintMpTable;
    @Element(start = "shillien_saint_cp_table_begin", end = "shillien_saint_cp_table_end")
    public LevelBonus shillienSaintCpTable;
    @Element(start = "titan_hp_table_begin", end = "titan_hp_table_end")
    public LevelBonus titanHpTable;
    @Element(start = "titan_mp_table_begin", end = "titan_mp_table_end")
    public LevelBonus titanMpTable;
    @Element(start = "titan_cp_table_begin", end = "titan_cp_table_end")
    public LevelBonus titanCpTable;
    @Element(start = "grand_khavatari_hp_table_begin", end = "grand_khavatari_hp_table_end")
    public LevelBonus grandKhavatariHpTable;
    @Element(start = "grand_khavatari_mp_table_begin", end = "grand_khavatari_mp_table_end")
    public LevelBonus grandKhavatariMpTable;
    @Element(start = "grand_khavatari_cp_table_begin", end = "grand_khavatari_cp_table_end")
    public LevelBonus grandKhavatariCpTable;
    @Element(start = "dominator_hp_table_begin", end = "dominator_hp_table_end")
    public LevelBonus dominatorHpTable;
    @Element(start = "dominator_mp_table_begin", end = "dominator_mp_table_end")
    public LevelBonus dominatorMpTable;
    @Element(start = "dominator_cp_table_begin", end = "dominator_cp_table_end")
    public LevelBonus dominatorCpTable;
    @Element(start = "doomcryer_hp_table_begin", end = "doomcryer_hp_table_end")
    public LevelBonus doomcryerHpTable;
    @Element(start = "doomcryer_mp_table_begin", end = "doomcryer_mp_table_end")
    public LevelBonus doomcryerMpTable;
    @Element(start = "doomcryer_cp_table_begin", end = "doomcryer_cp_table_end")
    public LevelBonus doomcryerCpTable;
    @Element(start = "fortune_seeker_hp_table_begin", end = "fortune_seeker_hp_table_end")
    public LevelBonus fortuneSeekerHpTable;
    @Element(start = "fortune_seeker_mp_table_begin", end = "fortune_seeker_mp_table_end")
    public LevelBonus fortuneSeekerMpTable;
    @Element(start = "fortune_seeker_cp_table_begin", end = "fortune_seeker_cp_table_end")
    public LevelBonus fortuneSeekerCpTable;
    @Element(start = "maestro_hp_table_begin", end = "maestro_hp_table_end")
    public LevelBonus maestroHpTable;
    @Element(start = "maestro_mp_table_begin", end = "maestro_mp_table_end")
    public LevelBonus maestroMpTable;
    @Element(start = "maestro_cp_table_begin", end = "maestro_cp_table_end")
    public LevelBonus maestroCpTable;
    @Element(start = "kamael_m_soldier_hp_table_begin", end = "kamael_m_soldier_hp_table_end")
    public LevelBonus kamaelMSoldierHpTable;
    @Element(start = "kamael_m_soldier_mp_table_begin", end = "kamael_m_soldier_mp_table_end")
    public LevelBonus kamaelMSoldierMpTable;
    @Element(start = "kamael_m_soldier_cp_table_begin", end = "kamael_m_soldier_cp_table_end")
    public LevelBonus kamaelMSoldierCpTable;
    @Element(start = "trooper_hp_table_begin", end = "trooper_hp_table_end")
    public LevelBonus trooperHpTable;
    @Element(start = "trooper_mp_table_begin", end = "trooper_mp_table_end")
    public LevelBonus trooperMpTable;
    @Element(start = "trooper_cp_table_begin", end = "trooper_cp_table_end")
    public LevelBonus trooperCpTable;
    @Element(start = "berserker_hp_table_begin", end = "berserker_hp_table_end")
    public LevelBonus berserkerHpTable;
    @Element(start = "berserker_mp_table_begin", end = "berserker_mp_table_end")
    public LevelBonus berserkerMpTable;
    @Element(start = "berserker_cp_table_begin", end = "berserker_cp_table_end")
    public LevelBonus berserkerCpTable;
    @Element(start = "m_soul_breaker_hp_table_begin", end = "m_soul_breaker_hp_table_end")
    public LevelBonus mSoulBreakerHpTable;
    @Element(start = "m_soul_breaker_mp_table_begin", end = "m_soul_breaker_mp_table_end")
    public LevelBonus mSoulBreakerMpTable;
    @Element(start = "m_soul_breaker_cp_table_begin", end = "m_soul_breaker_cp_table_end")
    public LevelBonus mSoulBreakerCpTable;
    @Element(start = "doombringer_hp_table_begin", end = "doombringer_hp_table_end")
    public LevelBonus doombringerHpTable;
    @Element(start = "doombringer_mp_table_begin", end = "doombringer_mp_table_end")
    public LevelBonus doombringerMpTable;
    @Element(start = "doombringer_cp_table_begin", end = "doombringer_cp_table_end")
    public LevelBonus doombringerCpTable;
    @Element(start = "m_soul_hound_hp_table_begin", end = "m_soul_hound_hp_table_end")
    public LevelBonus mSoulHoundHpTable;
    @Element(start = "m_soul_hound_mp_table_begin", end = "m_soul_hound_mp_table_end")
    public LevelBonus mSoulHoundMpTable;
    @Element(start = "m_soul_hound_cp_table_begin", end = "m_soul_hound_cp_table_end")
    public LevelBonus mSoulHoundCpTable;
    @Element(start = "kamael_f_soldier_hp_table_begin", end = "kamael_f_soldier_hp_table_end")
    public LevelBonus kamaelFSoldierHpTable;
    @Element(start = "kamael_f_soldier_mp_table_begin", end = "kamael_f_soldier_mp_table_end")
    public LevelBonus kamaelFSoldierMpTable;
    @Element(start = "kamael_f_soldier_cp_table_begin", end = "kamael_f_soldier_cp_table_end")
    public LevelBonus kamaelFSoldierCpTable;
    @Element(start = "warder_hp_table_begin", end = "warder_hp_table_end")
    public LevelBonus warderHpTable;
    @Element(start = "warder_mp_table_begin", end = "warder_mp_table_end")
    public LevelBonus warderMpTable;
    @Element(start = "warder_cp_table_begin", end = "warder_cp_table_end")
    public LevelBonus warderCpTable;
    @Element(start = "arbalester_hp_table_begin", end = "arbalester_hp_table_end")
    public LevelBonus arbalesterHpTable;
    @Element(start = "arbalester_mp_table_begin", end = "arbalester_mp_table_end")
    public LevelBonus arbalesterMpTable;
    @Element(start = "arbalester_cp_table_begin", end = "arbalester_cp_table_end")
    public LevelBonus arbalesterCpTable;
    @Element(start = "f_soul_breaker_hp_table_begin", end = "f_soul_breaker_hp_table_end")
    public LevelBonus fSoulBreakerHpTable;
    @Element(start = "f_soul_breaker_mp_table_begin", end = "f_soul_breaker_mp_table_end")
    public LevelBonus fSoulBreakerMpTable;
    @Element(start = "f_soul_breaker_cp_table_begin", end = "f_soul_breaker_cp_table_end")
    public LevelBonus fSoulBreakerCpTable;
    @Element(start = "trickster_hp_table_begin", end = "trickster_hp_table_end")
    public LevelBonus tricksterHpTable;
    @Element(start = "trickster_mp_table_begin", end = "trickster_mp_table_end")
    public LevelBonus tricksterMpTable;
    @Element(start = "trickster_cp_table_begin", end = "trickster_cp_table_end")
    public LevelBonus tricksterCpTable;
    @Element(start = "f_soul_hound_hp_table_begin", end = "f_soul_hound_hp_table_end")
    public LevelBonus fSoulHoundHpTable;
    @Element(start = "f_soul_hound_mp_table_begin", end = "f_soul_hound_mp_table_end")
    public LevelBonus fSoulHoundMpTable;
    @Element(start = "f_soul_hound_cp_table_begin", end = "f_soul_hound_cp_table_end")
    public LevelBonus fSoulHoundCpTable;
    @Element(start = "inspector_hp_table_begin", end = "inspector_hp_table_end")
    public LevelBonus inspectorHpTable;
    @Element(start = "inspector_mp_table_begin", end = "inspector_mp_table_end")
    public LevelBonus inspectorMpTable;
    @Element(start = "inspector_cp_table_begin", end = "inspector_cp_table_end")
    public LevelBonus inspectorCpTable;
    @Element(start = "judicator_hp_table_begin", end = "judicator_hp_table_end")
    public LevelBonus judicatorHpTable;
    @Element(start = "judicator_mp_table_begin", end = "judicator_mp_table_end")
    public LevelBonus judicatorMpTable;
    @Element(start = "judicator_cp_table_begin", end = "judicator_cp_table_end")
    public LevelBonus judicatorCpTable;
    @Element(start = "sigel_knight_hp_table_begin", end = "sigel_knight_hp_table_end")
    public LevelBonus sigelKnightHpTable;
    @Element(start = "sigel_knight_mp_table_begin", end = "sigel_knight_mp_table_end")
    public LevelBonus sigelKnightMpTable;
    @Element(start = "sigel_knight_cp_table_begin", end = "sigel_knight_cp_table_end")
    public LevelBonus sigelKnightCpTable;
    @Element(start = "tir_warrior_hp_table_begin", end = "tir_warrior_hp_table_end")
    public LevelBonus tirWarriorHpTable;
    @Element(start = "tir_warrior_mp_table_begin", end = "tir_warrior_mp_table_end")
    public LevelBonus tirWarriorMpTable;
    @Element(start = "tir_warrior_cp_table_begin", end = "tir_warrior_cp_table_end")
    public LevelBonus tirWarriorCpTable;
    @Element(start = "othel_rogue_hp_table_begin", end = "othel_rogue_hp_table_end")
    public LevelBonus othelRogueHpTable;
    @Element(start = "othel_rogue_mp_table_begin", end = "othel_rogue_mp_table_end")
    public LevelBonus othelRogueMpTable;
    @Element(start = "othel_rogue_cp_table_begin", end = "othel_rogue_cp_table_end")
    public LevelBonus othelRogueCpTable;
    @Element(start = "yr_archer_hp_table_begin", end = "yr_archer_hp_table_end")
    public LevelBonus yrArcherHpTable;
    @Element(start = "yr_archer_mp_table_begin", end = "yr_archer_mp_table_end")
    public LevelBonus yrArcherMpTable;
    @Element(start = "yr_archer_cp_table_begin", end = "yr_archer_cp_table_end")
    public LevelBonus yrArcherCpTable;
    @Element(start = "eolh_healer_hp_table_begin", end = "eolh_healer_hp_table_end")
    public LevelBonus eolhHealerHpTable;
    @Element(start = "eolh_healer_mp_table_begin", end = "eolh_healer_mp_table_end")
    public LevelBonus eolhHealerMpTable;
    @Element(start = "eolh_healer_cp_table_begin", end = "eolh_healer_cp_table_end")
    public LevelBonus eolhHealerCpTable;
    @Element(start = "wynn_summoner_hp_table_begin", end = "wynn_summoner_hp_table_end")
    public LevelBonus wynnSummonerHpTable;
    @Element(start = "wynn_summoner_mp_table_begin", end = "wynn_summoner_mp_table_end")
    public LevelBonus wynnSummonerMpTable;
    @Element(start = "wynn_summoner_cp_table_begin", end = "wynn_summoner_cp_table_end")
    public LevelBonus wynnSummonerCpTable;
    @Element(start = "is_enchanter_hp_table_begin", end = "is_enchanter_hp_table_end")
    public LevelBonus isEnchanterHpTable;
    @Element(start = "is_enchanter_mp_table_begin", end = "is_enchanter_mp_table_end")
    public LevelBonus isEnchanterMpTable;
    @Element(start = "is_enchanter_cp_table_begin", end = "is_enchanter_cp_table_end")
    public LevelBonus isEnchanterCpTable;
    @Element(start = "feoh_wizard_hp_table_begin", end = "feoh_wizard_hp_table_end")
    public LevelBonus feohWizardHpTable;
    @Element(start = "feoh_wizard_mp_table_begin", end = "feoh_wizard_mp_table_end")
    public LevelBonus feohWizardMpTable;
    @Element(start = "feoh_wizard_cp_table_begin", end = "feoh_wizard_cp_table_end")
    public LevelBonus feohWizardCpTable;
    @Element(start = "pc_karma_increase_table_begin", end = "pc_karma_increase_table_end")
    public LevelBonus pc_karma_increase_table;
    @Element(start = "base_physical_attack_begin", end = "base_physical_attack_end")
    private BaseParameterInt basePhysicalAttack;
    @Element(start = "base_critical_begin", end = "base_critical_end")
    private BaseParameterInt baseCritical;
    @Element(start = "base_attack_type_begin", end = "base_attack_type_end")
    private BaseParameterInt baseAttackType;
    @Element(start = "base_attack_speed_begin", end = "base_attack_speed_end")
    private BaseParameterInt baseAttackSpeed;
    @Element(start = "base_defend_begin", end = "base_defend_end")
    private BaseParameterArray baseDefend;
    @Element(start = "base_magic_attack_begin", end = "base_magic_attack_end")
    private BaseParameterInt baseMagicAttack;
    @Element(start = "base_magic_defend_begin", end = "base_magic_defend_end")
    private BaseParameterArray baseMagicDefend;
    @Element(start = "base_can_penetrate_begin", end = "base_can_penetrate_end")
    private BaseParameterInt baseCanPenetrate;
    @Element(start = "base_attack_range_begin", end = "base_attack_range_end")
    private BaseParameterInt baseAttackRange;
    @Element(start = "base_damage_range_begin", end = "base_damage_range_end")
    private BaseParameterArray baseDamageRange;
    @Element(start = "base_rand_dam_begin", end = "base_rand_dam_end")
    private BaseParameterInt baseRandDam;
    @Element(start = "level_bonus_begin", end = "level_bonus_end")
    private LevelBonus levelBonus;
    @Element(start = "str_bonus_begin", end = "str_bonus_end")
    private ParameterBonus strBonus;
    @Element(start = "int_bonus_begin", end = "int_bonus_end")
    private ParameterBonus intBonus;
    @Element(start = "con_bonus_begin", end = "con_bonus_end")
    private ParameterBonus conBonus;
    @Element(start = "men_bonus_begin", end = "men_bonus_end")
    private ParameterBonus menBonus;
    @Element(start = "dex_bonus_begin", end = "dex_bonus_end")
    private ParameterBonus dexBonus;
    @Element(start = "wit_bonus_begin", end = "wit_bonus_end")
    private ParameterBonus witBonus;
    @Element(start = "org_hp_regen_begin", end = "org_hp_regen_end")
    private BaseParameterArray orgHpRegen;
    @Element(start = "org_mp_regen_begin", end = "org_mp_regen_end")
    private BaseParameterArray orgMpRegen;
    @Element(start = "org_cp_regen_begin", end = "org_cp_regen_end")
    private BaseParameterArray orgCpRegen;
    @Element(start = "moving_speed_begin", end = "moving_speed_end")
    private BaseParameterArray movingSpeed;
    @Element(start = "org_jump_begin", end = "org_jump_end")
    private BaseParameterInt orgJump;
    @Element(start = "pc_breath_bonus_table_begin", end = "pc_breath_bonus_table_end")
    private BaseParameterInt pcBreathBonusTable;
    @Element(start = "pc_safe_fall_height_table_begin", end = "pc_safe_fall_height_table_end")
    private BaseParameterInt pcSafeFallHeightTable;
    @Element(start = "pc_collision_box_table_begin", end = "pc_collision_box_table_end")
    private BaseParameterArray pcCollisionBoxTable;
    @Element(start = "fighter_hp_table_begin", end = "fighter_hp_table_end")
    private LevelBonus fighterHpTable;
    @Element(start = "fighter_mp_table_begin", end = "fighter_mp_table_end")
    private LevelBonus fighterMpTable;
    @Element(start = "fighter_cp_table_begin", end = "fighter_cp_table_end")
    private LevelBonus fighterCpTable;
    @Element(start = "warrior_hp_table_begin", end = "warrior_hp_table_end")
    private LevelBonus warriorHpTable;
    @Element(start = "warrior_mp_table_begin", end = "warrior_mp_table_end")
    private LevelBonus warriorMpTable;
    @Element(start = "warrior_cp_table_begin", end = "warrior_cp_table_end")
    private LevelBonus warriorCpTable;
    @Element(start = "knight_hp_table_begin", end = "knight_hp_table_end")
    private LevelBonus knightHpTable;
    @Element(start = "knight_mp_table_begin", end = "knight_mp_table_end")
    private LevelBonus knightMpTable;
    @Element(start = "knight_cp_table_begin", end = "knight_cp_table_end")
    private LevelBonus knightCpTable;
    @Element(start = "rogue_hp_table_begin", end = "rogue_hp_table_end")
    private LevelBonus rogueHpTable;
    @Element(start = "rogue_mp_table_begin", end = "rogue_mp_table_end")
    private LevelBonus rogueMpTable;
    @Element(start = "rogue_cp_table_begin", end = "rogue_cp_table_end")
    private LevelBonus rogueCpTable;
    @Element(start = "warlord_hp_table_begin", end = "warlord_hp_table_end")
    private LevelBonus warlordHpTable;
    @Element(start = "warlord_mp_table_begin", end = "warlord_mp_table_end")
    private LevelBonus warlordMpTable;
    @Element(start = "warlord_cp_table_begin", end = "warlord_cp_table_end")
    private LevelBonus warlordCpTable;
    @Element(start = "gladiator_hp_table_begin", end = "gladiator_hp_table_end")
    private LevelBonus gladiatorHpTable;
    @Element(start = "gladiator_mp_table_begin", end = "gladiator_mp_table_end")
    private LevelBonus gladiatorMpTable;
    @Element(start = "gladiator_cp_table_begin", end = "gladiator_cp_table_end")
    private LevelBonus gladiatorCpTable;
    @Element(start = "paladin_hp_table_begin", end = "paladin_hp_table_end")
    private LevelBonus paladinHpTable;
    @Element(start = "paladin_mp_table_begin", end = "paladin_mp_table_end")
    private LevelBonus paladinMpTable;
    @Element(start = "paladin_cp_table_begin", end = "paladin_cp_table_end")
    private LevelBonus paladinCpTable;
    @Element(start = "dark_avenger_hp_table_begin", end = "dark_avenger_hp_table_end")
    private LevelBonus darkAvengerHpTable;
    @Element(start = "dark_avenger_mp_table_begin", end = "dark_avenger_mp_table_end")
    private LevelBonus darkAvengerMpTable;
    @Element(start = "dark_avenger_cp_table_begin", end = "dark_avenger_cp_table_end")
    private LevelBonus darkAvengerCpTable;
    @Element(start = "treasure_hunter_hp_table_begin", end = "treasure_hunter_hp_table_end")
    private LevelBonus treasureHunterHpTable;
    @Element(start = "treasure_hunter_mp_table_begin", end = "treasure_hunter_mp_table_end")
    private LevelBonus treasureHunterMpTable;
    @Element(start = "treasure_hunter_cp_table_begin", end = "treasure_hunter_cp_table_end")
    private LevelBonus treasureHunterCpTable;
    @Element(start = "hawkeye_hp_table_begin", end = "hawkeye_hp_table_end")
    private LevelBonus hawkeyeHpTable;
    @Element(start = "hawkeye_mp_table_begin", end = "hawkeye_mp_table_end")
    private LevelBonus hawkeyeMpTable;
    @Element(start = "hawkeye_cp_table_begin", end = "hawkeye_cp_table_end")
    private LevelBonus hawkeyeCpTable;

    public static PCParameterHolder getInstance() {
        return ourInstance;
    }

    @Override
    public void afterParsing() {
        super.afterParsing();
        classDataInfo.put(ClassID.fighter.getClassId(), new ClassDataInfo(fighterHpTable, fighterMpTable, fighterCpTable));
        classDataInfo.put(ClassID.warrior.getClassId(), new ClassDataInfo(warriorHpTable, warriorMpTable, warriorCpTable));
        classDataInfo.put(ClassID.knight.getClassId(), new ClassDataInfo(knightHpTable, knightMpTable, knightCpTable));
        classDataInfo.put(ClassID.rogue.getClassId(), new ClassDataInfo(rogueHpTable, rogueMpTable, rogueCpTable));
        classDataInfo.put(ClassID.warlord.getClassId(), new ClassDataInfo(warlordHpTable, warlordMpTable, warlordCpTable));
        classDataInfo.put(ClassID.gladiator.getClassId(), new ClassDataInfo(gladiatorHpTable, gladiatorMpTable, gladiatorCpTable));
        classDataInfo.put(ClassID.paladin.getClassId(), new ClassDataInfo(paladinHpTable, paladinMpTable, paladinCpTable));
        classDataInfo.put(ClassID.dark_avenger.getClassId(), new ClassDataInfo(darkAvengerHpTable, darkAvengerMpTable, darkAvengerCpTable));
        classDataInfo.put(ClassID.treasure_hunter.getClassId(), new ClassDataInfo(treasureHunterHpTable, treasureHunterMpTable, treasureHunterCpTable));
        classDataInfo.put(ClassID.hawkeye.getClassId(), new ClassDataInfo(hawkeyeHpTable, hawkeyeMpTable, hawkeyeCpTable));
        classDataInfo.put(ClassID.mage.getClassId(), new ClassDataInfo(mageHpTable, mageMpTable, mageCpTable));
        classDataInfo.put(ClassID.wizard.getClassId(), new ClassDataInfo(wizardHpTable, wizardMpTable, wizardCpTable));
        classDataInfo.put(ClassID.cleric.getClassId(), new ClassDataInfo(clericHpTable, clericMpTable, clericCpTable));
        classDataInfo.put(ClassID.sorceror.getClassId(), new ClassDataInfo(sorcererHpTable, sorcererMpTable, sorcererCpTable));
        classDataInfo.put(ClassID.necromancer.getClassId(), new ClassDataInfo(necromancerHpTable, necromancerMpTable, necromancerCpTable));
        classDataInfo.put(ClassID.warlock.getClassId(), new ClassDataInfo(warlockHpTable, warlockMpTable, warlockCpTable));
        classDataInfo.put(ClassID.bishop.getClassId(), new ClassDataInfo(bishopHpTable, bishopMpTable, bishopCpTable));
        classDataInfo.put(ClassID.prophet.getClassId(), new ClassDataInfo(prophetHpTable, prophetMpTable, prophetCpTable));
        classDataInfo.put(ClassID.elven_fighter.getClassId(), new ClassDataInfo(elvenFighterHpTable, elvenFighterMpTable, elvenFighterCpTable));
        classDataInfo.put(ClassID.elven_knight.getClassId(), new ClassDataInfo(elvenKnightHpTable, elvenKnightMpTable, elvenKnightCpTable));
        classDataInfo.put(ClassID.elven_scout.getClassId(), new ClassDataInfo(elvenScoutHpTable, elvenScoutMpTable, elvenScoutCpTable));
        classDataInfo.put(ClassID.temple_knight.getClassId(), new ClassDataInfo(templeKnightHpTable, templeKnightMpTable, templeKnightCpTable));
        classDataInfo.put(ClassID.sword_singer.getClassId(), new ClassDataInfo(swordsingerHpTable, swordsingerMpTable, swordsingerCpTable));
        classDataInfo.put(ClassID.plains_walker.getClassId(), new ClassDataInfo(plainWalkerHpTable, plainWalkerMpTable, plainWalkerCpTable));
        classDataInfo.put(ClassID.silver_ranger.getClassId(), new ClassDataInfo(silverRangerHpTable, silverRangerMpTable, silverRangerCpTable));
        classDataInfo.put(ClassID.elven_mage.getClassId(), new ClassDataInfo(elvenMageHpTable, elvenMageMpTable, elvenMageCpTable));
        classDataInfo.put(ClassID.elven_wizard.getClassId(), new ClassDataInfo(elvenWizardHpTable, elvenWizardMpTable, elvenWizardCpTable));
        classDataInfo.put(ClassID.oracle.getClassId(), new ClassDataInfo(oracleHpTable, oracleMpTable, oracleCpTable));
        classDataInfo.put(ClassID.spellsinger.getClassId(), new ClassDataInfo(spellsingerHpTable, spellsingerMpTable, spellsingerCpTable));
        classDataInfo.put(ClassID.elemental_summoner.getClassId(), new ClassDataInfo(elementalSummonerHpTable, elementalSummonerMpTable, elementalSummonerCpTable));
        classDataInfo.put(ClassID.elder.getClassId(), new ClassDataInfo(elderHpTable, elderMpTable, elderCpTable));
        classDataInfo.put(ClassID.dark_fighter.getClassId(), new ClassDataInfo(darkFighterHpTable, darkFighterMpTable, darkFighterCpTable));
        classDataInfo.put(ClassID.palus_knight.getClassId(), new ClassDataInfo(palusKnightHpTable, palusKnightMpTable, palusKnightCpTable));
        classDataInfo.put(ClassID.assassin.getClassId(), new ClassDataInfo(assasinHpTable, assasinMpTable, assasinCpTable));
        classDataInfo.put(ClassID.shillien_knight.getClassId(), new ClassDataInfo(shillienKnightHpTable, shillienKnightMpTable, shillienKnightCpTable));
        classDataInfo.put(ClassID.bladedancer.getClassId(), new ClassDataInfo(bladedancerHpTable, bladedancerMpTable, bladedancerCpTable));
        classDataInfo.put(ClassID.abyss_walker.getClassId(), new ClassDataInfo(abyssWalkerHpTable, abyssWalkerMpTable, abyssWalkerCpTable));
        classDataInfo.put(ClassID.phantom_ranger.getClassId(), new ClassDataInfo(phantomRangerHpTable, phantomRangerMpTable, phantomRangerCpTable));
        classDataInfo.put(ClassID.dark_mage.getClassId(), new ClassDataInfo(darkMageHpTable, darkMageMpTable, darkMageCpTable));
        classDataInfo.put(ClassID.dark_wizard.getClassId(), new ClassDataInfo(darkWizardHpTable, darkWizardMpTable, darkWizardCpTable));
        classDataInfo.put(ClassID.shillien_oracle.getClassId(), new ClassDataInfo(shillienOracleHpTable, shillienOracleMpTable, shillienOracleCpTable));
        classDataInfo.put(ClassID.spellhowler.getClassId(), new ClassDataInfo(spellhowlerHpTable, spellhowlerMpTable, spellhowlerCpTable));
        classDataInfo.put(ClassID.phantom_summoner.getClassId(), new ClassDataInfo(phantomSummonerHpTable, phantomSummonerMpTable, phantomSummonerCpTable));
        classDataInfo.put(ClassID.shillien_elder.getClassId(), new ClassDataInfo(shillienElderHpTable, shillienElderMpTable, shillienElderCpTable));
        classDataInfo.put(ClassID.orc_fighter.getClassId(), new ClassDataInfo(orcFighterHpTable, orcFighterMpTable, orcFighterCpTable));
        classDataInfo.put(ClassID.orc_raider.getClassId(), new ClassDataInfo(orcRaiderHpTable, orcRaiderMpTable, orcRaiderCpTable));
        classDataInfo.put(ClassID.orc_monk.getClassId(), new ClassDataInfo(orcMonkHpTable, orcMonkMpTable, orcMonkCpTable));
        classDataInfo.put(ClassID.destroyer.getClassId(), new ClassDataInfo(destroyerHpTable, destroyerMpTable, destroyerCpTable));
        classDataInfo.put(ClassID.tyrant.getClassId(), new ClassDataInfo(tyrantHpTable, tyrantMpTable, tyrantCpTable));
        classDataInfo.put(ClassID.orc_mage.getClassId(), new ClassDataInfo(orcMageHpTable, orcMageMpTable, orcMageCpTable));
        classDataInfo.put(ClassID.orc_shaman.getClassId(), new ClassDataInfo(orcShamanHpTable, orcShamanMpTable, orcShamanCpTable));
        classDataInfo.put(ClassID.overlord.getClassId(), new ClassDataInfo(overlordHpTable, overlordMpTable, overlordCpTable));
        classDataInfo.put(ClassID.warcryer.getClassId(), new ClassDataInfo(warcryerHpTable, warcryerMpTable, warcryerCpTable));
        classDataInfo.put(ClassID.dwarven_fighter.getClassId(), new ClassDataInfo(dwarvenFighterHpTable, dwarvenFighterMpTable, dwarvenFighterCpTable));
        classDataInfo.put(ClassID.scavenger.getClassId(), new ClassDataInfo(scavengerHpTable, scavengerMpTable, scavengerCpTable));
        classDataInfo.put(ClassID.artisan.getClassId(), new ClassDataInfo(artisanHpTable, artisanMpTable, artisanCpTable));
        classDataInfo.put(ClassID.bounty_hunter.getClassId(), new ClassDataInfo(bountyHunterHpTable, bountyHunterMpTable, bountyHunterCpTable));
        classDataInfo.put(ClassID.warsmith.getClassId(), new ClassDataInfo(warsmithHpTable, warsmithMpTable, warsmithCpTable));
        classDataInfo.put(ClassID.dreadnought.getClassId(), new ClassDataInfo(dreadnoughtHpTable, dreadnoughtMpTable, dreadnoughtCpTable));
        classDataInfo.put(ClassID.duelist.getClassId(), new ClassDataInfo(duelistHpTable, duelistMpTable, duelistCpTable));
        classDataInfo.put(ClassID.phoenix_knight.getClassId(), new ClassDataInfo(phoenixKnightHpTable, phoenixKnightMpTable, phoenixKnightCpTable));
        classDataInfo.put(ClassID.hell_knight.getClassId(), new ClassDataInfo(hellKnightHpTable, hellKnightMpTable, hellKnightCpTable));
        classDataInfo.put(ClassID.adventurer.getClassId(), new ClassDataInfo(adventurerHpTable, adventurerMpTable, adventurerCpTable));
        classDataInfo.put(ClassID.sagittarius.getClassId(), new ClassDataInfo(sagittariusHpTable, sagittariusMpTable, sagittariusCpTable));
        classDataInfo.put(ClassID.archmage.getClassId(), new ClassDataInfo(archmageHpTable, archmageMpTable, archmageCpTable));
        classDataInfo.put(ClassID.soultaker.getClassId(), new ClassDataInfo(soultakerHpTable, soultakerMpTable, soultakerCpTable));
        classDataInfo.put(ClassID.arcana_lord.getClassId(), new ClassDataInfo(arcanaLordHpTable, arcanaLordMpTable, arcanaLordCpTable));
        classDataInfo.put(ClassID.cardinal.getClassId(), new ClassDataInfo(cardinalHpTable, cardinalMpTable, cardinalCpTable));
        classDataInfo.put(ClassID.hierophant.getClassId(), new ClassDataInfo(hierophantHpTable, hierophantMpTable, hierophantCpTable));
        classDataInfo.put(ClassID.eva_templar.getClassId(), new ClassDataInfo(evasTemplarHpTable, evasTemplarMpTable, evasTemplarCpTable));
        classDataInfo.put(ClassID.sword_muse.getClassId(), new ClassDataInfo(swordMuseHpTable, swordMuseMpTable, swordMuseCpTable));
        classDataInfo.put(ClassID.wind_rider.getClassId(), new ClassDataInfo(windRiderHpTable, windRiderMpTable, windRiderCpTable));
        classDataInfo.put(ClassID.moonlight_sentinel.getClassId(), new ClassDataInfo(moonlightSentinelHpTable, moonlightSentinelMpTable, moonlightSentinelCpTable));
        classDataInfo.put(ClassID.mystic_muse.getClassId(), new ClassDataInfo(mysticMuseHpTable, mysticMuseMpTable, mysticMuseCpTable));
        classDataInfo.put(ClassID.elemental_master.getClassId(), new ClassDataInfo(elementalMasterHpTable, elementalMasterMpTable, elementalMasterCpTable));
        classDataInfo.put(ClassID.eva_saint.getClassId(), new ClassDataInfo(evasSaintHpTable, evasSaintMpTable, evasSaintCpTable));
        classDataInfo.put(ClassID.shillien_templar.getClassId(), new ClassDataInfo(shillienTemplarHpTable, shillienTemplarMpTable, shillienTemplarCpTable));
        classDataInfo.put(ClassID.spectral_dancer.getClassId(), new ClassDataInfo(spectralDancerHpTable, spectralDancerMpTable, spectralDancerCpTable));
        classDataInfo.put(ClassID.ghost_hunter.getClassId(), new ClassDataInfo(ghostHunterHpTable, ghostHunterMpTable, ghostHunterCpTable));
        classDataInfo.put(ClassID.ghost_sentinel.getClassId(), new ClassDataInfo(ghostSentinelHpTable, ghostSentinelMpTable, ghostSentinelCpTable));
        classDataInfo.put(ClassID.storm_screamer.getClassId(), new ClassDataInfo(stormScreamerHpTable, stormScreamerMpTable, stormScreamerCpTable));
        classDataInfo.put(ClassID.spectral_master.getClassId(), new ClassDataInfo(spectralMasterHpTable, spectralMasterMpTable, spectralMasterCpTable));
        classDataInfo.put(ClassID.shillien_saint.getClassId(), new ClassDataInfo(shillienSaintHpTable, shillienSaintMpTable, shillienSaintCpTable));
        classDataInfo.put(ClassID.titan.getClassId(), new ClassDataInfo(titanHpTable, titanMpTable, titanCpTable));
        classDataInfo.put(ClassID.grand_khauatari.getClassId(), new ClassDataInfo(grandKhavatariHpTable, grandKhavatariMpTable, grandKhavatariCpTable));
        classDataInfo.put(ClassID.dominator.getClassId(), new ClassDataInfo(dominatorHpTable, dominatorMpTable, dominatorCpTable));
        classDataInfo.put(ClassID.doomcryer.getClassId(), new ClassDataInfo(doomcryerHpTable, doomcryerMpTable, doomcryerCpTable));
        classDataInfo.put(ClassID.fortune_seeker.getClassId(), new ClassDataInfo(fortuneSeekerHpTable, fortuneSeekerMpTable, fortuneSeekerCpTable));
        classDataInfo.put(ClassID.maestro.getClassId(), new ClassDataInfo(maestroHpTable, maestroMpTable, maestroCpTable));
        classDataInfo.put(ClassID.kamael_m_soldier.getClassId(), new ClassDataInfo(kamaelMSoldierHpTable, kamaelMSoldierMpTable, kamaelMSoldierCpTable));
        classDataInfo.put(ClassID.trooper.getClassId(), new ClassDataInfo(trooperHpTable, trooperMpTable, trooperCpTable));
        classDataInfo.put(ClassID.berserker.getClassId(), new ClassDataInfo(berserkerHpTable, berserkerMpTable, berserkerCpTable));
        classDataInfo.put(ClassID.m_soul_breaker.getClassId(), new ClassDataInfo(mSoulBreakerHpTable, mSoulBreakerMpTable, mSoulBreakerCpTable));
        classDataInfo.put(ClassID.doombringer.getClassId(), new ClassDataInfo(doombringerHpTable, doombringerMpTable, doombringerCpTable));
        classDataInfo.put(ClassID.m_soul_hound.getClassId(), new ClassDataInfo(mSoulHoundHpTable, mSoulHoundMpTable, mSoulHoundCpTable));
        classDataInfo.put(ClassID.kamael_f_soldier.getClassId(), new ClassDataInfo(kamaelFSoldierHpTable, kamaelFSoldierMpTable, kamaelFSoldierCpTable));
        classDataInfo.put(ClassID.warder.getClassId(), new ClassDataInfo(warderHpTable, warderMpTable, warderCpTable));
        classDataInfo.put(ClassID.arbalester.getClassId(), new ClassDataInfo(arbalesterHpTable, arbalesterMpTable, arbalesterCpTable));
        classDataInfo.put(ClassID.f_soul_breaker.getClassId(), new ClassDataInfo(fSoulBreakerHpTable, fSoulBreakerMpTable, fSoulBreakerCpTable));
        classDataInfo.put(ClassID.trickster.getClassId(), new ClassDataInfo(tricksterHpTable, tricksterMpTable, tricksterCpTable));
        classDataInfo.put(ClassID.f_soul_hound.getClassId(), new ClassDataInfo(fSoulHoundHpTable, fSoulHoundMpTable, fSoulHoundCpTable));
        classDataInfo.put(ClassID.inspector.getClassId(), new ClassDataInfo(inspectorHpTable, inspectorMpTable, inspectorCpTable));
        classDataInfo.put(ClassID.judicator.getClassId(), new ClassDataInfo(judicatorHpTable, judicatorMpTable, judicatorCpTable));
		
		/* GOD
		classDataInfo.put(ClassID.sigel_knight.getClassId(), new ClassDataInfo(this.sigelKnightHpTable, this.sigelKnightMpTable, this.sigelKnightCpTable));
		classDataInfo.put(ClassID.tir_warrior.getClassId(), new ClassDataInfo(this.tirWarriorHpTable, this.tirWarriorMpTable, this.tirWarriorCpTable));
		classDataInfo.put(ClassID.othel_rogue.getClassId(), new ClassDataInfo(this.othelRogueHpTable, this.othelRogueMpTable, this.othelRogueCpTable));
		classDataInfo.put(ClassID.yr_archer.getClassId(), new ClassDataInfo(this.yrArcherHpTable, this.yrArcherMpTable, this.yrArcherCpTable));
		classDataInfo.put(ClassID.feoh_wizard.getClassId(), new ClassDataInfo(this.feohWizardHpTable, this.feohWizardMpTable, this.feohWizardCpTable));
		classDataInfo.put(ClassID.is_enchanter.getClassId(), new ClassDataInfo(this.isEnchanterHpTable, this.isEnchanterMpTable, this.isEnchanterCpTable));
		classDataInfo.put(ClassID.wynn_summoner.getClassId(), new ClassDataInfo(this.wynnSummonerHpTable, this.wynnSummonerMpTable, this.wynnSummonerCpTable));
		classDataInfo.put(ClassID.eolh_healer.getClassId(), new ClassDataInfo(this.eolhHealerHpTable, this.eolhHealerMpTable, this.eolhHealerCpTable));
		*/
    }

    public Map<Integer, ClassDataInfo> getClassDataInfo() {
        return classDataInfo;
    }

    public BaseParameterInt getBasePhysicalAttack() {
        return basePhysicalAttack;
    }

    public BaseParameterInt getBaseCritical() {
        return baseCritical;
    }

    public BaseParameterInt getBaseAttackType() {
        return baseAttackType;
    }

    public BaseParameterInt getBaseAttackSpeed() {
        return baseAttackSpeed;
    }

    public BaseParameterArray getBaseDefend() {
        return baseDefend;
    }

    public BaseParameterInt getBaseMagicAttack() {
        return baseMagicAttack;
    }

    public BaseParameterArray getBaseMagicDefend() {
        return baseMagicDefend;
    }

    public BaseParameterInt getBaseCanPenetrate() {
        return baseCanPenetrate;
    }

    public BaseParameterInt getBaseAttackRange() {
        return baseAttackRange;
    }

    public BaseParameterArray getBaseDamageRange() {
        return baseDamageRange;
    }

    public BaseParameterInt getBaseRandDam() {
        return baseRandDam;
    }

    public LevelBonus getLevelBonus() {
        return levelBonus;
    }

    public ParameterBonus getStrBonus() {
        return strBonus;
    }

    public ParameterBonus getIntBonus() {
        return intBonus;
    }

    public ParameterBonus getConBonus() {
        return conBonus;
    }

    public ParameterBonus getMenBonus() {
        return menBonus;
    }

    public ParameterBonus getDexBonus() {
        return dexBonus;
    }

    public ParameterBonus getWitBonus() {
        return witBonus;
    }

    public BaseParameterArray getOrgHpRegen() {
        return orgHpRegen;
    }

    public BaseParameterArray getOrgMpRegen() {
        return orgMpRegen;
    }

    public BaseParameterArray getOrgCpRegen() {
        return orgCpRegen;
    }

    public BaseParameterArray getMovingSpeed() {
        return movingSpeed;
    }

    public BaseParameterInt getOrgJump() {
        return orgJump;
    }

    public BaseParameterInt getPcBreathBonusTable() {
        return pcBreathBonusTable;
    }

    public BaseParameterInt getPcSafeFallHeightTable() {
        return pcSafeFallHeightTable;
    }

    public BaseParameterArray getPcCollisionBoxTable() {
        return pcCollisionBoxTable;
    }

    public LevelBonus getPcKarmaIncrease() {
        return pc_karma_increase_table;
    }

    @Override
    public int size() {
        return classDataInfo.size();
    }

    @Override
    public void clear() {
        classDataInfo.clear();
    }
}