package ru.j2dev.dataparser.holder.npcpos.maker_ex;

import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.holder.NpcPosHolder;
import ru.j2dev.dataparser.holder.npcpos.common.AIParameters;
import ru.j2dev.dataparser.holder.npcpos.common.DefaultMakerNpc;

/**
 * @author : Camelion
 * @date : 30.08.12  22:13
 */
@ParseSuper
public class NpcEx extends DefaultMakerNpc {
    @StringValue
    public String nickname; // Данная строка не задействована нигде кроме этого файла
    @ObjectValue(objectFactory = NpcPosHolder.AiParamsObjectFactory.class)
    public final AIParameters ai_parameters = new AIParameters(); // Присутствует всегда, params может быть пустым

    public AIParameters getAiParameters() {
        return ai_parameters;
    }
}
