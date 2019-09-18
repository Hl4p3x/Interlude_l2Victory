package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.stats.Env;

public class ConditionPlayerGender extends Condition {
    private final Gender _gender;

    public ConditionPlayerGender(final Gender gender) {
        _gender = gender;
    }

    public ConditionPlayerGender(final String gender) {
        this(Gender.valueOf(gender.toUpperCase()));
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.character.isPlayer() && ((Player) env.character).getSex() == _gender.getPlayerGenderId();
    }

    public enum Gender {
        MALE(0),
        FEMALE(1);

        private final int _playerGenderId;

        Gender(final int playerGenderId) {
            _playerGenderId = playerGenderId;
        }

        public int getPlayerGenderId() {
            return _playerGenderId;
        }
    }
}
