package ru.j2dev.dataparser.holder.npcpos.maker;

import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;
import ru.j2dev.dataparser.annotations.value.EnumValue;
import ru.j2dev.dataparser.holder.npcpos.DefaultMaker;
import ru.j2dev.dataparser.holder.npcpos.maker.spawn_time.DefaultSpawnTime;

import java.util.List;

/**
 * @author : Camelion
 * @date : 30.08.12  20:20
 */
@ParseSuper
public class NpcMaker extends DefaultMaker {
    @EnumValue
    public InitialSpawn initial_spawn; // Присутствует везде

    public DefaultSpawnTime spawn_time; // Может быть null. Задается через NpcMakerObjectFactory

    @Element(start = "npc_begin", end = "npc_end")
    public List<Npc> npcs;

    public List<Npc> getNpcs() {
        return npcs;
    }

    public enum InitialSpawn {
        all, // Остальные значения не известны
    }
}