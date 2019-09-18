package ru.j2dev.gameserver.listener.actor.player;

import ru.j2dev.gameserver.listener.PlayerListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;

/**
 * Created by JunkyFunky
 * on 20.12.2017 10:07
 * group j2dev
 */
@FunctionalInterface
public interface OnPlayerSkillAdd extends PlayerListener {
    void onPlayerSkillAdd(Player player, Skill newSkill, Skill oldSkill);
}
