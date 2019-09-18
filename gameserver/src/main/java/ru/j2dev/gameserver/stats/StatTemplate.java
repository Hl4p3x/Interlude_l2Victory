package ru.j2dev.gameserver.stats;

import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.stats.funcs.Func;
import ru.j2dev.gameserver.stats.funcs.FuncTemplate;
import ru.j2dev.gameserver.stats.triggers.TriggerInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StatTemplate {
    protected FuncTemplate[] _funcTemplates;
    protected List<TriggerInfo> _triggerList;

    public StatTemplate() {
        _funcTemplates = FuncTemplate.EMPTY_ARRAY;
        _triggerList = Collections.emptyList();
    }

    public List<TriggerInfo> getTriggerList() {
        return _triggerList;
    }

    public void addTrigger(final TriggerInfo f) {
        if (_triggerList.isEmpty()) {
            _triggerList = new ArrayList<>(4);
        }
        _triggerList.add(f);
    }

    public void attachFunc(final FuncTemplate f) {
        _funcTemplates = ArrayUtils.add(_funcTemplates, f);
    }

    public FuncTemplate[] getAttachedFuncs() {
        return _funcTemplates;
    }

    public Func[] getStatFuncs(final Object owner) {
        if (_funcTemplates.length == 0) {
            return Func.EMPTY_FUNC_ARRAY;
        }
        return Arrays.stream(_funcTemplates).map(funcTemplate -> funcTemplate.getFunc(owner)).toArray(Func[]::new);
    }
}
