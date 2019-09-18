package ru.j2dev.gameserver.skills.effects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.Skill.SkillTargetType;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.SymbolInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillLaunched;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

public final class EffectSymbol extends Effect {
    private static final Logger LOGGER = LoggerFactory.getLogger(EffectSymbol.class);

    private NpcInstance _symbol;

    public EffectSymbol(final Env env, final EffectTemplate template) {
        super(env, template);
        _symbol = null;
    }

    @Override
    public boolean checkCondition() {
        if (getSkill().getTargetType() != SkillTargetType.TARGET_SELF) {
            LOGGER.error("Symbol skill with target != self, id = " + getSkill().getId());
            return false;
        }
        final Skill skill = getSkill().getFirstAddedSkill();
        if (skill == null) {
            LOGGER.error("Not implemented symbol skill, id = " + getSkill().getId());
            return false;
        }
        return super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        final Skill skill = getSkill().getFirstAddedSkill();
        skill.setMagicType(getSkill().getMagicType());
        Location loc = _effected.getLoc();
        if (_effected.isPlayer() && ((Player) _effected).getGroundSkillLoc() != null) {
            loc = ((Player) _effected).getGroundSkillLoc();
            ((Player) _effected).setGroundSkillLoc(null);
        }
        final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(_skill.getSymbolId());
        if (getTemplate()._count <= 1) {
            _symbol = new SymbolInstance(IdFactory.getInstance().getNextId(), template, _effected, skill);
        } else {
            _symbol = new NpcInstance(IdFactory.getInstance().getNextId(), template);
        }
        _symbol.setLevel(_effected.getLevel());
        _symbol.setReflection(_effected.getReflection());
        _symbol.spawnMe(loc);
    }

    @Override
    public void onExit() {
        super.onExit();
        if (_symbol != null && _symbol.isVisible()) {
            _symbol.deleteMe();
        }
        _symbol = null;
    }

    @Override
    public boolean onActionTime() {
        if (getTemplate()._count <= 1) {
            return false;
        }
        final Creature effector = getEffector();
        final Skill skill = getSkill().getFirstAddedSkill();
        final NpcInstance symbol = _symbol;
        final double mpConsume = getSkill().getMpConsume();
        if (effector == null || skill == null || symbol == null) {
            return false;
        }
        if (mpConsume > effector.getCurrentMp()) {
            effector.sendPacket(Msg.NOT_ENOUGH_MP);
            return false;
        }
        effector.reduceCurrentMp(mpConsume, effector);
        for (final Creature cha : World.getAroundCharacters(symbol, getSkill().getSkillRadius(), 200)) {
            if (!cha.isDoor() && cha.getEffectList().getEffectsBySkill(skill) == null && skill.checkTarget(effector, cha, cha, false, false) == null) {
                if (skill.isOffensive() && !GeoEngine.canSeeTarget(symbol, cha, false)) {
                    continue;
                }
                final List<Creature> targets = new ArrayList<>(1);
                targets.add(cha);
                effector.callSkill(skill, targets, true);
                effector.broadcastPacket(new MagicSkillLaunched(symbol, getSkill(), cha.getObjectId()));
            }
        }
        return true;
    }
}
