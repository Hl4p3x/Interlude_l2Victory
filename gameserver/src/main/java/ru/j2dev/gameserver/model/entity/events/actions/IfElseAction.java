package ru.j2dev.gameserver.model.entity.events.actions;

import ru.j2dev.gameserver.model.entity.events.EventAction;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;

import java.util.Collections;
import java.util.List;

public class IfElseAction implements EventAction {
    private final String _name;
    private final boolean _reverse;
    private List<EventAction> _ifList;
    private List<EventAction> _elseList;

    public IfElseAction(final String name, final boolean reverse) {
        _ifList = Collections.emptyList();
        _elseList = Collections.emptyList();
        _name = name;
        _reverse = reverse;
    }

    @Override
    public void call(final GlobalEvent event) {
        final List<EventAction> list = (_reverse ? (!event.ifVar(_name)) : event.ifVar(_name)) ? _ifList : _elseList;
        list.forEach(action -> action.call(event));
    }

    public void setIfList(final List<EventAction> ifList) {
        _ifList = ifList;
    }

    public void setElseList(final List<EventAction> elseList) {
        _elseList = elseList;
    }
}
