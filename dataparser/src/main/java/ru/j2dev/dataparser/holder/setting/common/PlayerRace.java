package ru.j2dev.dataparser.holder.setting.common;

/**
 * This class defines all races (human, elf, darkelf, orc, dwarf) that a player can chose.<BR><BR>
 */
public enum PlayerRace {
    human,
    elf,
    darkelf,
    orc,
    dwarf,
    kamael;

    public int getId() {
        return ordinal();
    }
}
