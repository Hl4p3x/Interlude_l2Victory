package ru.j2dev.dataparser.holder.npcpos.common;

import ru.j2dev.dataparser.annotations.array.StringArray;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.annotations.value.TimeValue;

/**
 * @author : Camelion
 * @date : 30.08.12  20:55
 */
public class DefaultMakerNpc {
    @StringValue(withoutName = true)
    private String npc_name; // Имя NPC
    @StringValue(withoutBounds = true)
    private String pos; // Позиция. pos = anywhere - в любой точке территории, pos = {x,y,z,heading} - фиксированная точка, pos = {{x1, y1, z1, h1, chance_to_this_pos1%};{x2, y2, z2, h2, chance_to_this_pos2%}}
    @IntValue
    private int total; // Количество NPC в этой точке
    @TimeValue
    private long respawn; // Время респауна
    @TimeValue
    private long respawn_rand; // Случайный промежуток +- к респауну
    @IntValue
    private int is_chase_pc; // указатель радиуса для преследования мобом
    @StringValue
    private String Privates; // Минионы
    @StringValue
    private String dbname; // Имя, под которым NPC сохраняется в базе данных (может отсутствовать)
    @StringArray
    private String[] dbsaving; // Что сохранять в базу данных (death_time - время смерти, parameters - параметры NPC, pos - позиция)
    @StringValue
    private String boss_respawn_set; // Сохранять стейт босса в базу

    public String getNpcName() {
        return npc_name;
    }

    public String getPos() {
        return pos;
    }

    public int getTotal() {
        return total;
    }

    public long getRespawnTime() {
        return respawn;
    }

    public long getRespawnRandTime() {
        return respawn_rand;
    }

    public int getChasePcRange() {
        return is_chase_pc;
    }

    public String getPrivates() {
        return Privates;
    }

    public String getDBName() {
        return dbname;
    }

    public String[] getDBSaving() {
        return dbsaving;
    }

    public boolean getBossRespawnSet() {
        return boss_respawn_set != null && boss_respawn_set.isEmpty() && "yes".equalsIgnoreCase(boss_respawn_set);
    }
}