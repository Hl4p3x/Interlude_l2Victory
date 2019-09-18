package ru.j2dev.dataparser.holder.cubicdata;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.dataparser.annotations.value.DoubleValue;
import ru.j2dev.dataparser.annotations.value.IntValue;

/**
 * @author : Camelion
 * @date : 26.08.12 13:17
 * <p/>
 * Данные, которые присутствуют как для кубиков, так и для агатионов
 */
public class DefaultCubicData {
    @IntValue
    private int id; // ID кубика
    @IntValue
    private int level; // Уровень кубика (равен уровню скила, призывающего кубик)
    @IntValue
    private int slot; // какой-то слот, не может быть одновременно два помощника с одинаковыими слотами.
    @IntValue
    private int duration = -1; // Продолжительность (по умолчанию -1, без времени)
    @IntValue
    private int delay; // Какая-то задержка, возможно между действиями кубика\агатиона
    @IntValue
    private int max_count; // какое-то количество. Присутствует там, где delay > 0
    @IntValue
    private int use_up; // Неизвестно, всегда 0
    @DoubleValue
    private double power; // Сила помощника
    // Поля, обрабатываемые CubicDataObjectFactory
    private CubicDataTargetType target_type; // Тип цели, если by_skill - определяется скилом
    private CubicDataOpCond op_cond; // Какие-то условия
    private CubicDataSkill skill1; // Первый скилл
    private CubicDataSkill skill2; // Второй скилл
    private CubicDataSkill skill3; // Третий скилл

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public int getSlot() {
        return slot;
    }

    public int getDuration() {
        return duration;
    }

    public int getDelay() {
        return delay;
    }

    public int getMaxCount() {
        return max_count;
    }

    public int getUseUp() {
        return use_up;
    }

    public double getPower() {
        return power;
    }

    public CubicDataTargetType getTargetType() {
        return target_type;
    }

    public void setTargetType(final CubicDataTargetType target_type) {
        this.target_type = target_type;
    }

    public CubicDataOpCond getOpCond() {
        return op_cond;
    }

    public void setOpCond(final CubicDataOpCond op_cond) {
        this.op_cond = op_cond;
    }

    public CubicDataSkill getSkill1() {
        return skill1;
    }

    public void setSkill1(final CubicDataSkill skill1) {
        this.skill1 = skill1;
    }

    public CubicDataSkill getSkill2() {
        return skill2;
    }

    public void setSkill2(final CubicDataSkill skill2) {
        this.skill2 = skill2;
    }

    public CubicDataSkill getSkill3() {
        return skill3;
    }

    public void setSkill3(final CubicDataSkill skill3) {
        this.skill3 = skill3;
    }

    public boolean isCubic() {
        return false;
    }

    public boolean isAgathion() {
        return false;
    }

    public enum TargetType {
        target,
        heal,
        master,
        by_skill // Не используется при установке target_type в самом скиле
    }

    public static class CubicDataTargetType {
        private TargetType type; // target - кубик действует по цели владельца,
        // heal - лечит самона/владельца, by_skill - в
        // зависимости от скила
        // Только для type = heal, указывает, какой скил будет использоваться
        // Например в массиве (90;60;30;0). 100-90 - не используется, 90-60 -
        // первый скилл, 60-30 - второй скил, 0-30 - третий скил
        private int[] heal_params;

        public CubicDataTargetType(TargetType type) {
            this.type = type;
            heal_params = ArrayUtils.EMPTY_INT_ARRAY;
        }

        // Конструктор для кубиков типа heal
        public CubicDataTargetType(TargetType type, int[] heal_params) {
            this.type = type;
            this.heal_params = heal_params;
        }

        public TargetType getType() {
            return type;
        }

        public int[] getHealParameter() {
            return heal_params;
        }
    }

    public static class CubicDataOpCond {
        private boolean isDebuff;
        private int[] cond;

        // Конструктор для типа debuff
        public CubicDataOpCond() {
            isDebuff = true;
        }

        // Конструктор для типа {0;30%;1000}
        public CubicDataOpCond(int[] cond) {
            isDebuff = false;
            this.cond = cond;
        }

        public boolean isDebuff() {
            return isDebuff;
        }

        public int[] getCond() {
            return cond;
        }
    }

    public static class CubicDataSkill {
        // Может отсутствовать
        public int skillChance; // Шанс того, что помощник будет использовать именно этот скилл, отсутствует для скила типа TargetType.heal Параметры, присущие каждому скилу
        public String skill_name; // Название скила
        public int useChance; // Шанс использования этого скила, если прошел skillChance
        public int targetStaticObject; // 1 = true, 0 = false Может отсутствовать Если CubicData.target_type = by_skill, то для определения цели используются эти параметры
        public TargetType skill_target_type;
        public CubicDataOpCond skill_op_cond;

        public int getSkillChance() {
            return skillChance;
        }

        public String getSkillName() {
            return skill_name;
        }

        public int getUseChance() {
            return useChance;
        }

        public int getTragetStaticObject() {
            return targetStaticObject;
        }

        public TargetType getSkillTargetType() {
            return skill_target_type;
        }

        public CubicDataOpCond getSkillOpCond() {
            return skill_op_cond;
        }
    }
}