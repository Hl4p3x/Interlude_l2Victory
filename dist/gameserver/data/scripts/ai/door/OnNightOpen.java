package ai.door;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.gameserver.GameTimeController;
import ru.j2dev.gameserver.ai.DoorAI;
import ru.j2dev.gameserver.listener.game.OnDayNightChangeListener;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.instances.DoorInstance;

public class OnNightOpen extends DoorAI {
    public OnNightOpen(final DoorInstance actor) {
        super(actor);
        GameTimeController.getInstance().addListener(new NightDoorOpenController(actor));
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    private static class NightDoorOpenController implements OnDayNightChangeListener {
        private final HardReference<? extends DoorInstance> _actRef;

        NightDoorOpenController(final DoorInstance actor) {
            _actRef = actor.getRef();
        }

        @Override
        public void onDay() {
        }

        @Override
        public void onNight() {
            final DoorInstance door = _actRef.get();
            if (door != null && door.getReflection() == ReflectionManager.DEFAULT) {
                door.openMe();
                LOGGER.info("Zaken door (Location : " + door.getLoc() + ") is opened for 5 min.");
            } else {
                LOGGER.warn("Zaken door is null");
            }
        }
    }
}
