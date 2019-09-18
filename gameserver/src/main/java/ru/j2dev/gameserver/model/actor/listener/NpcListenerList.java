package ru.j2dev.gameserver.model.actor.listener;

import ru.j2dev.gameserver.listener.actor.npc.OnDecayListener;
import ru.j2dev.gameserver.listener.actor.npc.OnShowChatEventListener;
import ru.j2dev.gameserver.listener.actor.npc.OnShowChatListener;
import ru.j2dev.gameserver.listener.actor.npc.OnSpawnListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class NpcListenerList extends CharListenerList {
    public NpcListenerList(final NpcInstance actor) {
        super(actor);
    }

    @Override
    public NpcInstance getActor() {
        return (NpcInstance) actor;
    }

    public void onSpawn() {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnSpawnListener.class::isInstance).forEach(listener -> ((OnSpawnListener) listener).onSpawn(getActor()));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnSpawnListener.class::isInstance).forEach(listener -> ((OnSpawnListener) listener).onSpawn(getActor()));
        }
    }

    public void onDecay() {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnDecayListener.class::isInstance).forEach(listener -> ((OnDecayListener) listener).onDecay(getActor()));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnDecayListener.class::isInstance).forEach(listener -> ((OnDecayListener) listener).onDecay(getActor()));
        }
    }

    public void onShowChat() {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnShowChatListener.class::isInstance).forEach(listener -> ((OnShowChatListener) listener).onShowChat(getActor()));
        }

        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnShowChatListener.class::isInstance).forEach(listener -> ((OnShowChatListener) listener).onShowChat(getActor()));
        }
    }

    public void onShowChatEvent(final Player player) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnShowChatEventListener.class::isInstance).forEach(listener -> ((OnShowChatEventListener) listener).onShowChatEvent(getActor(), player));
        }

        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnShowChatEventListener.class::isInstance).forEach(listener -> ((OnShowChatEventListener) listener).onShowChatEvent(getActor(), player));
        }
    }
}
