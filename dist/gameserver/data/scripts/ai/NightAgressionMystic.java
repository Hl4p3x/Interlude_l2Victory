package ai;

import ru.j2dev.gameserver.GameTimeController;
import ru.j2dev.gameserver.ai.Mystic;
import ru.j2dev.gameserver.listener.game.OnDayNightChangeListener;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class NightAgressionMystic extends Mystic {
    public NightAgressionMystic(final NpcInstance actor) {
        super(actor);
        GameTimeController.getInstance().addListener(new NightAgressionDayNightListener());
    }

    private class NightAgressionDayNightListener implements OnDayNightChangeListener {
        private NightAgressionDayNightListener() {
            if (GameTimeController.getInstance().isNowNight()) {
                onNight();
            } else {
                onDay();
            }
        }

        @Override
        public void onDay() {
            getActor().setAggroRange(0);
        }

        @Override
        public void onNight() {
            getActor().setAggroRange(-1);
        }
    }
}
