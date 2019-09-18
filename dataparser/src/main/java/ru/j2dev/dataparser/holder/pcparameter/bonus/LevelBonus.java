package ru.j2dev.dataparser.holder.pcparameter.bonus;

import ru.j2dev.dataparser.annotations.value.DoubleValue;

/**
 * @author KilRoy
 */
public class LevelBonus {
    @DoubleValue
    public double lvl_1;
    @DoubleValue
    public double lvl_2;
    @DoubleValue
    public double lvl_3;
    @DoubleValue
    public double lvl_4;
    @DoubleValue
    public double lvl_5;
    @DoubleValue
    public double lvl_6;
    @DoubleValue
    public double lvl_7;
    @DoubleValue
    public double lvl_8;
    @DoubleValue
    public double lvl_9;
    @DoubleValue
    public double lvl_10;
    @DoubleValue
    public double lvl_11;
    @DoubleValue
    public double lvl_12;
    @DoubleValue
    public double lvl_13;
    @DoubleValue
    public double lvl_14;
    @DoubleValue
    public double lvl_15;
    @DoubleValue
    public double lvl_16;
    @DoubleValue
    public double lvl_17;
    @DoubleValue
    public double lvl_18;
    @DoubleValue
    public double lvl_19;
    @DoubleValue
    public double lvl_20;
    @DoubleValue
    public double lvl_21;
    @DoubleValue
    public double lvl_22;
    @DoubleValue
    public double lvl_23;
    @DoubleValue
    public double lvl_24;
    @DoubleValue
    public double lvl_25;
    @DoubleValue
    public double lvl_26;
    @DoubleValue
    public double lvl_27;
    @DoubleValue
    public double lvl_28;
    @DoubleValue
    public double lvl_29;
    @DoubleValue
    public double lvl_30;
    @DoubleValue
    public double lvl_31;
    @DoubleValue
    public double lvl_32;
    @DoubleValue
    public double lvl_33;
    @DoubleValue
    public double lvl_34;
    @DoubleValue
    public double lvl_35;
    @DoubleValue
    public double lvl_36;
    @DoubleValue
    public double lvl_37;
    @DoubleValue
    public double lvl_38;
    @DoubleValue
    public double lvl_39;
    @DoubleValue
    public double lvl_40;
    @DoubleValue
    public double lvl_41;
    @DoubleValue
    public double lvl_42;
    @DoubleValue
    public double lvl_43;
    @DoubleValue
    public double lvl_44;
    @DoubleValue
    public double lvl_45;
    @DoubleValue
    public double lvl_46;
    @DoubleValue
    public double lvl_47;
    @DoubleValue
    public double lvl_48;
    @DoubleValue
    public double lvl_49;
    @DoubleValue
    public double lvl_50;
    @DoubleValue
    public double lvl_51;
    @DoubleValue
    public double lvl_52;
    @DoubleValue
    public double lvl_53;
    @DoubleValue
    public double lvl_54;
    @DoubleValue
    public double lvl_55;
    @DoubleValue
    public double lvl_56;
    @DoubleValue
    public double lvl_57;
    @DoubleValue
    public double lvl_58;
    @DoubleValue
    public double lvl_59;
    @DoubleValue
    public double lvl_60;
    @DoubleValue
    public double lvl_61;
    @DoubleValue
    public double lvl_62;
    @DoubleValue
    public double lvl_63;
    @DoubleValue
    public double lvl_64;
    @DoubleValue
    public double lvl_65;
    @DoubleValue
    public double lvl_66;
    @DoubleValue
    public double lvl_67;
    @DoubleValue
    public double lvl_68;
    @DoubleValue
    public double lvl_69;
    @DoubleValue
    public double lvl_70;
    @DoubleValue
    public double lvl_71;
    @DoubleValue
    public double lvl_72;
    @DoubleValue
    public double lvl_73;
    @DoubleValue
    public double lvl_74;
    @DoubleValue
    public double lvl_75;
    @DoubleValue
    public double lvl_76;
    @DoubleValue
    public double lvl_77;
    @DoubleValue
    public double lvl_78;
    @DoubleValue
    public double lvl_79;
    @DoubleValue
    public double lvl_80;
    @DoubleValue
    public double lvl_81;
    @DoubleValue
    public double lvl_82;
    @DoubleValue
    public double lvl_83;
    @DoubleValue
    public double lvl_84;
    @DoubleValue
    public double lvl_85;
    @DoubleValue
    public double lvl_86;
    @DoubleValue
    public double lvl_87;
    @DoubleValue
    public double lvl_88;
    @DoubleValue
    public double lvl_89;
    @DoubleValue
    public double lvl_90;
    @DoubleValue
    public double lvl_91;
    @DoubleValue
    public double lvl_92;
    @DoubleValue
    public double lvl_93;
    @DoubleValue
    public double lvl_94;
    @DoubleValue
    public double lvl_95;
    @DoubleValue
    public double lvl_96;
    @DoubleValue
    public double lvl_97;
    @DoubleValue
    public double lvl_98;
    @DoubleValue
    public double lvl_99;

    public double returnValue(final int level) {
        try {
            return getClass().getField("lvl_" + level).getDouble(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}