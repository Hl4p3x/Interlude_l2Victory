package ru.j2dev.dataparser.holder.petdata;

import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.pch.LinkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author KilRoy
 */
public class PetData {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetData.class);
    @IntValue
    private int pet_index; // Индекс ИД пета
    @StringValue
    private String npc_name; // NPC Template используемый петом
    @StringValue
    private String item; // Итем, контролирующий пета
    @IntValue
    private int sync_level; // При вызове, синхронизация уровня с овнером
    @IntArray
    private int[] evolve; // уровни, на которых пет эволюционирует
    @Element(start = "stat_begin", end = "stat_end")
    private List<PetLevelStat> petLevelStat;

    public int getPetIndex() {
        return pet_index;
    }

    public String getPetName() {
        return npc_name;
    }

    public String getControlItem() {
        return item;
    }

    public boolean getSyncLevel() {
        return sync_level == 1;
    }

    public int[] getPetEvolve() {
        return evolve;
    }

    public List<PetLevelStat> getLevelStat() {
        return petLevelStat;
    }

    public PetLevelStat getLevelStatForLevel(final int level) {
        for (final PetLevelStat levelStat : petLevelStat) {
            if (levelStat.getLevel() == level) {
                return levelStat;
            }
        }
        LOGGER.error("getLevelStatForLevel(int) returned null from level: {} for npcTemplate: {}", level, npc_name);
        return null;
    }

    public int getControlItemId() {
        return item != null && !item.isEmpty() ? LinkerFactory.getInstance().findClearValue(item) : -1;
    }

    public int getMinLvl() {
        return petLevelStat.stream().mapToInt(PetLevelStat::getLevel).min().getAsInt();
    }

    public int getMaxLvl() {
        return petLevelStat.stream().mapToInt(PetLevelStat::getLevel).max().getAsInt();
    }
}