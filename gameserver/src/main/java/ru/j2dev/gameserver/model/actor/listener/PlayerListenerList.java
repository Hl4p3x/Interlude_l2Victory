package ru.j2dev.gameserver.model.actor.listener;

import ru.j2dev.gameserver.listener.actor.player.*;
import ru.j2dev.gameserver.listener.inventory.OnItemUseListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadGame;
import ru.j2dev.gameserver.model.quest.QuestState;

public class PlayerListenerList extends CharListenerList {
    public PlayerListenerList(final Player actor) {
        super(actor);
    }

    @Override
    public Player getActor() {
        return (Player) actor;
    }

    public void onEnter() {
        if (!PlayerListenerList.global.getListeners().isEmpty()) {
            PlayerListenerList.global.getListeners().stream().filter(OnPlayerEnterListener.class::isInstance).forEach(listener -> ((OnPlayerEnterListener) listener).onPlayerEnter(getActor()));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnPlayerEnterListener.class::isInstance).forEach(listener -> ((OnPlayerEnterListener) listener).onPlayerEnter(getActor()));
        }
    }

    public void onExit() {
        if (!PlayerListenerList.global.getListeners().isEmpty()) {
            PlayerListenerList.global.getListeners().stream().filter(OnPlayerExitListener.class::isInstance).forEach(listener -> ((OnPlayerExitListener) listener).onPlayerExit(getActor()));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnPlayerExitListener.class::isInstance).forEach(listener -> ((OnPlayerExitListener) listener).onPlayerExit(getActor()));
        }
    }

    public void onTeleport(final int x, final int y, final int z, final Reflection reflection) {
        if (!PlayerListenerList.global.getListeners().isEmpty()) {
            PlayerListenerList.global.getListeners().stream().filter(OnTeleportListener.class::isInstance).forEach(listener -> ((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnTeleportListener.class::isInstance).forEach(listener -> ((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection));
        }
    }

    public void onQuestStateChange(final QuestState questState) {
        if (!PlayerListenerList.global.getListeners().isEmpty()) {
            PlayerListenerList.global.getListeners().stream().filter(OnQuestStateChangeListener.class::isInstance).forEach(listener -> ((OnQuestStateChangeListener) listener).onQuestStateChange(getActor(), questState));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnQuestStateChangeListener.class::isInstance).forEach(listener -> ((OnQuestStateChangeListener) listener).onQuestStateChange(getActor(), questState));
        }
    }

    public void onOlyCompetitionCompleted(final OlympiadGame olympiadGame, final boolean isWin) {
        if (!PlayerListenerList.global.getListeners().isEmpty()) {
            PlayerListenerList.global.getListeners().stream().filter(OnOlyCompetitionListener.class::isInstance).forEach(listener -> ((OnOlyCompetitionListener) listener).onOlyCompetitionCompleted(getActor(), olympiadGame, isWin));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnOlyCompetitionListener.class::isInstance).forEach(listener -> ((OnOlyCompetitionListener) listener).onOlyCompetitionCompleted(getActor(), olympiadGame, isWin));
        }
    }

    public void onGainExpSp(final long exp, final long sp) {
        if (!PlayerListenerList.global.getListeners().isEmpty()) {
            PlayerListenerList.global.getListeners().stream().filter(OnGainExpSpListener.class::isInstance).forEach(listener -> ((OnGainExpSpListener) listener).onGainExpSp(getActor(), exp, sp));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnGainExpSpListener.class::isInstance).forEach(listener -> ((OnGainExpSpListener) listener).onGainExpSp(getActor(), exp, sp));
        }
    }

    public void onPvpPkKill(final Player victim, final boolean isPk) {
        if (!PlayerListenerList.global.getListeners().isEmpty()) {
            PlayerListenerList.global.getListeners().stream().filter(OnPvpPkKillListener.class::isInstance).forEach(listener -> ((OnPvpPkKillListener) listener).onPvpPkKill(getActor(), victim, isPk));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnPvpPkKillListener.class::isInstance).forEach(listener -> ((OnPvpPkKillListener) listener).onPvpPkKill(getActor(), victim, isPk));
        }
    }

    public void onPartyInvite() {
        if (!PlayerListenerList.global.getListeners().isEmpty()) {
            PlayerListenerList.global.getListeners().stream().filter(OnPlayerPartyInviteListener.class::isInstance).forEach(listener -> ((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor()));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnPlayerPartyInviteListener.class::isInstance).forEach(listener -> ((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor()));
        }
    }

    public void onPartyLeave() {
        if (!PlayerListenerList.global.getListeners().isEmpty()) {
            PlayerListenerList.global.getListeners().stream().filter(OnPlayerPartyLeaveListener.class::isInstance).forEach(listener -> ((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor()));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnPlayerPartyLeaveListener.class::isInstance).forEach(listener -> ((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor()));
        }
    }

    public void onPlayerSkillsRestored(final Player player) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnPlayerSkillRestored.class::isInstance).forEach(listener -> ((OnPlayerSkillRestored) listener).onPlayerSkillsRestored(player));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnPlayerSkillRestored.class::isInstance).forEach(listener -> ((OnPlayerSkillRestored) listener).onPlayerSkillsRestored(player));
        }
    }

    public void onPlayerSkillAdd(final Player player, final Skill newSkill, final Skill oldSkill) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnPlayerSkillAdd.class::isInstance).forEach(listener -> ((OnPlayerSkillAdd) listener).onPlayerSkillAdd(player, newSkill, oldSkill));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnPlayerSkillAdd.class::isInstance).forEach(listener -> ((OnPlayerSkillAdd) listener).onPlayerSkillAdd(player, newSkill, oldSkill));
        }
    }

    public void onPlayerClassChange(final Player player, final int oldClass, final int newClass) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnPlayerClassChange.class::isInstance).forEach(listener -> ((OnPlayerClassChange) listener).onPlayerClassChange(player, oldClass, newClass));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnPlayerClassChange.class::isInstance).forEach(listener -> ((OnPlayerClassChange) listener).onPlayerClassChange(player, oldClass, newClass));
        }
    }

    public void onItemUse(final int itemId, final Player player) {
        if(!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnItemUseListener.class::isInstance).forEach(listener -> ((OnItemUseListener) listener).onItemUse(itemId, player));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnItemUseListener.class::isInstance).forEach(listener -> ((OnItemUseListener) listener).onItemUse(itemId, player));
        }
    }
}
