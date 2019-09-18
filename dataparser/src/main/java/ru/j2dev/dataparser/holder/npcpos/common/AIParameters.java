package ru.j2dev.dataparser.holder.npcpos.common;

import java.util.Collections;
import java.util.Map;

public class AIParameters {
    private final Map<String, Object> params;

    public AIParameters(Map<String, Object> params) // Конструктор для NpcAIObjectFactory
    {
        this.params = params;
    }

    public AIParameters() {
        params = Collections.emptyMap();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}