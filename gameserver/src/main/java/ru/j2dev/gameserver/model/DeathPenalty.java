package ru.j2dev.gameserver.model;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.tables.SkillTable;

public class DeathPenalty {
    private static final int _skillId = 5076;
    private static final int _fortuneOfNobleseSkillId = 1325;
    private static final int _charmOfLuckSkillId = 2168;
    private final HardReference<Player> _playerRef;
    private int _level;
    private boolean _hasCharmOfLuck;

    public DeathPenalty(final Player player, final int level) {
        _playerRef = player.getRef();
        _level = level;
    }

    public Player getPlayer() {
        return _playerRef.get();
    }

    public int getLevel() {
        if (_level > 15) {
            _level = 15;
        }
        if (_level < 0) {
            _level = 0;
        }
        return Config.ALLOW_DEATH_PENALTY_C5 ? _level : 0;
    }

    public int getLevelOnSaveDB() {
        if (_level > 15) {
            _level = 15;
        }
        if (_level < 0) {
            _level = 0;
        }
        return _level;
    }

    public void notifyDead(final Creature killer) {
        if (!Config.ALLOW_DEATH_PENALTY_C5) {
            return;
        }
        if (_hasCharmOfLuck) {
            _hasCharmOfLuck = false;
            return;
        }
        if (killer == null || killer.isPlayable()) {
            return;
        }
        final Player player = getPlayer();
        if (player == null || player.getLevel() <= 9) {
            return;
        }
        int karmaBonus = player.getKarma() / Config.ALT_DEATH_PENALTY_C5_KARMA_PENALTY;
        if (karmaBonus < 0) {
            karmaBonus = 0;
        }
        if (Rnd.chance(Config.ALT_DEATH_PENALTY_C5_CHANCE + karmaBonus)) {
            addLevel();
        }
    }

    public void restore(final Player player) {
        final Skill remove = player.getKnownSkill(_skillId);
        if (remove != null) {
            player.removeSkill(remove, true);
        }
        if (!Config.ALLOW_DEATH_PENALTY_C5) {
            return;
        }
        if (getLevel() > 0) {
            player.addSkill(SkillTable.getInstance().getInfo(_skillId, getLevel()), false);
            player.sendPacket(new SystemMessage(1916).addNumber(getLevel()));
        }
        player.sendEtcStatusUpdate();
        player.updateStats();
    }

    public void addLevel() {
        final Player player = getPlayer();
        if (player == null || getLevel() >= 15 || player.isGM()) {
            return;
        }
        if (getLevel() != 0) {
            final Skill remove = player.getKnownSkill(_skillId);
            if (remove != null) {
                player.removeSkill(remove, true);
            }
        }
        ++_level;
        player.addSkill(SkillTable.getInstance().getInfo(_skillId, getLevel()), false);
        player.sendPacket(new SystemMessage(1916).addNumber(getLevel()));
        player.sendEtcStatusUpdate();
        player.updateStats();
    }

    public void reduceLevel() {
        final Player player = getPlayer();
        if (player == null || getLevel() <= 0) {
            return;
        }
        final Skill remove = player.getKnownSkill(_skillId);
        if (remove != null) {
            player.removeSkill(remove, true);
        }
        --_level;
        if (getLevel() > 0) {
            player.addSkill(SkillTable.getInstance().getInfo(_skillId, getLevel()), false);
            player.sendPacket(new SystemMessage(1916).addNumber(getLevel()));
        } else {
            player.sendPacket(Msg.THE_DEATH_PENALTY_HAS_BEEN_LIFTED);
        }
        player.sendEtcStatusUpdate();
        player.updateStats();
    }

    public void checkCharmOfLuck() {
        final Player player = getPlayer();
        if (player != null) {
            for (final Effect e : player.getEffectList().getAllEffects()) {
                if (e.getSkill().getId() == _charmOfLuckSkillId || e.getSkill().getId() == _fortuneOfNobleseSkillId) {
                    _hasCharmOfLuck = true;
                    return;
                }
            }
        }
        _hasCharmOfLuck = false;
    }
}
