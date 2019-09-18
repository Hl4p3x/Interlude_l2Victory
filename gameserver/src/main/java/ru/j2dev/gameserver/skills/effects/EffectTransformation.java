package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.skills.skillclasses.Transformation;
import ru.j2dev.gameserver.stats.Env;

public final class EffectTransformation extends Effect {
    private final boolean isFlyingTransform;

    public EffectTransformation(final Env env, final EffectTemplate template) {
        super(env, template);
        final int id = (int) template._value;
        isFlyingTransform = template.getParam().getBool("isFlyingTransform", id == 8 || id == 9 || id == 260);
    }

    @Override
    public boolean checkCondition() {
        return _effected.isPlayer() && (!isFlyingTransform || _effected.getX() <= -166168) && super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        final Player player = (Player) _effected;
        player.setTransformationTemplate(getSkill().getNpcId());
        if (getSkill() instanceof Transformation) {
            player.setTransformationName(((Transformation) getSkill()).transformationName);
        }
        final int id = (int) calc();
        if (isFlyingTransform) {
            final boolean isVisible = player.isVisible();
            if (player.getPet() != null) {
                player.getPet().unSummon();
            }
            player.decayMe();
            player.setFlying(true);
            player.setLoc(player.getLoc().changeZ(300));
            player.setTransformation(id);
            if (isVisible) {
                player.spawnMe();
            }
        } else {
            player.setTransformation(id);
        }
    }

    @Override
    public void onExit() {
        super.onExit();
        if (_effected.isPlayer()) {
            final Player player = (Player) _effected;
            if (getSkill() instanceof Transformation) {
                player.setTransformationName(null);
            }
            if (isFlyingTransform) {
                final boolean isVisible = player.isVisible();
                player.decayMe();
                player.setFlying(false);
                player.setLoc(player.getLoc().correctGeoZ());
                player.setTransformation(0);
                if (isVisible) {
                    player.spawnMe();
                }
            } else {
                player.setTransformation(0);
            }
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
