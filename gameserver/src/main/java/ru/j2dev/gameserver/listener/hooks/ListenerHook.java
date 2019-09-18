package ru.j2dev.gameserver.listener.hooks;


import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.*;

public class ListenerHook {
    private static Map<ListenerHookType, Set<ListenerHook>> _globalListenerHooks = new HashMap<>();

    private static void addGlobalListenerHookType(ListenerHookType type, ListenerHook hook) {
        Set<ListenerHook> hooks = _globalListenerHooks.computeIfAbsent(type, k -> new HashSet<>());
        hooks.add(hook);
    }

    public static Set<ListenerHook> getGlobalListenerHooks(ListenerHookType type) {
        Set<ListenerHook> hooks = _globalListenerHooks.get(type);
        if (hooks == null)
            return Collections.emptySet();
        return hooks;
    }

    protected void addHookNpc(ListenerHookType type, int npcId) {
        try {
            NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(npcId);
            if (template != null)
                template.addListenerHook(type, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void addHookNpc(ListenerHookType type, int... npcIds) {
        for (int npcId : npcIds)
            addHookNpc(type, npcId);
    }

    protected void addHookPlayer(ListenerHookType type, Player player) {
        player.addListenerHook(type, this);
    }

    protected void removeHookPlayer(ListenerHookType type, Player player) {
        player.removeListenerHookType(type, this);
    }

    protected void addHookGlobal(ListenerHookType type) {
        addGlobalListenerHookType(type, this);
    }

    public void onNpcKill(NpcInstance npc, Player killer) {
    }

    public void onNpcAttack(NpcInstance npc, int damage, Player attacker) {
    }

    public void onNpcAsk(NpcInstance npc, int ask, long reply, Player player) {
    }

    public boolean onNpcFirstTalk(NpcInstance npc, Player player) {
        return false;
    }

    public void onNpcSpawn(NpcInstance npc) {
    }

    public void onNpcDespawn(NpcInstance npc) {
    }

    public void onPlayerFinishCastSkill(Player player, int skillId) {
    }

    public void onPlayerDie(Player player, Creature killer) {
    }

    public void onPlayerEnterGame(Player player) {
    }

    public void onPlayerQuitGame(Player player) {
    }

    public void onPlayerTeleport(Player player, int reflectionId) {
    }

    public void onPlayerCreate(Player player) {
    }

    public void onGlobalBbs(String command, Player player) {
    }

    public void onPlayerGlobalLevelUp(Player player, int oldLevel, int newLevel) {
    }

    public void onPlayerGlobalDie(Player player) {
    }

    public void onPlayerGlobalFriendAdd(Player player) {
    }

    public void onPlayerGlobalItemAdd(Player player, int itemId, long count) {
    }

    public void onPlayerGlobalKill(Player player, Player killer) {
    }

    public void onPlayerGlobalPvPUp(Player player, int oldPvP) {
    }

    public void onPlayerGlobalPKUp(Player player, int oldPK) {
    }

    public void onPlayerGlobalStartCastleSiegeInClan(int objectId) {
    }

    public void onPlayerGlobalTakeCastle(Player player) {
    }

    public void onPlayerGlobalTakeDamage(Player player, Creature attacker) {
    }
}